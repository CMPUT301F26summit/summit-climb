package com.example.summit_climb.controller

/**
 * One entrant's star rating of one campsite.
 *
 * @property entrantId: [com.example.summit_climb.model.User.userId] of the entrant who rated.
 * @property campsiteId: [com.example.summit_climb.model.Campsite.id] being rated.
 * @property stars: star value, a whole number from 1 to 5.
 * @constructor Creates a rating. With no arguments, creates an empty (invalid) rating.
 */
data class Rating(
    var entrantId: String = "",
    var campsiteId: String = "",
    var stars: Int = 0
    ) {
    /**
     * Checks that the star value is a whole number from 1 to 5.
     * @return `true` if [stars] is between 1 and 5.
     */
    fun validStars(): Boolean{
        return stars in 1..5
    }
}