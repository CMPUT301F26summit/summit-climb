package com.example.summit_climb.model

// Purpose: the data for one lottery, saved in the Firestore "lotteries" collection.
// An owner creates a lottery for one or more of their campsites. Entrants join
// during the registration period, winners are drawn when it ends, and winners
// can reserve during the booking period.
// The entrants themselves are stored as WaitlistEntry documents, not in this class.


/** The stages a lottery goes through. */
enum class LotteryStatus {
    OPEN,       // registration period: entrants can join or leave
    DRAWN,      // registration ended and winners have been picked
    CLOSED      // booking period is over
}

/**
 * One lottery for one or more campsites.
 *
 *  @property id -> the unique id of this lottery
 *  @property ownerId -> the campsite owner who created it
 *  @property campsiteIds -> the campsites included in this lottery
 *  @property registrationStart -> when entrants can start joining
 *  @property registrationEnd -> when joining closes and the draw happens
 *  @property bookingStart -> when winners can start reserving
 *  @property bookingEnd -> when winners can no longer reserve
 *  @property winnerCount -> how many entrants get picked
 *  @property waitlistCap -> max entrants allowed to join; null means no limit
 *  @property status -> which stage the lottery is in
 */
data class Lottery(
    val id: String = "",
    val ownerId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStart: Long = 0L,
    val registrationEnd: Long = 0L,
    val bookingStart: Long = 0L,
    val bookingEnd: Long = 0L,
    val winnerCount: Int = 0,
    val waitlistCap: Int? = null,
    val status: LotteryStatus = LotteryStatus.OPEN
)