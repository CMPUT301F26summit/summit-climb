package com.example.summit_climb.controller

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.summit_climb.model.User
import com.example.summit_climb.data.UserRepository
import com.example.summit_climb.service.ImageService

/**
 * Controls the actions that a user can perform on their profile
 * Lets users view and edit information, toggle notifications, change their photo, and delete their account
 * Actions that can fail return a message to show the user.
 *
 * @param repository: where user information is read and saved
 * @param imageService: uploads and deletes profile photos
 * @constructor Creates a controller for the signed-in user's profile.
 */
class ProfileController(
    private val repository: UserRepository = UserRepository(),
    private val imageService: ImageService = ImageService(),
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")

    var photoUrl by mutableStateOf("")
    var uploadPhoto by mutableStateOf(false)

    var notificationsStatus by mutableStateOf(true)

    var profileLoaded by mutableStateOf(false)
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
        profileLoaded = true

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
        repository.getUser(uid) { user ->
            currentUser = user
            fillDetails(user)
        }
    }

    /**
     * Saves the changes to user's name, email, and/or phone number
     * Does nothing if the profile has not loaded.
     *
     * @param onSuccess: provides message if saving works
     * @param onFailure: provides message if saving fails
     */
    fun saveProfile(onSuccess: () -> Unit, onFailure: (Exception) -> Unit = {}) {
        val user = currentUser
        if (user == null) {
            onFailure(Exception("Your profile hasn't loaded yet. Please try again."))
            return
        }

        val updatedUser = User(
            name = name.trim(),
            email = email.trim(),
            phoneNumber = phone.trim()
        )

        if (!updatedUser.validName()) {
            onFailure(Exception("Please enter a name"))
            return
        }
        if (!updatedUser.validEmail()) {
            onFailure(Exception("Please enter a valid email"))
            return
        }
        if (!updatedUser.validPhone()) {
            onFailure(Exception("Please enter a valid 10 digit phone number"))
            return
        }

        user.name = updatedUser.name
        user.email = updatedUser.email
        user.phoneNumber = updatedUser.phoneNumber

        repository.saveProfile(user)
        onSuccess()
    }

    /**
     * Deletes the signed-in user's profile and account.
     * Does nothing if the profile has not loaded or nobody is signed in.
     *
     * @param onFailure: provides message if deletion fails
     */
    fun deleteProfile(onFailure: (Exception) -> Unit = {}) {
        val user = currentUser
        if (user == null) {
            onFailure(Exception("There was an error"))
            return
        }

        repository.deleteUser(user)
        currentUser = null
        name = ""
        email = ""
        phone = ""
        photoUrl = ""
        deleteStatus = true
    }

    /**
     * Toggles notifications on and off for the user.
     * Does nothing if the profile has not loaded.
     *
     * @param on: 'true' if user wants notifications, 'false' if they do not
     * @param onFailure: provides a message if switching fails
     */
    fun setNotifications(on: Boolean, onFailure: (Exception) -> Unit = {}) {
        val user = currentUser
        if (user == null) {
            onFailure(Exception("There was an error"))
            return
        }
        notificationsStatus = on
        user.notificationsOptedOut = !on
        repository.updateNotificationOpt(user.userId, !on)
    }

    /**
     * Ensures that the personal information fields are cleared upon account deletion
     */
    fun clearProfileFields() {
        name = ""
        email = ""
        phone = ""
        photoUrl = ""
        deleteStatus = true
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
        val user = currentUser
        if (user == null) {
            onFailure(Exception("There was an error"))
            return
        }
        val oldUrl = user.profileImageUrl
        uploadPhoto = true
        imageService.upload(uri, "campsites") { url ->
            uploadPhoto = false
            if (url == null) {
                onFailure(Exception("Unable to upload photo"))
            } else {
                user.profileImageUrl = url
                photoUrl = url
                repository.saveProfile(user)
                if (oldUrl.isNotBlank()) {
                    imageService.delete(oldUrl)
                }
            }
        }
    }

    /**
     * Removes the user's profile photo.
     * Removes the saved photo URL from the profile, then deletes the old photo file from storage.
     * Reports an error if the profile has not loaded.
     *
     * @param onFailure: provides an error if the profile has not loaded
     */

    fun removeProfilePhoto(onFailure: (Exception) -> Unit = {}) {
        val user = currentUser

        if (user == null) {
            onFailure(Exception("There was an error"))
            return
        }

        val oldUrl = user.profileImageUrl

        user.profileImageUrl = ""
        photoUrl = ""
        repository.saveProfile(user)
        if (oldUrl != "") {
            imageService.delete(oldUrl)
        }
    }
}
