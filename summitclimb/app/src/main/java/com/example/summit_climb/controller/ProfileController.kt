package com.example.summit_climb.controller
import com.example.summit_climb.User
import com.example.summit_climb.UserRepository
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ProfileController(
    private val repository: UserRepository = UserRepository() ,
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var notificationsStatus by mutableStateOf(true)
        private set
    var deleteStatus by mutableStateOf(false)
        private set

    init {
        loadProfile()
    }

    private fun fillDetails(user: User) {
        name = user.name
        email = user.email
        phone = user.phoneNumber
        notificationsStatus = !user.notificationsOptedOut
    }

    fun loadProfile() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        repository.getUser(uid) { details ->
            currentUser = details
            fillDetails(details)
        }
    }

    fun saveProfile() {
        val user = currentUser ?: return

        val tempUser = User(
            name = name,
            email = email,
            phoneNumber = phone
        )

        user.name = name
        user.email = email
        user.phoneNumber = phone
        repository.saveProfile(user)
    }

    fun deleteProfile() {
        val user = currentUser ?: return
        val firebaseAccount = Firebase.auth.currentUser ?: return

        firebaseAccount.delete()
            .addOnSuccessListener {
                repository.deleteUser(user)
                currentUser = null
                deleteStatus = true
            }

    }
    fun setNotifications(on: Boolean) {
        val user = currentUser ?: return
        notificationsStatus = on
        user.notificationsOptedOut = !on
        repository.updateNotificationOpt(user.userId, !on)
    }

}

