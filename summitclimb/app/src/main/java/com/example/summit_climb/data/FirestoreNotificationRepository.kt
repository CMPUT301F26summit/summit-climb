package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

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
                val fresh = snapshot.documents.mapNotNull { document ->
                    document.toObject(Notification::class.java)
                        ?.copy(id = document.id)
                }

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

    /** watches only this user's notifications and reports each change as it happens */
    override fun listenToUser(
        userId: String,
        onAdded: (Notification) -> Unit,
        onModified: (Notification) -> Unit,
        onRemoved: (Notification) -> Unit
    ): () -> Unit {
        val registration = notificationsRef
            .whereEqualTo("recipientId", userId)   // Firestore only sends this user's notifications
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // only the documents that changed, not the whole collection
                for (change in snapshot.documentChanges) {
                    val notification = change.document.toObject(Notification::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED -> onAdded(notification)
                        DocumentChange.Type.MODIFIED -> onModified(notification)
                        DocumentChange.Type.REMOVED -> onRemoved(notification)
                    }
                }
            }
        return { registration.remove() }
    }
}
