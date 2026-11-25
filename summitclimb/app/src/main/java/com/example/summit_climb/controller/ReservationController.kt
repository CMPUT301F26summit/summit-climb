package com.example.summit_climb.controller

import com.example.summit_climb.data.ReservationRepository
import com.example.summit_climb.model.Reservation

class ReservationController(
    private val repository: ReservationRepository
) {

    /** Retrieves booked dates for a campsite */

    fun getBookedDates(
        campsiteId: String,
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getBookedDates(campsiteId, onSuccess, onFailure)
    }

    fun getReservationsByEntrant(
        entrantId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getReservationsByEntrant(
            entrantId,
            onSuccess,
            onFailure)
    }

    // Companion object to make the function static
    // and accessible without creating an instance of the class
    companion object{
        fun datesOverlap(
            start1: String,
            end1: String,
            start2: String,
            end2: String
        ): Boolean {
            return start1 < end2 && start2 < end1 // If start1 is before end2 and start2 is before end1,
                                                 // they overlap
        }
    }

    fun createReservation(
        campsiteId: String,
        entrantId: String,
        entrantName: String,
        startDate: String,
        endDate: String,
        guests: Int,
        totalPrice: Double?,
        rulesAccepted: Boolean?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.createReservation(
            campsiteId,
            entrantId,
            entrantName,
            startDate,
            endDate,
            guests,
            totalPrice,
            rulesAccepted,
            onSuccess,
            onFailure
        )
    }

    fun cancelReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.cancelReservation(reservationId, onSuccess, onFailure) // Calls the repository's cancelReservation function
    }
}