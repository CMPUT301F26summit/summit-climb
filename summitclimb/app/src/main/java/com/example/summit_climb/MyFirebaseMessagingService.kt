package com.example.summit_climb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messaging (FCM) messages.
 *
 * This service receives push notifications sent to the app
 * and displays them as Android system notifications.
 *
 * @constructor Creates the Firebase messaging service
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when Firebase generates a new FCM registration token
     * or refreshed an existing token
     *
     * Saves the new token to the authenticated user's Firestore
     * document so that the backend can use it to deliver push
     * notifications to this app installation
     *
     *
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "New FCM token generated")

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            FcmTokenManager().saveToken(userId, token)
        } else {
            Log.d("FCM", "No authenticated user. Token not saved.")
        }
    }

    /**
     * Called when an FCM message is received while this
     * service is handling the message.
     *
     * Checks notification permission, retrieves the message
     * title and body, builds an Android notification, and
     * displays it using NotificationManagerCompat.
     *
     * @param message Incoming Firebase Cloud Messaging message
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Push notification received")

        // check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // get notification content from the incoming FCM message
        val title = message.notification?.title
            ?:message.data["title"]
            ?: "Summit Climb"

        val body = message.notification?.body
            ?:message.data["body"]
            ?: "You have a new update."

        // build the Android notification
        val notification = NotificationCompat.Builder(
            this,
            "summit_climb_notifications"
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // display the notification
        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}