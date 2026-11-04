package com.example.summit_climb

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the Firestore operations for campsites.
 * Firestore stores campsites in a `campsites` collection. A snapshot listener keeps
 * [campsites] up to date, so any screen that reads it is redrawn when Firestore changes.
 * Screens need call this class (by [CampsiteSearchController]) instead of
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

    val campsite: List<Campsite> = _campsites

    init {
        campRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                return@addSnapshotListener
            }
            _campsites.clear()

            snapshots?.documents?.forEach { document ->
                if (error != null){
                    return@addSnapshotListener
                }
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null){
                    campsite.id = document.id
                    _campsites.add(campsite)
                }
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
        return _campsites.find { campsite -> campsite.id == campsiteId}
    }

    /**
     * Gets every campsite with listing status [ListingStatus.APPROVED].
     * Entrant searches start from this list. [CampsiteSearchController.filterCampsites]
     * @return the approved campsites.
     */
    fun approvedCampsites(): List<Campsite>{
        return  _campsites.filter { campsite -> campsite.listingStatus == ListingStatus.APPROVED }
    }

    /**
     * Saves a rating and updates the campsite's average rating.
     * The rating is stored inside the campsite document, so rating and new
     * average are saved together. If entrant already rated this campsite,
     * new rating replaces the old one (see [Campsite.setRating]).
     *
     * @param rating: rating to save.
     */
    fun saveRating(rating: Rating){
        val campsite = getCampsite(rating.campsiteId) ?: return
        val updatedCampsite = campsite.copy()
        updatedCampsite.setRating(rating.entrantId, rating.stars)
        campRef.document(updatedCampsite.id).set(updatedCampsite)
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
    fun searchCampsites(keyword: String, status: ListingStatus?): List<Campsite>{
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
    fun updateListingStatus(campsiteId: String, status: ListingStatus, adminNotes: String){
        campRef.document(campsiteId).update(
            "listingStatus", status.name,
            "adminNotes", adminNotes
        )
    }

    /**
     * Deletes a campsite. Admins use.
     * @param campsiteId: campsite to delete.
     */
    fun deleteCamp(campsiteId: String){
        campRef.document(campsiteId).delete()
    }
}