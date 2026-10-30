package com.example.summit_climb

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


/**
 * Handles the Firestore operations for user profiles.
 * Firestore stores users in a `users` collection.
 * Each document's ID is the user's device based User.userId. 
 * A snapshot listener keeps users up to date for the admin screens.
 *
 * Example:
 * ```
 * val deviceId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
 * userRepository.getOrCreateUser(deviceId) { user ->
 *     println("Hello ${user.name}")
 * }
 * ```
 *
 * @param lotteryRepository: used to remove a user's waitlist entries when the user is deleted.
 * @constructor Connects to the `users` collection and starts listening for changes.
 */
class UserRepository {
    private  val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val imageService = ImageService()
    private val _users = mutableStateListOf<User>()

    // Every user in Firestore. Updates automatically. Used by the admin screens.
    val users: List<User> = _users

    init{
        userRef.addSnapshotListener { snapshot, error ->
            if (error != null){
                return@addSnapshotListener
            }
            _users.clear()

            snapshot?.documents?.forEach { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _users.add(user)
                }
            }
        }
    }

    /**
     * Gets the user for this device or creates a new one on first launch.
     * A new user only has User.userId set and the default UserRole.ENTRANT role.
     * @param deviceId:  device based ID
     * @param onResult: called with the existing or newly created user.
     */
    fun getUser(
        deviceId: String,
        onResult: (User) -> Unit
    ){
        userRef.document(deviceId).get().addOnSuccessListener { document ->
            val exist = document.toObject(User::class.java)
            if (exist != null){
                // User already exists
                onResult(exist)
            }
            else{
                val newUser = User(userId = deviceId)
                userRef.document(deviceId).set(newUser)
                onResult(newUser)
            }
        }
    }

    /**
     * Saves profile changes by replacing the stored profile with user
     * @param user: user with the changes.
     */
    fun saveProfile(
        user:User
    ){
        userRef.document(user.userId).set(user)
    }

    /**
     * Saves the user's notification opt out setting.
     * @param userId: user to update.
     * @param optedOut: `true` to stop notifications, `false` to receive them.
     */
    fun updateNotificationOpt(
        userId:String,
        optOut: Boolean
    ){
        userRef.document(userId).update("notificationsOptedOut", optOut)
    }

    /**
     * Deletes the user along with their waitlist entries and profile image.
     * Entrants use this to delete their own profile. Admin can use it to delete a user.
     * @param user: user to be deleted.
     */
    fun deleteUser(
        user: User
    ){
        lotteryRepository.getUserEntries(user.userId){ entries ->
            entries.forEach{ entry ->
                lotteryRepository.removeWaitlistEntry(entry)
            }
        }

        if (user.profileImageUrl.isNotBlank()){
            imageService.deleteImage(user.profileImageUrl)
        }
        userRef.document(user.userId).delete()
    }

    /**
     * Gets all users, searched by name, email and filtered by account status. For admins.
     * Example: `searchUsers("alex", AccountStatus.ACTIVE)` 
     * returns active users whose name or email contains "alex".
     *
     * @param searchText: text the name or email must contain. Not case sensitive.
     * @param status: account status to keep or `null` to keep every status.
     * @return the matching users.
     */
    fun searchUsers(
        searchText:String,
        status: AccountStatus?
    ):List<User> {
        return _users.filter { user ->
            val nameMatch = user.name.contains(searchText.trim(), ignoreCase = true)
            val emailMatch = user.email.contains(searchText.trim(), ignoreCase = true)
            val statusMatch = status == null || user.accountStatus == status
            (nameMatch || emailMatch) && statusMatch
        }
    }

    /**
     * Changes a user's account status, for example to deactivate them. For admins.
     * @param userId: the user to update.
     * @param status: the new account status.
     */
    fun updateStatus(
        userId: String,
        status: AccountStatus
    ){
        userRef.document(userId).update("accountStatus", status.name)
    }
}