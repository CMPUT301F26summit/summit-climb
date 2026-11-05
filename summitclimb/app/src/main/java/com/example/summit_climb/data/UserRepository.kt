package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.AccountStatus
import com.example.summit_climb.model.User
import com.example.summit_climb.service.ImageService
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles Firestore operations for user profiles.
 * User documents are stored in the "users" collection.
 */
class UserRepository {

    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val imageService = ImageService()

    private val _users = mutableStateListOf<User>()
    val users: List<User> = _users

    init {
        userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserRepository", "Failed to load users", error)
                return@addSnapshotListener
            }

            _users.clear()

            snapshot?.documents?.forEach { document ->
                document.toObject(User::class.java)?.let { user ->
                    _users.add(user)
                }
            }
        }
    }

    /** Gets an existing user or creates a new one. */
    fun getUser(
        deviceId: String,
        onResult: (User) -> Unit
    ) {
        userRef.document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)

                if (user != null) {
                    onResult(user)
                } else {
                    val newUser = User(userId = deviceId)

                    userRef.document(deviceId)
                        .set(newUser)
                        .addOnSuccessListener {
                            onResult(newUser)
                        }
                        .addOnFailureListener { error ->
                            Log.e("UserRepository", "Failed to create user", error)
                        }
                }
            }
            .addOnFailureListener { error ->
                Log.e("UserRepository", "Failed to get user", error)
            }
    }

    /** Saves changes to a user's profile. */
    fun saveProfile(user: User) {
        userRef.document(user.userId)
            .set(user)
            .addOnFailureListener { error ->
                Log.e("UserRepository", "Failed to save profile", error)
            }
    }

    /** Updates the user's notification preference. */
    fun updateNotificationOpt(
        userId: String,
        optOut: Boolean
    ) {
        userRef.document(userId)
            .update("notificationsOptedOut", optOut)
            .addOnFailureListener { error ->
                Log.e("UserRepository", "Failed to update notifications", error)
            }
    }

    /**
     * Deletes the user's waitlist entries and user document.
     * Requests profile image deletion after Firestore cleanup succeeds.
     */
    fun deleteUser(user: User) {
        db.collectionGroup("entries")
            .whereEqualTo("entrantId", user.userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.size() >= 500) {
                    Log.e("UserRepository", "Too many waitlist entries to delete in one batch")
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.delete(userRef.document(user.userId))

                batch.commit()
                    .addOnSuccessListener {
                        if (user.profileImageUrl.isNotBlank()) {
                            imageService.delete(user.profileImageUrl) { success ->
                                if (!success) {
                                    Log.e("UserRepository", "Failed to delete profile image")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e("UserRepository", "Failed to delete user", error)
                    }
            }
            .addOnFailureListener { error ->
                Log.e("UserRepository", "Failed to load waitlist entries", error)
            }
    }

    /** Searches users by name, email, and account status. */
    fun searchUsers(
        searchText: String,
        status: AccountStatus?
    ): List<User> {
        val query = searchText.trim()

        return _users.filter { user ->
            val nameMatch = user.name.contains(query, ignoreCase = true)
            val emailMatch = user.email.contains(query, ignoreCase = true)
            val statusMatch = status == null || user.accountStatus == status

            (nameMatch || emailMatch) && statusMatch
        }
    }

    /** Updates a user's account status. */
    fun updateStatus(
        userId: String,
        status: AccountStatus
    ) {
        userRef.document(userId)
            .update("accountStatus", status.name)
            .addOnFailureListener { error ->
                Log.e("UserRepository", "Failed to update account status", error)
            }
    }
}