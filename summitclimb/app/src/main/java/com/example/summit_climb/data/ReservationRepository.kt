package com.example.summit_climb.data

import com.example.summit_climb.model.Reservation

interface ReservationRepository {

    fun getBookedDates(
        campsiteId: String,
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getReservationsByCampsite(
        campsiteId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun rejectReservation(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )


}