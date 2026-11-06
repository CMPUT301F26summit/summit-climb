package com.example.summit_climb.model

/**
 * Represents the notification data stored in the Firestore
 * "notifications collection.
 *
 * Notification types include lottery results, reservation updates,
 * and messages between users.
 *
 * Known Issues:
 * - The timestamp uses the phone's local clock
 * - Tapping a notification may not navigate to its
 * related screen if the destination is not available
 */


/**
 * One notification sent to one user.
 *
 *  @property id the unique id of this notification
 *  @property senderId the User.userId of who sent it ("system" for automatic ones)
 *  @property recipientId the User.userId of who receives it
 *  @property type what kind of notification it is
 *  @property title the short heading the user sees
 *  @property text the message the user sees
 *  @property campsiteId the campsite it is about (empty if none)
 *  @property relatedId the lottery or reservation it is about (empty if none)
 *  @property batchId the same for every notification sent together, so an owner
 *  can see how many entrants one message reached (empty if sent alone)
 *  @property timestamp when it was made
 *  @property read true if the user has already opened it
 */
data class Notification(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val title: String = "",
    val text: String = "",
    val campsiteId: String = "",
    val relatedId: String = "",
    val batchId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

/** The different kinds of notifications supported by the app.  */
enum class NotificationType {
    LOTTERY_WIN,            // the user won the lottery
    LOTTERY_LOSS,           // the user lost the lottery
    RESERVATION_UPDATE,     // a reservation was confirmed, cancelled, or rejected
    MESSAGE                 // a message from a campsite owner or to an entrant
}