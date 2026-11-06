package com.example.summit_climb

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/*Interfaces to improve access and communication with other files for ProfileControllerTest */
interface UserDataSource {
    fun getUser(uid: String, onFailure: (Exception) -> Unit = {}, onResult: (User) -> Unit)
    fun saveProfile(user: User, onFailure: (Exception) -> Unit = {}, onSuccess: () -> Unit = {})
    fun updateNotificationOpt(userId: String, optOut: Boolean, onFailure: (Exception) -> Unit = {})
    fun deleteUser(user: User)
}

interface ImageUploader {
    fun upload(imageUri: Uri, folder: String, onDone: (String?) -> Unit)

    fun delete(imageUrl: String, onDone: (Boolean) -> Unit = {})
}


interface AccountService {
    val currentUserId: String?

    fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class UserRepositoryAdapter(
    private val repository: UserRepository = UserRepository()
) : UserDataSource {

    override fun getUser(uid: String, onFailure: (Exception) -> Unit, onResult: (User) -> Unit) {
        repository.getUser(uid, onFailure = onFailure) { onResult(it) }
    }

    override fun saveProfile(user: User, onFailure: (Exception) -> Unit, onSuccess: () -> Unit) {
        repository.saveProfile(user, onFailure = onFailure, onSuccess = onSuccess)
    }

    override fun updateNotificationOpt(userId: String, optOut: Boolean, onFailure: (Exception) -> Unit) {
        repository.updateNotificationOpt(userId, optOut, onFailure = onFailure)
    }

    override fun deleteUser(user: User) {
        repository.deleteUser(user)
    }
}

class ImageServiceAdapter(
    private val service: ImageService = ImageService()
) : ImageUploader {

    override fun upload(imageUri: Uri, folder: String, onDone: (String?) -> Unit) {
        service.upload(imageUri, folder) { onDone(it) }
    }

    override fun delete(imageUrl: String, onDone: (Boolean) -> Unit) {
        service.delete(imageUrl) { onDone(it) }
    }
}

class FirebaseAccountService : AccountService {
    override val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    override fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val account = Firebase.auth.currentUser
        if (account == null) {
            onFailure(Exception("Nobody is signed in"))
            return
        }
        account.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}

/**
 * Controls the actions that a user can perform on their profile
 * Lets users view and edit information, toggle notifications, change their photo,
 * and delete their account
 * Actions that can fail take an onFailure callback, called with a message to show the user.
 *
 * Example:
 * val controller = ProfileController()
 * controller.name = "Mila"
 * controller.saveProfile()
 *
 * @param repository: where user information is read and saved
 * @param imageService: uploads and deletes profile photos
 * @param account: the signed-in account
 * @constructor Creates a controller for the signed-in user's profile.
 * With no arguments it uses the real Firebase versions. Tests pass fakes.
 */
class ProfileController(
    private val repository: UserDataSource = UserRepositoryAdapter(),
    private val imageService: ImageUploader = ImageServiceAdapter(),
    private val account: AccountService = FirebaseAccountService()
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")

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
        val uid = account.currentUserId ?: return
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
        if (account.currentUserId == null) return

        account.deleteAccount(
            onSuccess = {
                repository.deleteUser(user)
                currentUser = null
                deleteStatus = true
            },
            onFailure = {
                onFailure(Exception("Unable to delete account", it))
            }
        )
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