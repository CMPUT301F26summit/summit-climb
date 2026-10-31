package com.example.summit_climb

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Purpose: receives push notifications from Firebase Cloud Messaging and passes
// them to SystemNotifier to show in the notification bar.

/** Receives FCM pushes, even when the app is closed. */
class SummitMessagingService : FirebaseMessagingService() {

    // called when a message arrives while the app is in the foreground,
    // OR for any data-only message regardless of app state.
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val id = data["id"] ?: message.messageId ?: System.currentTimeMillis().toString()
        val text = data["text"] ?: message.notification?.body ?: return

        SystemNotifier(applicationContext).show(id, text, data["screen"])
    }

    // ---- add a comment here pls ------
    override fun onRegistered(installationId: String) {
        Log.d("SummitFCM", "Registered installation ID: $installationId")
        sendRegistrationToServer(installationId)
    }

    private fun sendRegistrationToServer(id:String) {
        // need to do: save this ID against the signed-in user in your backend/repository
    }
}