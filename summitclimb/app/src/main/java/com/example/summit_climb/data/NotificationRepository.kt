package com.example.summit_climb.data

import com.example.summit_climb.model.Notification

/** Defines the operations available for managing notifications. */
interface NotificationRepository {

    /** Returns all notifications and updates when Firestore changes */
    val notifications: List<Notification>

    /** Saves a new notification to Firestore */
    fun send(notification: Notification)

    /** Marks a notification as read */
    fun markRead(notificationId: String)

    /** Returns a user's notifications, newest first. */
    fun forUser(userId: String): List<Notification>

    /** Returns notifications sent be a specific user, newest first */
    fun sentBy(senderId: String): List<Notification>

    /**
     * Listens for changes to a specific user's notifications.
     *
     * Calls the appropriate callback when a notification is added,
     * modified, or removed
     *
     * @param userId ID of the notification recipient
     * @param onAdded Callback for newly added notifications
     * @param onModified Callback for modified notifications
     * @param onRemoved Callback for removed notifications
     * @return A function that stops the listener when called
     */
    fun listenToUser(
        userId: String,
        onAdded: (Notification) -> Unit,
        onModified: (Notification) -> Unit,
        onRemoved: (Notification) -> Unit
    ): () -> Unit
}
