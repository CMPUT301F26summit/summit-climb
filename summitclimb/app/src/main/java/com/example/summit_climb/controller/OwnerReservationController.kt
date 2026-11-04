package com.example.summit_climb.controller

import com.example.summit_climb.data.ReservationRepository
import com.example.summit_climb.model.Reservation
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType

class OwnerReservationController(
    private val repository: ReservationRepository,
    private val notificationRepository: NotificationRepository
){
    fun getBookedDates(
        campsiteId: String,
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getBookedDates(campsiteId, onSuccess, onFailure)
    }
    fun getReservationsByCampsite(
        campsiteId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.getReservationsByCampsite(campsiteId, onSuccess, onFailure)
    }

    fun rejectReservation(
        reservationId: String,
        entrantId: String,
        campsiteId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.rejectReservation(
            reservationId = reservationId,
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
            onFailure = onFailure
        )
    }
}
