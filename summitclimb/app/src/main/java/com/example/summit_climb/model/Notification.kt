package com.example.summit_climb.model

// Purpose: the data for one notification, saved in the Firestore "notifications."

/**
 * One notification sent to one user.
 *
 *  @property id ==> the unique id of this notification
 *  @property senderId ==> who sent it
 *  @property recipientId ==> who receives it
 *  @property type ==> what kind of notification is it
 *  @property text ==> the message the user sees
 *  @property campsiteId ==> the campsite it is about (empty if none)
 *  @property relatedId ==> the lottery or reservation it is about (empty if none)
 *  @property batchId ==> the same for every notification sent together, so an owner
 *  can see how many entrants one message received (empty if sent alone)
 *  @property timestamp ==> when it was made
 *  @property read ==> True if the user has already opened it
 *  @constructor ==> creates a notification. with no arguments, creates an empty unread message
 */
data class Notification(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val text: String = "",
    val campsiteId: String  = "",
    val relatedId: String  = "",
    val batchId: String  = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)



/** The different kinds of notification. Lets Admin filter them by kind for later
 * // Types:
 * //    - LOTTERY_WIN = "You won the lottery!"
 * //    - LOTTERY_LOSS = "You lost the lottery."
 * //    - RESERVATION_UPDATE: "Reservation for {campsite} is confirmed."
 * //    - MESSAGE: "{Campsite_owner} sent a message."
 * //  It is saved in the Firestore "notifications" collection.
 * */
enum class NotificationType {
    LOTTERY_WIN,            // the user won the lottery
    LOTTERY_LOSS,           // the user lost the lottery
    RESERVATION_UPDATE,     // a reservation was confirmed, cancelled, or rejected
    MESSAGE                 // a message from a campsite owner or to an entrant
}
