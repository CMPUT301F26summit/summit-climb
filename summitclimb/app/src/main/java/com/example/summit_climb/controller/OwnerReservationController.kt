package com.example.summit_climb.controller

import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Reservation
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.data.ReservationRepository

// Repositories are passed in so we can use fake ones for testing

class OwnerReservationController(
    private val repository: ReservationRepository, // Private so it can't be accessed from outside
    private val notificationRepository: NotificationRepository
){

    fun getBookedDates(
        campsiteId: String,
        onSuccess: (List<Pair<String, String>>) -> Unit,  // List of start and end dates (in a pair)
        onFailure: (Exception) -> Unit // Error message if failure to get booked dates
    ) {

        // Passes both callbacks to the repository so it can return the result or error
        repository.getBookedDates(campsiteId, onSuccess, onFailure)
    }
    fun getReservationsByCampsite(
        campsiteId: String,
        onSuccess: (List<Reservation>) -> Unit, // List of reservations if success
        onFailure: (Exception) -> Unit // Error message if failure to get reservations
    ){
        repository.getReservationsByCampsite(campsiteId, onSuccess, onFailure)
    }

    fun getReservationsByEntrant(
        entrantId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getReservationsByEntrant(
            entrantId = entrantId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun rejectReservation(
        reservationId: String,
        entrantId: String,
        campsiteId: String,
        onSuccess: () -> Unit, // If the reservation is rejected successfully
        onFailure: (Exception) -> Unit // Error message if failure to reject
    ){
        repository.rejectReservation(
            reservationId = reservationId,

            // If the reservation is rejected successfully,
            // send a notification and let the screen know it worked
            onSuccess = {
                notificationRepository.send(
                    Notification(
                        senderId = "system",
                        recipientId = entrantId,
                        type = NotificationType.RESERVATION_UPDATE,
                        title = "Reservation Rejected",
                        text = "Your reservation for ${campsiteId} has been rejected.",
                        campsiteId = campsiteId,
                        relatedId = reservationId
                    )
                )
                onSuccess()
            },

            // Left expects the failure callback; right provides the one received by this controller
            onFailure = onFailure
        )
    }

    /**
     * Marks a reservation as completed and sends a rating request
     * to the entrant
     */
    fun completeReservation(
        reservationId: String,
        entrantId: String,
        campsiteId: String,
        campsiteName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.completeReservation(
            reservationId = reservationId,
            onSuccess = {
                notificationRepository.send(
                    Notification(
                        senderId = "system",
                        recipientId = entrantId,
                        type = NotificationType.RATING_REQUEST,
                        title = "Rate Your Experience?",
                        text = "Your stay at $campsiteName has ended. How was your experience?",
                        campsiteId = campsiteId,
                        relatedId = reservationId
                    )
                )
                onSuccess()
            },
            onFailure = onFailure
        )
    }
}
