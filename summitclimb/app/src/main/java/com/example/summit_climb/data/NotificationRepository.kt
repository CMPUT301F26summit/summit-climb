package com.example.summit_climb.data

import com.example.summit_climb.model.Notification

// Purpose: list of what the app can do with notifications
// Ability to:
//         - save a notification
//         - mark a notification as read
//         - get a user's notifications (their inbox)
//         - get the notifications an owner has sent
// Known problems: None yet.

/** Everything the app can do with notifications. */
interface NotificationRepository {

    /** all notifications, updates by itself when Firestore changes */
    val notifications: List<Notification>

    /** saves a new notification */
    fun send(notification: Notification)

    /** marks a notification as read */
    fun markRead(notificationId: String)

    /** one user's notifications, newest first (their inbox). */
    fun forUser(userId: String): List<Notification>

    /** notifications sent by one person (a campsite owner), newest first. */
    fun sentBy(senderId: String): List<Notification>
}