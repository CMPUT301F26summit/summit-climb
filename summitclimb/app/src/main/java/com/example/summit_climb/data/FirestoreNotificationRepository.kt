
package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firestore implementation of [NotificationRepository].
 *
 * Stores notifications in the Firestore "notifications" collection
 * and listens for changes to notification data.
 *
 * Notifications can be retrieved by recipient or sender.
 *
 * Known Issues:
 * - The initial snapshot listener loads all notifications into the local list.
 * - The initial snapshot listener is not removed automatically.
 * - User notification preferences are not checked yet.
 *
 * @param db Firestore database used to access notification documents.
 */
class FirestoreNotificationRepository(
    db: FirebaseFirestore
) : NotificationRepository {

    /** Reference to the notifications collection in Firestore. */
    private val notificationsRef = db.collection("notifications")

    /** Local observable list containing the notifications retrieved from Firestore. */
    private val _notifications = mutableStateListOf<Notification>()

    /**
     * Provides access to the locally stored notifications.
     *
     * @return A list of notifications currently loaded from Firestore.
     */
    override val notifications: List<Notification>
        get() = _notifications

    /**
     * Starts listening for changes to the notifications collection
     * when the repository is created.
     *
     * Updates the local notification list whenever Firestore changes.
     */
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
     * Saves a new notification to the Firestore notifications collection.
     *
     * Firestore generates a unique document ID for the notification.
     *
     * @param notification The notification to save.
     */
    override fun send(notification: Notification) {
        val doc = notificationsRef.document()
        doc.set(notification.copy(id = doc.id))
    }

    /**
     * Marks a notification as read in Firestore.
     *
     * @param notificationId The ID of the notification to update.
     */
    override fun markRead(notificationId: String) {
        notificationsRef.document(notificationId).update("read", true)
    }

    /**
     * Retrieves notifications addressed to a specific user, newest first.
     *
     * @param userId The ID of the recipient.
     * @return A list of notifications addressed to the specified user.
     */
    override fun forUser(userId: String): List<Notification> =
        _notifications
            .filter { it.recipientId == userId }
            .sortedByDescending { it.timestamp }

    /**
     * Retrieves notifications sent by a specific user, newest first.
     *
     * @param senderId The ID of the sender.
     * @return A list of notifications sent by the specified user.
     */
    override fun sentBy(senderId: String): List<Notification> =
        _notifications
            .filter { it.senderId == senderId }
            .sortedByDescending { it.timestamp }

    /**
     * Listens for real-time changes to a specific user's notifications.
     *
     * Reports newly added, modified, and removed notifications through
     * the corresponding callbacks.
     *
     * @param userId The ID of the user whose notifications are monitored.
     * @param onAdded Callback invoked when a notification is added.
     * @param onModified Callback invoked when a notification is modified.
     * @param onRemoved Callback invoked when a notification is removed.
     * @return A function that removes the Firestore listener when invoked.
     */
    override fun listenToUser(
        userId: String,
        onAdded: (Notification) -> Unit,
        onModified: (Notification) -> Unit,
        onRemoved: (Notification) -> Unit
    ): () -> Unit {
        val registration = notificationsRef
            .whereEqualTo("recipientId", userId)
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
