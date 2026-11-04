package com.example.summit_climb.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LotteryTest {

    // registration 1000–2000, booking 3000–4000
    private val lottery = Lottery(
        registrationStart = 1000L,
        registrationEnd = 2000L,
        bookingStart = 3000L,
        bookingEnd = 4000L
    )

    @Test
    fun newLotteryHasEmptyDefaults() {
        val empty = Lottery()
        assertEquals("", empty.id)
        assertTrue(empty.campsiteIds.isEmpty())
        assertEquals(0, empty.entrantCount)
        assertNull(empty.waitlistCap)
        assertEquals(LotteryStatus.OPEN, empty.status)
    }

    @Test
    fun registrationOpenDuringPeriod() {
        assertTrue(lottery.isRegistrationOpen(1500L))
    }

    @Test
    fun registrationOpenAtExactStart() {
        assertTrue(lottery.isRegistrationOpen(1000L))
    }

    @Test
    fun registrationClosedAtExactEnd() {
        assertFalse(lottery.isRegistrationOpen(2000L))
    }

    @Test
    fun registrationClosedBeforeStart() {
        assertFalse(lottery.isRegistrationOpen(999L))
    }

    @Test
    fun bookingOpenDuringPeriod() {
        assertTrue(lottery.isInBookingPeriod(3500L))
    }

    @Test
    fun bookingClosedAtExactEnd() {
        assertFalse(lottery.isInBookingPeriod(4000L))
    }

    @Test
    fun betweenPeriodsNeitherIsOpen() {
        assertFalse(lottery.isRegistrationOpen(2500L))
        assertFalse(lottery.isInBookingPeriod(2500L))
    }
}