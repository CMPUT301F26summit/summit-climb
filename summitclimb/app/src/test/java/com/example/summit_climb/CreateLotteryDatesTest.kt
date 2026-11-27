package com.example.summit_climb

import com.example.summit_climb.ui.owner.dayOfEnd
import com.example.summit_climb.ui.owner.dayOfStart
import com.example.summit_climb.ui.owner.endOfDay
import com.example.summit_climb.ui.owner.startOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// tests for how the List in Lottery screen turns picked days into lottery times (F7)
class CreateLotteryDatesTest {

    private val day = LocalDate.of(2026, 12, 10)

    @Test
    fun startOfDayGoesBackToTheSameDay() {
        assertEquals(day, dayOfStart(startOfDay(day)))
    }

    @Test
    fun endOfDayGoesBackToTheSameDay() {
        assertEquals(day, dayOfEnd(endOfDay(day)))
    }

    @Test
    fun endOfDayIncludesTheWholeDay() {
        // a whole day is 24 hours (Dec 10 has no clock change)
        assertEquals(24L * 60 * 60 * 1000, endOfDay(day) - startOfDay(day))
    }

    @Test
    fun bookingTheDayAfterRegistrationClosesIsAllowed() {
        // validate() needs bookingStart >= registrationEnd
        assertTrue(startOfDay(day.plusDays(1)) >= endOfDay(day))
    }

    @Test
    fun bookingTheSameDayRegistrationClosesIsNotAllowed() {
        assertTrue(startOfDay(day) < endOfDay(day))
    }
}