package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Handles saving, retrieving, and listening for notifications
 * using the Firestore "notifications" collection
 *
 * Known Issues:
 *  - The initial snapshot listener downloads the entire notifications collection
 *  - The initial snapshot listener is not removed
 *  - User notification preferences are not checked yet.
 */

/**
 * Implements NotificationRepository using Firebase Firestore.
 *
 * @param db Firestore database used to access notification documents.
 */
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

    /**
     * Saves a new notification to Firestore.
     *
     * Firestore generates a unique document ID for the notification.
     *
     * @param notification Notification data to save.
     */
    override fun send(notification: Notification) {
        val doc = notificationsRef.document()
        doc.set(notification.copy(id = doc.id))
    }

    /**
     * Marks the specified notification as read in Firestore.
     *
     * @param notificationId ID of the notification to update.
     */
    override fun markRead(notificationId: String) {
        notificationsRef.document(notificationId).update("read", true)
    }

    /**
     * Retrieves notifications sent to a specific user, newest first.
     *
     * @param userId of the notification recipient.
     * @return List of notifications belonging to one user.
     */
    override fun forUser(userId: String): List<Notification> =
        _notifications
            .filter { it.recipientId == userId }        // only notifications sent to this user
            .sortedByDescending { it.timestamp }        // newest at the top


    /**
     *
     * Retrieves notifications sent by a specific user, newest first.
     *
     * @param senderId ID of the notification sender.
     * @return List of notifications sent by the user.
     */
    override fun sentBy(senderId: String): List<Notification> =
        _notifications
            .filter { it.senderId == senderId }         // only notifications this person sent
            .sortedByDescending { it.timestamp }        // newest at the top

    /**
     *
     * Listens for notification changes belonging to a specific user/
     *
     * Calls the appropriate callback when a notification is added,
     * modified, or removed.
     *
     * @param userId ID of the notification recipient
     * @param onAdded Callback for newly added notifications
     * @param onModified Callback for modified notifications
     * @param onRemoved Callback for removed notifications
     * @return A function that removes the Firestore listener when called
     */
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
