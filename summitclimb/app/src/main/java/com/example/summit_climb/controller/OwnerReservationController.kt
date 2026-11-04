package com.example.summit_climb.controller

import com.example.summit_climb.data.ReservationRepository
import com.example.summit_climb.model.Reservation

class OwnerReservationController(
    private val repository: ReservationRepository
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
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.rejectReservation(reservationId, onSuccess, onFailure)
    }
}
