package com.example.summit_climb.model

/**
 * Represents a campsite reservation made by an entrant.
 *
 * Stores the reservation details, including the campsite,
 * entrant, booking dates, number of guests, and reservation status.
 *
 * Reservation statuses include ACTIVE, COMPLETED, CANCELLED, and REJECTED.
 *
 * Dates are stored as strings in yyyy-MM-dd format.
 */

data class Reservation(
    val id: String = "",
    val campsiteId: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val guests: Int = 0,
    val status: String = "ACTIVE"
)

