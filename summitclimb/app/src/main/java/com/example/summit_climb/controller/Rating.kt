package com.example.summit_climb.controller

/**
 * One entrant's star rating and written review of one campsite
 * for a specific reservation.
 *
 * @property reservationId ID of the reservation being rated.
 * @property entrantId ID of the entrant submitting the rating.
 * @property campsiteId ID of the campsite being rated.
 * @property stars star value, a whole number from 1 to 5.
 * @property review written review of the campsite.
 *
 */
data class Rating(
    var reservationId: String = "",
    var entrantId: String = "",
    var campsiteId: String = "",
    var stars: Int = 0,
    var review: String = ""
) {
    /**
     * Checks that the star value is a whole number from 1 to 5.
     * @return `true` if [stars] is between 1 and 5.
     */
    fun validStars(): Boolean{
        return stars in 1..5
    }

    /**
     * Checks that the rating has the required information
     *
     * @return true if the reservation and campsite IDs are not blank
     * and the star value is valid
     */
    fun isValid(): Boolean {
        return reservationId.isNotBlank() &&
                entrantId.isNotBlank() &&
                campsiteId.isNotBlank() &&
                validStars()
    }
}