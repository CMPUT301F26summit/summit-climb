package com.example.summit_climb.model

/**
 * Admin use: displays a row of profiles in the app.
 *
 * @param id: user's id.
 * @param name: user's profile name.
 * @param email: user's email address.
 * @param phoneNumber: user's phone number.
 * @param profileImageUrl: user's profile image download URL.
 * @param activeProfile: active accounts are true, deactivated accounts are false.
 * @param reservations: reservations the user has made.
 * @param listings: campsites the user has listed.
 * @param flagged: admin flagging users who break guidelines as true.
 */
data class AdminProfileView(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val activeProfile: Boolean,
    val reservations: Int,
    val listings: Int,
    val flagged: Boolean = false
)
