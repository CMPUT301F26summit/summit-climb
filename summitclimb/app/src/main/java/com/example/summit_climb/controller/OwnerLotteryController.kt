package com.example.summit_climb.controller

import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry

/**
 * Purpose: everything a campsite owner does with lotteries:
 * create and edit them (with validation), list their lotteries,
 * watch a lottery's waitlist, and get a status label for badges.
 */

class OwnerLotteryController(
    private val repository: LotteryRepository
) {

    // checks a lottery's settings, returns what's wrong, or null if it's valid
    fun validate(lottery: Lottery, now: Long = System.currentTimeMillis()): String? {
        if (lottery.campsiteIds.isEmpty()) {
            return "Pick at least one campsite"
        }
        if (lottery.registrationStart >= lottery.registrationEnd) {
            return "Registration must end after it starts"
        }
        if (lottery.registrationEnd <= now) {
            return "Registration must end in the future"
        }
        if (lottery.bookingStart < lottery.registrationEnd) {
            return "Booking can't start before registration ends"
        }
        if (lottery.bookingStart >= lottery.bookingEnd) {
            return "Booking must end after it starts"
        }
        if (lottery.winnerCount < 1) {
            return "There must be at least one winner"
        }
        val cap = lottery.waitlistCap
        if (cap != null && cap < lottery.winnerCount) {
            return "Waitlist limit can't be smaller than the number of winners"
        }
        return null
    }

    // saves a new lottery if it's valid
    fun createLottery(
        lottery: Lottery,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        now: Long = System.currentTimeMillis()
    ) {
        val problem = validate(lottery, now)
        if (problem != null) {
            onFailure(Exception(problem))
            return
        }
        repository.add(lottery)
        onSuccess()
    }

    // saves edits to a lottery, only while it's valid and registration hasn't closed
    fun editLottery(
        lottery: Lottery,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        now: Long = System.currentTimeMillis()
    ) {
        val saved = repository.get(lottery.id)
        if (saved == null) {
            onFailure(Exception("Lottery not found"))
            return
        }
        if (saved.status != LotteryStatus.OPEN || now >= saved.registrationEnd) {
            onFailure(Exception("A lottery can't be edited after registration closes"))
            return
        }
        val problem = validate(lottery, now)
        if (problem != null) {
            onFailure(Exception(problem))
            return
        }
        repository.update(lottery)
        onSuccess()
    }

    // all lotteries this owner created
    fun lotteriesFor(ownerId: String): List<Lottery> =
        repository.forOwner(ownerId)

    // live waitlist for one lottery, call the returned function to stop listening
    fun listenToWaitlist(lotteryId: String, onUpdate: (List<WaitlistEntry>) -> Unit): () -> Unit =
        repository.listenToEntries(lotteryId, onUpdate)

    // short label for a lottery badge, based on its dates
    fun statusLabel(lottery: Lottery, now: Long = System.currentTimeMillis()): String {
        if (now < lottery.registrationStart) {
            return "Upcoming"
        }
        if (lottery.isRegistrationOpen(now)) {
            return "Lottery Open"
        }
        return "Closed"
    }
}