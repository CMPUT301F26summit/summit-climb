/*
package com.example.summit_climb


import com.example.summit_climb.User
import com.example.summit_climb.UserRepository
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.net.Uri
import com.example.summit_climb.ImageService

/**
 * Controls the actions that a user can perform on their profile
 * Lets users view and edit information, toggle notifications, and delete their account
 * Actions that can fail take an onFailure callback, called with a message to show the user.
 *
 * Example:
 * val controller = ProfileController()
 * controller.name = "Mila"
 * controller.saveProfile()
 *
 * @param repository: repository containing user information
 * @constructor Creates a controller for the signed-in user's profile
 */
class ProfileController (
    private val repository: UserRepository = UserRepository() ,
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")

    private val imageService = ImageService()

    var photoUrl by mutableStateOf("")
    var uploadPhoto by mutableStateOf(false)

    var notificationsStatus by mutableStateOf(true)
    var deleteStatus by mutableStateOf(false)

    init {
        loadProfile()
    }

    /**
     * Copies a user's details into the editable profile fields
     * @param user: signed-in user whose details are shown
     */
    private fun fillDetails(user: User) {
        name = user.name
        email = user.email
        phone = user.phoneNumber
        photoUrl = user.profileImageUrl
        notificationsStatus = !user.notificationsOptedOut
    }

    /**
     * If a user is signed in, obtains their existing details
     * and fills the editable fields.
     * Does nothing if nobody is signed in.
     *
     * @param onFailure: provides message if loading fails
     */
    fun loadProfile(onFailure: (Exception) -> Unit = {}) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        repository.getUser(uid, onFailure = {
            onFailure(Exception("Unable to load profile", it))
        }) { user ->
            currentUser = user
            fillDetails(user)
        }
    }

    /**
     * Updates the user's name, email, and/or phone number.
     * Does nothing if the profile has not loaded.
     *
     * @param onFailure: provides message if saving fails
     */
    fun saveProfile(onFailure: (Exception) -> Unit = {}) {
        val user = currentUser ?: return
        user.name = name
        user.email = email
        user.phoneNumber = phone
        repository.saveProfile(user, onFailure = {
           onFailure(Exception("Unable to save profile", it))
        })
    }

    /**
     * Deletes the signed-in user's profile and account.
     * Does nothing if the profile has not loaded or nobody is signed in.
     *
     * @param onFailure: provides message if deletion fails
     */
    fun deleteProfile(onFailure: (Exception) -> Unit = {}) {
        val user = currentUser ?: return
        val firebaseAccount = Firebase.auth.currentUser ?: return

        firebaseAccount.delete()
            .addOnSuccessListener {
                repository.deleteUser(user)
                currentUser = null
                deleteStatus = true
            }
            .addOnFailureListener {
                onFailure(Exception("Unable to delete account", it))
            }
    }

    /**
     * Toggles notifications on and off for the user.
     * Does nothing if the profile has not loaded.
     *
     * @param on: 'true' if user wants notifications, 'false' if they do not
     * @param onFailure: provides a message if switching fails
     */
    fun setNotifications(on: Boolean, onFailure: (Exception) -> Unit = {}) {
        val user = currentUser ?: return
        notificationsStatus = on
        user.notificationsOptedOut = !on
        repository.updateNotificationOpt(
            user.userId,
            !on,
            onFailure = {
                notificationsStatus = !on
                user.notificationsOptedOut = on
                onFailure(Exception("Unable to update notifications", it))
            }
        )
    }

    /**
     * Uploads a new profile photo, saves its URL and deletes the old photo.
     * Keeps old photo if update fails
     * Does nothing if the profile has not loaded.
     *
     * @param uri: identifies chosen image
     * @param onFailure: provides a message if upload fails
     */
    fun updateProfilePhoto(uri: Uri, onFailure: (Exception) -> Unit = {}) {
        val user = currentUser ?: return
        val oldUrl = user.profileImageUrl
        uploadPhoto = true
        imageService.upload(uri, "profilePicture/${user.userId}") { url ->
            if (url == null) {
                uploadPhoto = false
                onFailure(Exception("Unable to upload photo"))
            } else {
                user.profileImageUrl = url
                repository.saveProfile(
                    user,
                    onFailure = {
                        uploadPhoto = false
                        user.profileImageUrl = oldUrl
                        onFailure(Exception("Unable to save photo"))
                    },
                    onSuccess = {
                        uploadPhoto = false
                        photoUrl = url
                        if (oldUrl.isNotBlank()) {
                            imageService.delete(oldUrl)
                        }
                    }
                )
            }
        }
    }
}
 */

