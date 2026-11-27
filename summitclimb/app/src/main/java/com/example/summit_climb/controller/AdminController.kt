package com.example.summit_climb.controller

import com.example.summit_climb.data.AdminProfilesRepository
import com.example.summit_climb.data.UserRepository
import com.example.summit_climb.model.AdminProfileView
import com.example.summit_climb.model.UserRole
import com.example.summit_climb.ui.admin.AdminBookingView
import com.google.firebase.auth.FirebaseAuth

/**
 * Admin controller.
 * Only admins can access the screen, profiles can be searched and filtered,
 * and an admin cannot deactivate their own account.
 *
 * @param repository: reads and writes the profiles in Firestore.
 * @param currentUserId: returns the id of the user, or null if nobody is signed in.
 */
class AdminController(
    private val repository: AdminProfilesRepository = AdminProfilesRepository(),
    private val currentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid }
){

    /**
     * Checks that the current user has the ADMIN role.
     *
     * @param onResult: true if the user is an admin, otherwise false.
     */
    fun checkAdmin(onResult: (Boolean) -> Unit) {
        val uid = currentUserId()
        if (uid == null) {
            onResult(false)
            return
        }
        UserRepository().getUser(uid) { user ->
            onResult(UserRole.ADMIN in user.roles)
        }
    }

    /**
     * Starts updating for all profiles.
     *
     * @param onProfiles: called with the up-to-date list of profiles every time one changes.
     * @param onError: called if the profiles can't be read.
     * @return a function that stops updating.
     */
    fun updateProfiles(
        onProfiles: (List<AdminProfileView>) -> Unit,
        onError: (Exception) -> Unit
    ): () -> Unit = repository.updateChanges(onProfiles, onError)

    /**
     * Deactivates or reactivates an account.
     *
     * @param profile: the profile to change.
     * @param active: to reactivate is set to true, and false for deactivation.
     * @param onFailure: called if the change is not allowed or could not be saved.
     */
    fun setActive(
        profile: AdminProfileView,
        active: Boolean,
        onFailure: (Exception) -> Unit = {}
    ){
        if (!active && profile.id == currentUserId()) {
            return
        }
        repository.setActive(profile.id, active, onFailure)
    }

    /**
     * Flags or unflags a profile for review.
     *
     * @param profile: the profile to update.
     * @param flagged: flagged is true, otherwise unflagged
     * @param onFailure: called if the change could not be saved.
     */
    fun setFlagged(
        profile: AdminProfileView,
        flagged: Boolean,
        onFailure: (Exception) -> Unit = {}
    ){
        repository.setFlagged(profile.id, flagged, onFailure)
    }

    fun getBookingsForUser(
        userId: String,
        onBookings: (List<AdminBookingView>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): () -> Unit = repository.updateReservation(userId, onBookings, onError)

    companion object {
        /**
         * Searches and filters a list of profiles.
         *
         * @param profiles: the profiles to start from.
         * @param searchText: text to look for in the name or email.
         * @param active: sets true for active accounts only, otherwise false for deactivated only.
         * @param flaggedProfiles: sets true to keep only flagged profiles.
         * @return the profiles that match, sorted by name.
         */
        fun filterProfiles(
            profiles: List<AdminProfileView>,
            searchText: String = "",
            active: Boolean? = null,
            flaggedProfiles: Boolean = false
        ): List<AdminProfileView> {
            val matches = profiles
                .filter { user ->
                    searchText == "" ||
                            user.name.contains(searchText, ignoreCase = true) ||
                            user.email.contains(searchText, ignoreCase = true)
                }
                .filter { user -> active == null || user.activeProfile == active }
                .filter { user -> !flaggedProfiles || user.flagged }

            return matches.sortedBy { it.name.lowercase() }
        }
    }
}