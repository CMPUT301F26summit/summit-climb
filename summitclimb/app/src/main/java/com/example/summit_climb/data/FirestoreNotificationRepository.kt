package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.google.firebase.firestore.FirebaseFirestore

// Purpose: saves and loads notifications using the Firestore "notifications" collection
// Known problems: None yet.

/** Firestore version of NotificationRepository */
class FirestoreNotificationRepository(
    private val db: FirebaseFirestore
) : NotificationRepository {
    private val notificationsRef = db.collection("notifications")

    private val _notifications = mutableStateListOf<Notification>()
    override val notifications: List<Notification>
        get() = _notifications

    // watches Firestore, whenever something changes, refill the local list
    init {
        notificationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("NotificationRepo", "Listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fresh = snapshot.toObjects(Notification::class.java)
                _notifications.clear()
                _notifications.addAll(fresh)
            }
        }
    }

    /** saves a new notification. Firestore makes the ID */
    override fun send(notification: Notification) {
        val doc = notificationsRef.document()
        doc.set(notification.copy(id = doc.id))
    }

    /** marks a notification as read */
    override fun markRead(notificationId: String) {
        notificationsRef.document(notificationId).update("read", true)
    }

    /** one user's inbox, newest first */
    override fun forUser(userId: String): List<Notification> =
        _notifications
            .filter { it.recipientId == userId }
            .sortedByDescending { it.timestamp }

    /** notifications sent by one person, newest first */
    override fun sentBy(senderId: String): List<Notification> =
        _notifications
            .filter { it.senderId == senderId }
            .sortedByDescending { it.timestamp }
}

