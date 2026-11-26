package com.example.summit_climb.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.summit_climb.controller.Rating
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.ListingStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.summit_climb.service.ImageService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log

/**
 * Handles the Firestore operations for campsites.
 * Firestore stores campsites in a `campsites` collection. A snapshot listener keeps
 * [campsites] up to date, so any screen that reads it is redrawn when Firestore changes.
 * Screens need call this class (by [com.example.summit_climb.controller.CampsiteSearchController]) instead of
 * working with Firestore directly.
 *
 * Example:
 * val repository = CampsiteRepository()
 * val pineLake = repository.getCampsite("campsite1")
 * @constructor Connects to the `campsites` collection and starts listening for changes.
 */
class CampsiteRepository {
    private val db = Firebase.firestore
    private val campRef = db.collection("campsites")
    private val _campsites = mutableStateListOf<Campsite>()

    var isLoaded by mutableStateOf(false)
        private set

    val campsite: List<Campsite> = _campsites

    init {
        campRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshots != null) {
                _campsites.clear()

                snapshots.documents.forEach { document ->
                    val campsite = document.toObject(Campsite::class.java)

                    if (campsite != null) {
                        campsite.id = document.id
                        _campsites.add(campsite)
                    }
                }
                isLoaded = true
            }
        }
    }


    /**
     * Gets one campsite by its ID.
     *
     * @param campsiteId: Firestore document ID of the campsite.
     * @return the campsite, or null if no campsite has this ID.
     */
    fun getCampsite(campsiteId: String): Campsite? {
        return _campsites.find { campsite -> campsite.id == campsiteId }
    }

    fun fetchCampsite(campsiteId: String, onResult: (Campsite?) -> Unit) {
        if (campsiteId.isBlank()) {
            onResult(null)
            return
        }
        campRef.document(campsiteId).get()
            .addOnSuccessListener { doc ->
                val campsite = doc.toObject(Campsite::class.java)
                campsite?.id = doc.id
                onResult(campsite)
            }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * Gets every campsite with listing status [com.example.summit_climb.model.ListingStatus.APPROVED].
     * Entrant searches start from this list. [com.example.summit_climb.controller.CampsiteSearchController.filterCampsites]
     * @return the approved campsites.
     */
    fun approvedCampsites(): List<Campsite> {
        return _campsites.filter { campsite -> campsite.listingStatus == ListingStatus.APPROVED }
    }

    /**
     * Saves a rating and review for a specific reservation.
     *
     * Uses a Firestore transaction to prevent duplicate ratings and
     * keep the campsite's ratings, reviews, and average in sync.
     *
     * @param rating: rating and review to save.
     * @param onSuccess called when the rating is successfully saved
     * @param onFailure called when validation or saving fails
     */
    fun saveRating(
        rating: Rating,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!rating.isValid()) {
            onFailure(IllegalArgumentException("Invalid rating information."))
            return
        }

        val campsiteRef = campRef.document(rating.campsiteId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(campsiteRef)

            if (!snapshot.exists()) {
                throw IllegalStateException("Campsite not found.")
            }

            // Read existing ratings and reviews from Firestore.
            val existingRatings = snapshot.get("ratings") as? Map<*, *> ?: emptyMap<Any, Any>()
            val existingReviews = snapshot.get("reviews") as? Map<*, *> ?: emptyMap<Any, Any>()

            // Prevent the same reservation from being rated twice.
            if (existingRatings.containsKey(rating.reservationId)) {
                throw IllegalStateException("This reservation has already been rated.")
            }

            val updatedRatings = existingRatings.entries.associate {
                it.key.toString() to (it.value as Number).toInt()
            }.toMutableMap()

            val updatedReviews = existingReviews.entries.associate {
                it.key.toString() to it.value.toString()
            }.toMutableMap()

            updatedRatings[rating.reservationId] = rating.stars
            updatedReviews[rating.reservationId] = rating.review.trim()

            val averageRating = updatedRatings.values.average()

            // Update all rating information together.
            transaction.update(
                campsiteRef,
                mapOf(
                    "ratings" to updatedRatings,
                    "reviews" to updatedReviews,
                    "avgRating" to averageRating
                )
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { error ->
            onFailure(error)
        }
    }

    /**
     * Gets all campsites, searched by keyword and filtered by listing status. Admins use.
     * Example: `searchAllCampsites("lake", ListingStatus.PENDING)`
     * returns pending campsites whose name, description or tags contain "lake".
     *
     * @param keyword: word to search for. Empty matches every campsite.
     * @param status: listing status to keep, or `null` to keep every status.
     * @return the matching campsites.
     */
    fun searchCampsites(keyword: String, status: ListingStatus?): List<Campsite> {
        return _campsites.filter { campsite ->
            val matchesKeyword = campsite.keywordMatch(keyword)
            val matchesStatus = status == null || campsite.listingStatus == status
            matchesStatus && matchesKeyword
        }
    }

    /**
     * Changes a campsite's listing status and admin notes. Admins use.
     * Example: `updateListingStatus("campsite1", ListingStatus.REJECTED, "Photos are missing")`
     *
     * @param campsiteId: campsite to update.
     * @param status: new listing status.
     * @param adminNotes: admin's notes(replaces any old notes).
     */
    fun updateListingStatus(campsiteId: String, status: ListingStatus, adminNotes: String) {
        val ref = campRef.document(campsiteId)
        ref.update("listingStatus", status.name, "adminNotes", adminNotes)
            .addOnSuccessListener {
                ref.get().addOnSuccessListener { doc ->
                    campStatusCheck(doc.getString("ownerId") ?: "")
                }
            }
    }

    /**
     * Deletes a campsite. Admins use.
     * @param campsiteId: campsite to delete.
     */
    fun deleteCamp(campsiteId: String) {
        val ref = campRef.document(campsiteId)
        ref.get().addOnSuccessListener { doc ->
            val ownerId = doc.getString("ownerId") ?: ""
            ref.delete().addOnSuccessListener { campStatusCheck(ownerId) }
        }
    }

    /**
     * Deletes any registered campsites.
     * @param userId: campsite to delete.
     */
    fun deleteListing(userId: String) {
        campRef.whereEqualTo("ownerId", userId).get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.delete()
                val imageUrl = doc.getString("promoImageUrl") ?: ""
                if (imageUrl.isNotBlank()) {
                    ImageService().delete(imageUrl)
                }
            }
            UserRepository().removeOwnerRole(userId)
        }
    }

    /**
     * Checks if the user has any approved listings, if not owner role is removed.
     * @param userId: campsite to delete.
     */
    fun campStatusCheck(ownerId: String) {
        Log.d("checkCamp", "check called for '$ownerId'")
        if (ownerId.isBlank()) return
        val approvedCount = _campsites.count {
            it.ownerId == ownerId && it.listingStatus == ListingStatus.APPROVED
        }
        val users = UserRepository()

        if (approvedCount == 0) {
            users.removeOwnerRole(ownerId)
        } else {
            users.addOwnerRole(ownerId)
        }
    }
}

