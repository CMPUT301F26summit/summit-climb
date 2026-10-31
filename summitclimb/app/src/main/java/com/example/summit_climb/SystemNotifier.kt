package com.example.summit_climb

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.summit_climb.model.Notification

// Purpose: show a notification in the phone's notification bar

/** Shows Android notifications using NotificationManager. */
class SystemNotifier(private val context: Context) {

    companion object {
        // every notification we show belongs to this channel
        const val CHANNEL_ID = "summit_notifications"
        const val EXTRA_SCREEN = "screen"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Summit Climb", NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** shows one notification from the app's own Notification model */
    fun show(notification: Notification, screen: String? = null) {
        show(notification.id, notification.text, screen)
    }

    /** shows one notification: used directly by the FCM service */
    fun show(id: String, text: String, screen: String? = null) {
        // false if the user turned notifications off
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // tapping opens the app and tells it which screen to go to
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            screen?.let { putExtra(EXTRA_SCREEN, it) }
        }
        val pending = PendingIntent.getActivity(
            context, id.hashCode(), intent,     // unique request code so extras aren't reused
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val built = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Summit Climb")
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        // the ID stops the same notification from appearing twice
        NotificationManagerCompat.from(context).notify(id.hashCode(), built)
    }
}