package com.example.summit_climb

import org.junit.Test
import org.junit.Assert.assertTrue
import com.example.summit_climb.controller.ReservationController
import org.junit.Assert.assertFalse

class ReservationControllerTest {

    @Test

    fun overlappingDatesReturnTrue() {

        assertTrue(
            ReservationController.datesOverlap(
                "2023-06-01",
                "2023-06-05",
                "2023-06-03",
                "2023-06-07"
            )
        )
    }

    @Test

    fun nonOverlappingDatesReturnFalse() {

        assertFalse(
            ReservationController.datesOverlap(
                "2023-06-01",
                "2023-06-05",
                "2023-06-9",
                "2023-06-11"
            )
        )
    }

    @Test
    fun checkoutAndCheckinOnSameDayDoNotOverlap() {
        assertFalse(
            ReservationController.datesOverlap(
                "2026-11-10",
                "2026-11-12",
                "2026-11-12",
                "2026-11-14"
            )
        )
    }
}