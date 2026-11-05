package com.example.summit_climb.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType

// Purpose: keeps the current user's notifications up to date while the app is open,
//   counts the unread ones, tells the screens which one just arrived (for the in-app
//   pop-up), marks one as read, and works out which screen a tapped notification opens.
// Known problems:
//          - it does not show notifications in the phone's notification bar yet
//          (planned for the final checkpoint).
//          - it does not check whether the user turned notifications off

class NotificationController(
    private val repository: NotificationRepository,
    private val currentUserId: () -> String?
) {

    // stores the current user's notifications
    val notifications = mutableStateListOf<Notification>()

    // counts unread notifications
    val unreadCount: Int
        get() = notifications.count { !it.read }

    // stops the Firestore listener
    private var removeListener: (() -> Unit)? = null

    // anything created before this existed already; only later ones get a pop-up
    private var listeningSince = 0L

    /** starts watching the logged-in user's notifications (call after login and on launch) */
    fun startListening() {
        val id = currentUserId() ?: return
        removeListener?.invoke()                 // never run two listeners at once
        notifications.clear()                    // the listener adds them back from Firestore
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
    }

    // adds one notification in the right spot so the list stays newest first
    private fun addNewestFirst(notification: Notification) {
        if (notifications.any { it.id == notification.id }) return   // never add twice
        val index = notifications.indexOfFirst { it.timestamp < notification.timestamp }
        if (index == -1) notifications.add(notification) else notifications.add(index, notification)
    }

    // marks one notification as read
    fun markRead(notificationId: String) {
        repository.markRead(notificationId)
    }

    /** the route of the screen this notification opens, or null to stay where we are */
    fun destinationFor(notification: Notification): String? = when (notification.type) {
        NotificationType.LOTTERY_WIN,
        NotificationType.LOTTERY_LOSS -> "entrantLotteries"
        NotificationType.RESERVATION_UPDATE -> null   // set once a My Reservations route exists
        NotificationType.MESSAGE -> null
    }
}
