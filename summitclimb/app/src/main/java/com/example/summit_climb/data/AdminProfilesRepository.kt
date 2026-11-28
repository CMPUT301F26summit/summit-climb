package com.example.summit_climb.data

import com.example.summit_climb.model.AdminProfileView
import com.example.summit_climb.ui.admin.AdminBookingView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Loads every user profile and saves admin changes.
 * Counts each profile's booking, listing, and campsites collections.
 *
 * @param db: the Firestore database to read from and write to.
 */
class AdminProfilesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
){
    companion object {

        // collections
        const val USERS = "users"
        const val RESERVATIONS = "reservations"
        const val CAMPSITES = "campsites"


        const val FIELD_STATUS = "accountStatus"
        const val FIELD_FLAGGED = "flagged"
        const val FIELD_RESERVATION_ENTRANT = "entrantId"
        const val FIELD_CAMPSITE_OWNER = "ownerId"

        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_DEACTIVATED = "DEACTIVATED"
    }

    /**
     * Tracks changes to users, reservations and campsites.
     *
     * @param userProfiles: called with the up-to-date list of profiles.
     * @param onError: doesn't read userid
     * @return a function that stops updating. Call it when the screen is closed.
     */
    fun updateChanges(
        userProfiles: (List<AdminProfileView>) -> Unit,
        onError: (Exception) -> Unit): () -> Unit {
        var users: List<DocumentSnapshot>? = null
        var reservations: List<DocumentSnapshot> = emptyList()
        var campsites: List<DocumentSnapshot> = emptyList()

        fun display() {
            val users = users ?: return
            val profiles = mutableListOf<AdminProfileView>()

            for (doc in users) {
                val status = doc.getString(FIELD_STATUS) ?: STATUS_ACTIVE

                profiles.add(
                    AdminProfileView(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unentered name",
                        email = doc.getString("email") ?: "Unentered email",
                        phoneNumber = doc.getString("phoneNumber") ?: doc.getString("phone") ?: "",
                        profileImageUrl = doc.getString("profileImageUrl") ?: doc.getString("photoUrl") ?: "",
                        activeProfile = status == STATUS_ACTIVE,
                        reservations = reservations.count { it.getString(FIELD_RESERVATION_ENTRANT) == doc.id },
                        listings = campsites.count { it.getString(FIELD_CAMPSITE_OWNER) == doc.id },
                        flagged = doc.getBoolean(FIELD_FLAGGED) ?: false
                    )
                )
            }

            userProfiles(profiles)
        }

        val updateUserFirestore = db.collection(USERS).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            users = snap?.documents
            display()
        }

        val updateReservations = db.collection(RESERVATIONS).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            reservations = snap?.documents ?: emptyList()
            display()
        }

        val updateCampsites = db.collection(CAMPSITES).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            campsites = snap?.documents ?: emptyList()
            display()
        }

        return {
            updateUserFirestore.remove()
            updateReservations.remove()
            updateCampsites.remove()
        }
    }

    /**
     * Activates or deactivates a user's account.
     *
     * @param userId: the user to change.
     * @param active: set true for active, false otherwise
     * @param onFailure: update cannot be saved.
     */
    fun setActive(
        userId: String,
        active: Boolean,
        onFailure: (Exception) -> Unit = {}
    ){
        db.collection(USERS).document(userId)
            .update(
                FIELD_STATUS,if (active)
                    STATUS_ACTIVE
                else
                    STATUS_DEACTIVATED
            )
            .addOnFailureListener(onFailure)
    }

    /**
     * Flags or unflags a user's profile for review.
     *
     * @param userId: the user to change.
     * @param flagged: sets true for flagged, false otherwise
     * @param onFailure: update cannot be saved.
     */
    fun setFlagged(
        userId: String,
        flagged: Boolean,
        onFailure: (Exception) -> Unit = {}
    ){
        db.collection(USERS).document(userId)
            .update(FIELD_FLAGGED, flagged)
            .addOnFailureListener(onFailure)
    }

    /**
     * Keeps reservations up to date.
     * Watches the reservations and campsites collections and sends the new list.
     *
     * @param userId the user whose bookings to load.
     * @param onReservation set to the up-to-date list of the user's bookings.
     * @param onError when the bookings can't be read.
     * @return a function that stops the updates. Call it when the screen is closed.
     */
    fun updateReservation(
        userId: String,
        onReservation: (List<AdminBookingView>) -> Unit,
        onError: (Exception) -> Unit
    ): () -> Unit {
        var reservations: List<DocumentSnapshot> = emptyList()
        var campsites: List<DocumentSnapshot> = emptyList()

        fun display() {
            val bookingViews = mutableListOf<AdminBookingView>()

            for (doc in reservations) {
                val campsite = campsites.find { it.id == doc.getString("campsiteId") }

                bookingViews.add(
                    AdminBookingView(
                        id = doc.id,
                        campsiteName = campsite?.getString("name") ?: "Campsite",
                        details = "Check-in: ${doc.getString("startDate") ?: ""} · ${doc.getString("endDate") ?: ""}",
                        active = (doc.getString("status") ?: "ACTIVE") == "ACTIVE"
                    )
                )
            }
            onReservation(bookingViews)
        }

        val updateReservations = db.collection(RESERVATIONS)
            .whereEqualTo(FIELD_RESERVATION_ENTRANT, userId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                reservations = snap?.documents ?: emptyList()
                display()
            }

        val updateCampsites = db.collection(CAMPSITES)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                campsites = snap?.documents ?: emptyList()
                display()
            }

        return {
            updateReservations.remove()
            updateCampsites.remove()
        }
    }

    fun UpdateListings(
        userId: String,
        onListings: (List<com.example.summit_climb.ui.admin.AdminCampsiteView>) -> Unit,
        onError: (Exception) -> Unit
    ): () -> Unit {
        val registration = db.collection(CAMPSITES)
            .whereEqualTo(FIELD_CAMPSITE_OWNER, userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err)
                    return@addSnapshotListener
                }
                val listings = snap?.documents?.map { doc ->
                    com.example.summit_climb.ui.admin.AdminCampsiteView(
                        id = doc.id,
                        name = doc.getString("name") ?: "Campsite",
                        location = doc.getString("location") ?: "Location",
                        status = doc.getString("listingStatus") ?: "APPROVED"
                    )
                } ?: emptyList()
                onListings(listings)
            }
        return { registration.remove() }
    }
}