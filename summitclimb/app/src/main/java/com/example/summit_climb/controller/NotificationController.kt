package com.example.summit_climb.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType

// Purpose: gives the notification screens to the current user's notifications,
// the undread count, and a way to mark one as read. keeps the current user's notification
// up to date while the app is open, tells the screens which one just arrived, and which
// screen a tapped notification opens.
// Known problems:
//          - it does not show notifications in the phone's notification bar yet
//          (planned for the final checkpoint).

class NotificationController(
    private val repository: NotificationRepository,
    private val currentUserId: () -> String?
) {

    // stores the current user's notifications
    val notifications = mutableStateListOf<Notification>()

    // counts unread notifications
    val unreadCount: Int
        get() = notifications.count { !it.read }

    // the notification that just arrived while the app was open (null if none to show)
    var newest by mutableStateOf<Notification?>(null)
        private set

    // stops the Firestore listener
    private var removeListener: (() -> Unit)? = null

    // anything created before this existed already; only later ones get a pop-up
    private var listeningSince = 0L

    /** starts watching the logged-in user's notifications (call after login and on launch) */
    fun startListening() {
        val id = currentUserId() ?: return
        removeListener?.invoke()                 // never run two listeners at once
        notifications.clear()                    // the listener adds them back from Firestore
        newest = null
        listeningSince = System.currentTimeMillis()
        removeListener = repository.listenToUser(
            userId = id,
            onAdded = { addNewestFirst(it) },
            onModified = { changed ->
                // replace the old copy (for example, after it was marked read)
                val index = notifications.indexOfFirst { it.id == changed.id }
                if (index >= 0) notifications[index] = changed
            },
            onRemoved = { gone -> notifications.removeAll { it.id == gone.id } }
        )
    }

    /** stops watching (call when the user logs out) */
    fun stopListening() {
        removeListener?.invoke()
        removeListener = null
        notifications.clear()
        newest = null
    }

    // adds one notification in the right spot so the list stays newest first
    private fun addNewestFirst(notification: Notification) {
        if (notifications.any { it.id == notification.id }) return   // never add twice
        val index = notifications.indexOfFirst { it.timestamp < notification.timestamp }
        if (index == -1) notifications.add(notification) else notifications.add(index, notification)

        // a new unread one that arrived while the app is open gets the pop-up
        if (!notification.read && notification.timestamp > listeningSince) {
            newest = notification
        }
    }

    /** hides the pop-up */
    fun dismissBanner() {
        newest = null
    }

    // marks a function as read
    fun markRead(notificationId: String) {
        repository.markRead(notificationId)
    }

    /** the route of the screen this notification opens, or null to stay where we are */
    fun destinationFor(notification: Notification): String? = when (notification.type) {
        NotificationType.LOTTERY_WIN,
        NotificationType.LOTTERY_LOSS -> "myLotteries"
        NotificationType.RESERVATION_UPDATE -> "myReservations"
        NotificationType.MESSAGE -> null
    }

    /** Sends sample notifications to the logged-in user for testing ==> remove later */
    fun sendTestNotifications() {
        val id = currentUserId() ?: return
        listOf(
            Notification(
                senderId = "system", recipientId = id,
                type = NotificationType.LOTTERY_WIN,
                text = "Congratulations! You've been selected for Cedar Valley. Please choose your reservation dates."
            ),
            Notification(
                senderId = "system", recipientId = id,
                type = NotificationType.LOTTERY_LOSS,
                text = "Unfortunately, you weren't selected for Lakeside Camp this time."
            ),
            Notification(
                senderId = "system", recipientId = id,
                type = NotificationType.RESERVATION_UPDATE,
                text = "Your reservation for Summit Camp has been confirmed."
            )
        ).forEach { repository.send(it) }
    }
}
