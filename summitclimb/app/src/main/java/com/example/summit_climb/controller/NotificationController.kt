package com.example.summit_climb.controller

import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType


class NotificationController(
    private val repository: NotificationRepository,
    private val userId: String
) {

    // stores the current user's notifications
    val notifications = mutableStateListOf<Notification>()

    // counts unread notifications
    val unreadCount: Int
        get() = notifications.count { !it.read }

    // keeps track of the Firestore listener
    private var removeListener: (() -> Unit)? = null

    init {
        startListening()
    }

    // starts listening for the user's notifications
    private fun startListening() {
        removeListener = repository.listenToUser(userId) { updatedNotifications ->
            notifications.clear()
            notifications.addAll(updatedNotifications)
        }
    }

    // marks a function as read
    fun markRead(notificationId: String) {
        repository.markRead(notificationId)
    }

    /** Sends sample notifications for testing ==> remove later */
    fun sendTestNotifications() {

        val samples = listOf(

            Notification(
                senderId = "admin",
                recipientId = userId,
                type = NotificationType.LOTTERY_WIN,
                text = "Congratulations! You've been selected for Cedar Valley. Please choose your reservation dates."
            ),

            Notification(
                senderId = "admin",
                recipientId = userId,
                type = NotificationType.LOTTERY_LOSS,
                text = "Unfortunately, you weren't selected for Lakeside Camp this time."
            ),

            Notification(
                senderId = "admin",
                recipientId = userId,
                type = NotificationType.RESERVATION_UPDATE,
                text = "Your reservation for Summit Camp has been confirmed."
            ),
        )

        samples.forEach { notification ->
            repository.send(notification)
        }
    }

    // Stops listening for notifications
    fun stopListening() {
        removeListener?.invoke()
        removeListener = null
    }
}