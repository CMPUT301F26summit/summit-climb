package com.example.summit_climb.data

import com.example.summit_climb.model.Notification

/** Everything the app can do with notifications. */
interface NotificationRepository {

    /** all notifications, updates by itself when Firestore changes */
    val notifications: List<Notification>

    /** sends a notification by storing it in Firestore */
    fun send(notification: Notification)

    /** marks a notification as read */
    fun markRead(notificationId: String)

    /** one user's inbox, newest first */
    fun forUser(userId: String): List<Notification>

    /** notifications sent by one person (a campsite owner), newest first */
    fun sentBy(senderId: String): List<Notification>
}