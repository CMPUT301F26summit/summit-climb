package com.example.summit_climb.controller

import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification

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

    // stops listening for notifications
    fun stopListening() {
        removeListener?.invoke()
        removeListener = null
    }
}