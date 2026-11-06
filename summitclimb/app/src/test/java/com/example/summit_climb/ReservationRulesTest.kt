package com.example.summit_climb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// Unit tests for the reservation screen's date rules.
class ReservationRulesTest {

    private fun day(d: Int): LocalDate = LocalDate.of(2026, 11, d)

    // booking period Nov 10 – Nov 30, Nov 15 already booked
    private val info = ReservationUiInfo(
        lotteryId = "1",
        campsiteName = "Cedar Valley",
        location = "Banff, AB",
        bookingStart = day(10),
        bookingEnd = day(30),
        bookedDates = setOf(day(15)),
        reserved = false
    )

    // nightsBetween
    @Test
    fun countsNights() {
        assertEquals(2L, nightsBetween(day(14), day(16)))
    }

    //isInBookingPeriod
    @Test
    fun stayInsidePeriodIsOk() {
        assertTrue(isInBookingPeriod(day(12), day(14), day(10), day(30)))
    }

    @Test
    fun checkInBeforePeriodIsNotOk() {
        assertFalse(isInBookingPeriod(day(9), day(12), day(10), day(30)))
    }

    @Test
    fun checkOutAfterPeriodIsNotOk() {
        assertFalse(isInBookingPeriod(LocalDate.of(2026, 11, 29), LocalDate.of(2026, 12, 1), day(10), day(30)))
    }

    @Test
    fun checkOutOnLastDayIsOk() {
        assertTrue(isInBookingPeriod(day(28), day(30), day(10), day(30)))
    }

    // overlapsBooked

    @Test
    fun bookedNightInsideStayOverlaps() {
        assertTrue(overlapsBooked(day(14), day(16), setOf(day(15))))
    }

    @Test
    fun checkingOutOnBookedDayDoesNotOverlap() {
        // leaving the morning of the 15th doesn't need the night of the 15th
        assertFalse(overlapsBooked(day(13), day(15), setOf(day(15))))
    }

    @Test
    fun noBookedNightsNoOverlap() {
        assertFalse(overlapsBooked(day(12), day(14), emptySet()))
    }

    // datesProblem

    @Test
    fun nothingPickedAsksForCheckIn() {
        assertEquals(ReservationText.PICK_CHECK_IN, datesProblem(null, null, info))
    }

    @Test
    fun onlyCheckInAsksForCheckOut() {
        assertEquals(ReservationText.PICK_CHECK_OUT, datesProblem(day(12), null, info))
    }

    @Test
    fun datesOutsidePeriodAreRejected() {
        assertTrue(datesProblem(day(5), day(7), info)!!.startsWith("Please pick dates between"))
    }

    @Test
    fun bookedNightIsRejected() {
        assertEquals(ReservationText.ALREADY_BOOKED, datesProblem(day(14), day(16), info))
    }

    @Test
    fun goodDatesHaveNoProblem() {
        assertNull(datesProblem(day(11), day(13), info))
    }

    // nextSelection

    @Test
    fun firstTapSetsCheckIn() {
        assertEquals(Pair(day(12), null), nextSelection(null, null, day(12)))
    }

    @Test
    fun laterTapSetsCheckOut() {
        assertEquals(Pair(day(12), day(14)), nextSelection(day(12), null, day(14)))
    }

    @Test
    fun earlierTapMovesCheckIn() {
        assertEquals(Pair(day(10), null), nextSelection(day(12), null, day(10)))
    }

    @Test
    fun tapAfterBothPickedStartsOver() {
        assertEquals(Pair(day(20), null), nextSelection(day(12), day(14), day(20)))
    }
}