package com.example.summit_climb

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manages Firebase Cloud Messaging (FCM) registration token
 *
 * Saves the device's FCM token to the corresponding user's
 * Firestore document so the backend can use it to send
 * push notifications to that user.
 */
class FcmTokenManager {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Saves or updates the FCM registration token for a user
     *
     * Updates only the "fcmToken" field in the user's Firestore
     * document, keeping the user's other profile information
     *
     * Logs whether the token was saved successfully or whether
     * an error occurred
     *
     * @param userId Firebase Authentication UID of the user
     * @param token FCM registration token assigned to the device
     *
     */
    fun saveToken(userId: String, token: String) {
        db.collection("users")
            .document(userId)
            .set(
                mapOf("fcmToken" to token),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener {
                Log.d("FCM", "FCM token saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("FCM", "Failed to save FCM token", exception)
            }
    }
}