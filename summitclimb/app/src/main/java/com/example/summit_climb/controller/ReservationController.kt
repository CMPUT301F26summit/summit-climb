package com.example.summit_climb.controller

import com.example.summit_climb.data.ReservationRepository

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

    /** Checks if a campsite is booked for a given date range
     * Makes sure dates don't overlap
     */

    companion object{
        fun datesOverlap(
            start1: String,
            end1: String,
            start2: String,
            end2: String
        ): Boolean {
            return start1 < end2 && start2 < end1
        }
    }

    fun createReservation(
        campsiteId: String,
        entrantId: String,
        entrantName: String,
        startDate: String,
        endDate: String,
        guests: Int,
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
            onSuccess,
            onFailure
        )
    }

    fun cancelReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.cancelReservation(reservationId, onSuccess, onFailure)
    }
}