package com.example.summit_climb

/**
 * A entrant's place on one lottery's waitlist.
 * Stored by LotteryRepository in the `entries` subcollection of the lottery,
 * at `lotteries/{lotteryId}/entries/{entrantId}`.
 *
 * Example:
 * ```
 * val entry = WaitlistEntry(
 *     lotteryId = "lottery1",
 *     entrantId = "device123",
 *     joinedAt = System.currentTimeMillis()
 * )
 * entry.result // WAITING
 * ```
 *
 * @property lotteryId: ID of the Lottery this entry belongs to
 * @property entrantId: User.userId of the entrant who joined
 * @property joinedAt: when the entrant joined
 * @property result: entry's result. Starts as WaitlistResult.WAITING
 * @constructor Creates a waitlist entry. With no arguments, creates an empty WAITING entry.
 */
class WaitlistEntry(
    var lotteryId: String = "",
    var entrantId: String = "",
    var joinedAt: Long = 0L,
    var result: WaitlistResult = WaitlistResult.WAITING
) {
    /**
     * Checks whether this entry can be removed, which is only while registration is open.
     * 
     * @param lottery: lottery this entry belongs to.
     * @param now: current time in milliseconds.
     * @return `true` if the lottery's registration is open at now.
     */
    fun removable(lottery: Lottery, now: Long = System.currentTimeMillis()): Boolean{
        return lottery.isRegisterationOpen(now)
    }

    /**
     * Checks whether this entry can be declined or reserved
     * Only WaitlistResult.WON entry can and only during the lottery's booking period.
     *
     * @param lottery: lottery this entry belongs to.
     * @param now: current time in milliseconds.
     * @return `true` if result is WON and now is inside the booking period.
     */
    fun action(lottery: Lottery, now: Long = System.currentTimeMillis()): Boolean{
        val won = (result == WaitlistResult.WON)
        val booking_open = lottery.isInBookingPeriod(now)
        return won && booking_open
    }
}

/**
 * Possible results of a WaitlistEntry.
 * The normal order is WAITING → WON or LOST.
 * A winner then goes to RESERVED or [DECLINED].
 */
enum class WaitlistResult{
    WAITING, WON, LOST, DECLINED, RESERVED
}