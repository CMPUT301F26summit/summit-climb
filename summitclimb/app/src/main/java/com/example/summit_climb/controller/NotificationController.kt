package com.example.summit_climb.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType

/**
 * Manages the current user's notifications
 *
 * Listens for notification updates, tracks unread notifications,
 * marks notifications as read, and determined navigation destination
 * when a notification is selected.
 *
 * Known Issues:
 *  - Notifications are not displayed yet in the phone's notification bar
 *  - User preferences are not yet checked.
 */

/**
 * Controls notification data and behaviour for the current user
 *
 * @property repository Provides access to notification data
 * @property currentUserId Provides the ID of the currently logged-in user.
 */
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

    /**
     * Starts listening for notifications updates for the current user
     *
     * Removes any previous listener before starting  a new one
     * Clears the current notification list before loading updates
    */
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

    /**
     * Stops the notification listener and clears the current notification list.
     *
     * Should be called when the user logs out.
    */
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

    /**
     * Marks a notification as read.
     *
     * @param notificationId ID of the notification to mark as read.
    */
    fun markRead(notificationId: String) {
        repository.markRead(notificationId)
    }

    /**
     * Determines which screen should open when a notification is selected
     *
     * @param notification The selected notification
     * @return The navigation route, or null if no destination is available
     */
    fun destinationFor(notification: Notification): String? = when (notification.type) {
        NotificationType.LOTTERY_WIN,
        NotificationType.LOTTERY_LOSS -> "entrantLotteries"

        NotificationType.RESERVATION_UPDATE -> null   // set once a My Reservations route exists
        NotificationType.MESSAGE -> null
    }
}
