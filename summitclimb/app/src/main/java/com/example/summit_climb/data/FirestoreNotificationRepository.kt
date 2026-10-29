package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.google.firebase.firestore.FirebaseFirestore

// Purpose: saves and loads notifications using the Firestore "notifications" collection.
// Known problems:
//   - It downloads ALL notifications, not just the current user's, then picks the
//     right ones on the phone.
//   - It starts watching Firestore when created and never stops.
//   - Notifications are not blocked yet when a user has turned them off.

/** Firestore version of NotificationRepository. */
class FirestoreNotificationRepository(
    db: FirebaseFirestore
) : NotificationRepository {

    private val notificationsRef = db.collection("notifications")

    // local copy of the notifications
    private val _notifications = mutableStateListOf<Notification>()

    override val notifications: List<Notification>
        get() = _notifications

    // runs once when the repository is created, starts watching Firestore
    init {
        notificationsRef.addSnapshotListener { snapshot, error ->
            // something went wrong: print the error to Logcat and stop
            if (error != null) {
                Log.e("NotificationRepo", "Listen failed", error)
                return@addSnapshotListener
            }
            // otherwise replace local copy with the newest data from Firestore
            if (snapshot != null) {
                val fresh = snapshot.toObjects(Notification::class.java)
                _notifications.clear()
                _notifications.addAll(fresh)
            }
        }
    }

    /** sends a notification by storing it in Firestore. Firestore makes the ID */
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
            .filter { it.recipientId == userId }        // only notifications sent to this user
            .sortedByDescending { it.timestamp }        // newest at the top

    /** notifications sent by one person, newest first */
    override fun sentBy(senderId: String): List<Notification> =
        _notifications
            .filter { it.senderId == senderId }         // only notifications this person sent
            .sortedByDescending { it.timestamp }        // newest at the top
}