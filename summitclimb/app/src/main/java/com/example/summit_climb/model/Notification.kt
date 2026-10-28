package com.example.summit_climb.model

// Purpose: the data for one notification.
// Types:
//    - Lottery_win = "You won the lottery!"
//    - Lottery_loss = "You lost the lottery."
//    - Reservation_update: "Reservation for {campsite} is confirmed."
//    - Message: "{Campsite_owner} sent a message."
//  It is saved in the Firestore "notifications" collection.
//  Known Problems: None yet.

/** The different kinds of notification. Lets Admin filter them by kind. */
enum class NotificationType {
    LOTTERY_WIN,            // the user won the lottery
    LOTTERY_LOSS,           // the user lost the lottery
    RESERVATION_UPDATE,     // a reservation was confirmed, cancelled, or rejected
    MESSAGE                 // a message from a campsite owner or to an entrant
}

/**
 * One notification sent to one user.
 *
 *  @property id ==> the unique id of this notification
 *  @property senderId ==> who sent it
 *  @property recipientId ==> who receives it
 *  @property type ==> what kind of notification is it
 *  @property text ==> the message the user sees
 *  @property timestamp ==> when it was made
 *  @property read ==> True if the user has already opened it
 */
data class Notification(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
