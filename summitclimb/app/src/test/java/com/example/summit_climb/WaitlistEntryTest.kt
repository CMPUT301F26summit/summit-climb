package com.example.summit_climb

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [com.example.summit_climb.model.WaitlistEntry]: when it can be removed, declined or reserved.
 *
 * Times are fixed numbers so the tests give the same result every day.
 * The lottery's timeline is:
 * ```
 * day 0 ---- registration ---- day 10 ---- (draw) ---- day 12 ---- booking ---- day 20
 * ```
 */
class WaitlistEntryTest {

    private lateinit var lottery: Lottery
    private lateinit var entry: WaitlistEntry

    @Before
    fun setUp() {
        lottery = Lottery(
            id = "lottery1",
            registrationStart = day(0),
            registrationEnd = day(10),
            bookingStart = day(12),
            bookingEnd = day(20)
        )
        entry = WaitlistEntry(lotteryId = "lottery1", entrantId = "user1", joinedAt = day(1))
    }

    @Test
    fun newEntry_startsAsWaiting() {
        assertEquals(WaitlistResult.WAITING, entry.result)
    }

    @Test
    fun canBeRemoved_registrationOpen_returnsTrue() {
        assertTrue(entry.removable(lottery, now = day(5)))
    }

    @Test
    fun canBeRemoved_registrationClosed_returnsFalse() {
        assertFalse(entry.removable(lottery, now = day(11)))
    }

    @Test
    fun canDeclineOrReserve_wonDuringBookingPeriod_returnsTrue() {
        entry.result = WaitlistResult.WON

        assertTrue(entry.action(lottery, now = day(15)))
    }

    @Test
    fun canDeclineOrReserve_wonButBookingPeriodOver_returnsFalse() {
        entry.result = WaitlistResult.WON

        assertFalse(entry.action(lottery, now = day(21)))
    }

    @Test
    fun canDeclineOrReserve_wonButBookingNotStarted_returnsFalse() {
        entry.result = WaitlistResult.WON

        assertFalse(entry.action(lottery, now = day(11)))
    }

    @Test
    fun canDeclineOrReserve_lostDuringBookingPeriod_returnsFalse() {
        entry.result = WaitlistResult.LOST

        assertFalse(entry.action(lottery, now = day(15)))
    }

    /** Turns a day number into milliseconds, counting from a fixed starting time. */
    private fun day(number: Int): Long {
        val oneDay = 24L * 60 * 60 * 1000
        return START_TIME + number * oneDay
    }

    private companion object {
        /** A fixed time (Jan 15, 2027) so tests do not depend on today's date. */
        const val START_TIME = 1_800_000_000_000L
    }
}
