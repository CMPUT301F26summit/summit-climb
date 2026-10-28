package com.example.summit_climb

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserRepository {
    private  val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val imageService = ImageService()
    private val _users = mutableStateListOf<User>()

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

    fun saveProfile(
        user:User
    ){
        userRef.document(user.userId).set(user)
    }

    fun updateNotificationOpt(
        userId:String,
        optOut: Boolean
    ){
        userRef.document(userId).update("notificationsOptedOut", optOut)
    }

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

    fun updateStatus(
        userId: String,
        status: AccountStatus
    ){
        userRef.document(userId).update("accountStatus", status.name)
    }
}