package com.example.summit_climb

import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult

/**
 * Handles what an entrant does with lotteries: join, leave, see results and decline a win.
 * Screens call this class instead of [LotteryRepository] directly.
 * Joining and leaving wait for Firestore, so they report back through an `onDone` callback
 * instead of returning a value.
 *
 * @param lotteryRepository: where lotteries and waitlist entries are stored.
 * @param currentUserId: [User.userId] of the entrant using the app.
 * @constructor Creates a controller for one entrant.
 */
class EntrantLotteryController(
    private val lotteryRepository: LotteryRepository,
    private val currentUserId: String
) {

    /**
     * Gets one lottery.
     * @param lotteryId: ID of the lottery.
     * @return the lottery, or `null` if it does not exist.
     */
    fun getLottery(lotteryId: String): Lottery? {
        return lotteryRepository.get(lotteryId)
    }

    /**
     * Adds current user to an active lottery's waitlist.
     * Rejects join if the lottery is closed, full, or already joined.
     *
     * @param lottery: lottery to join.
     * @param myEntries: user's current waitlist entries, from [getMyLotteries].
     * @param onDone: called with `null` if the user joined, or the reason the join failed.
     */
    fun joinLottery(
        lottery: Lottery,
        myEntries: List<WaitlistEntry>,
        onDone: (String?) -> Unit
    ) {
        // check rules
        val alreadyJoined = hasJoined(lottery, myEntries)
        val rejectionReason = getJoinRejectionReason(lottery, alreadyJoined)
        if (rejectionReason != null) {
            onDone(rejectionReason)
            return
        }

        // ask Firestore to add the user. The repository creates WaitlistEntry.
        lotteryRepository.join(
            lotteryId = lottery.id,
            entrantId = currentUserId,
            onSuccess = { onDone(null) },
            onFailure = { onDone(MESSAGE_JOIN_FAILED) }
        )
    }

    /**
     * Removes current user from a lottery's waitlist, only while registration is open.
     * @param lottery: lottery to leave.
     * @param myEntry: user's entry in that lottery.
     * @param onDone: called with `null` if the user left, [MESSAGE_CANNOT_LEAVE] if registration
     * is closed, or [MESSAGE_LEAVE_FAILED] if Firestore could not remove them.
     */
    fun leaveLottery(
        lottery: Lottery,
        myEntry: WaitlistEntry,
        onDone: (String?) -> Unit
    ) {
        if (!myEntry.removable(lottery)) {
            onDone(MESSAGE_CANNOT_LEAVE)
            return
        }

        lotteryRepository.leave(
            lotteryId = lottery.id,
            entrantId = currentUserId,
            onSuccess = { onDone(null) },
            onFailure = { onDone(MESSAGE_LEAVE_FAILED) }
        )
    }

    /**
     * Gets lotteries the user joined, with each one's result.
     * Each [WaitlistEntry] has the [WaitlistEntry.lotteryId] and the [WaitlistEntry.result]
     * (WAITING, WON, LOST, DECLINED or RESERVED).
     * @param onResult: called with the user's waitlist entries. Empty if they joined nothing.
     */
    fun getMyLotteries(onResult: (List<WaitlistEntry>) -> Unit) {
        lotteryRepository.entriesFor(currentUserId, onResult)
    }

    /**
     * Gets lotteries the user won that are still in their booking period.
     * @param myEntries the user's waitlist entries, from [getMyLotteries].
     * @return the lotteries the user can still reserve or decline.
     */
    fun getWonLotteriesInBookingPeriod(myEntries: List<WaitlistEntry>): List<Lottery> {
        val lotteries = myEntries.mapNotNull { entry -> getLottery(entry.lotteryId) }
        return filterWonLotteriesInBookingPeriod(myEntries, lotteries)
    }

    /**
     * Declines a won lottery for user, setting their entry to [WaitlistResult.DECLINED].
     * @param lottery: lottery the user won.
     * @param myEntry: user's entry in that lottery.
     * @return `null` if declined or [MESSAGE_CANNOT_DECLINE] if the entry is not WON
     * or the booking period is over.
     */
    fun declineWonLottery(lottery: Lottery, myEntry: WaitlistEntry): String? {
        if (!myEntry.action(lottery)) {
            return MESSAGE_CANNOT_DECLINE
        }
        lotteryRepository.setResult(lottery.id, currentUserId, WaitlistResult.DECLINED)
        return null
    }

    companion object {
        //The user is already on this lottery's waitlist.
        const val MESSAGE_ALREADY_JOINED = "You have already joined this lottery."

        //The registration period has not started or has ended.
        const val MESSAGE_CLOSED = "Registration for this lottery is closed."

        //The waitlist has reached its cap.
        const val MESSAGE_FULL = "This lottery's waitlist is full."

        // The user tried to leave after registration closed.
        const val MESSAGE_CANNOT_LEAVE = "You can only leave while registration is open."

        // The user tried to decline an entry that is not WON or outside the booking period.
        const val MESSAGE_CANNOT_DECLINE = "You can only decline a win during the booking period."

        // Firestore could not add the user
        const val MESSAGE_JOIN_FAILED = "Couldn't join the lottery. Please try again."

        // Firestore could not remove the user.
        const val MESSAGE_LEAVE_FAILED = "Couldn't leave the lottery. Please try again."

        /**
         * Checks whether user already has an entry for this lottery.
         * @param lottery the lottery to look for.
         * @param myEntries the user's waitlist entries.
         * @return `true` if one of [myEntries] belongs to [lottery].
         */
        fun hasJoined(lottery: Lottery, myEntries: List<WaitlistEntry>): Boolean {
            return myEntries.any { entry -> entry.lotteryId == lottery.id }
        }

        /**
         * Checks whether a lottery's waitlist has reached its cap
         *
         * @param lottery: lottery to check.
         * @return `true` if no more entrants can join.
         */
        fun isWaitlistFull(lottery: Lottery): Boolean {
            val cap = lottery.waitlistCap
            if (cap == null) {
                return false
            }
            return lottery.entrantCount >= cap
        }

        /**
         * Finds reason the user cannot join a lottery, if there is one.
         *
         * @param lottery: lottery the user wants to join.
         * @param alreadyJoined: `true` if the user is already on this lottery's waitlist.
         * @param now: the current time in milliseconds.
         * @return the reason to show the user or `null` if they can join.
         */
        fun getJoinRejectionReason(
            lottery: Lottery,
            alreadyJoined: Boolean,
            now: Long = System.currentTimeMillis()
        ): String? {
            if (alreadyJoined) {
                return MESSAGE_ALREADY_JOINED
            }

            val isOpenStage = lottery.status == LotteryStatus.OPEN
            val isInRegistrationPeriod = lottery.isRegistrationOpen(now)
            if (!isOpenStage || !isInRegistrationPeriod) {
                return MESSAGE_CLOSED
            }

            if (isWaitlistFull(lottery)) {
                return MESSAGE_FULL
            }
            return null
        }

        /**
         * Keeps the lotteries where user's entry is WON and booking period has not ended.
         * @param myEntries: user's waitlist entries.
         * @param lotteries: lotteries to check.
         * @param now: current time in milliseconds.
         * @return lotteries user can still reserve or decline.
         */
        fun filterWonLotteriesInBookingPeriod(
            myEntries: List<WaitlistEntry>,
            lotteries: List<Lottery>,
            now: Long = System.currentTimeMillis()
        ): List<Lottery> {
            return lotteries.filter { lottery ->
                val myEntry = myEntries.find { entry -> entry.lotteryId == lottery.id }
                myEntry != null && myEntry.action(lottery, now)
            }
        }
    }
}