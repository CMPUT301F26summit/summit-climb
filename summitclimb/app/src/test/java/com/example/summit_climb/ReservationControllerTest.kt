package com.example.summit_climb

import org.junit.Test
import org.junit.Assert.assertTrue
import com.example.summit_climb.controller.ReservationController
import org.junit.Assert.assertFalse
import com.example.summit_climb.model.Reservation
import java.lang.Exception
import org.junit.Assert.assertEquals

class ReservationControllerTest {

    private class FakeRepository : ReservationRepository {

        var receivedCampsiteId = ""
        var receivedEntrantId = ""
        var receivedEntrantName = ""
        var receivedStartDate = ""
        var receivedEndDate = ""
        var receivedGuests = 0
        var receivedReservationId = ""

        override fun getBookedDates(
            campsiteId: String,
            onSuccess: (List<Pair<String, String>>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(emptyList())
        }

        override fun getReservationsByCampsite(
            campsiteId: String,
            onSuccess: (List<Reservation>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(emptyList())
        }

        override fun rejectReservation(
            reservationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess()
        }

        override fun createReservation(
            campsiteId: String,
            entrantId: String,
            entrantName: String,
            startDate: String,
            endDate: String,
            guests: Int,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            receivedCampsiteId = campsiteId
            receivedEntrantId = entrantId
            receivedEntrantName = entrantName
            receivedStartDate = startDate
            receivedEndDate = endDate
            receivedGuests = guests

            onSuccess()
        }

        override fun cancelReservation(
            reservationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            receivedReservationId = reservationId
            onSuccess()
        }
    }

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
                "2023-06-09",
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

    @Test
    fun correctDetails() {

        val fakeRepository = FakeRepository()
        val controller = ReservationController(fakeRepository)

        controller.createReservation(
            "campsite123",
            "entrant456",
            "John Doe",
            "2023-06-01",
            "2023-06-05",
            4,
            onSuccess = {},
            onFailure = {}
        )

        assertEquals("campsite123", fakeRepository.receivedCampsiteId)
        assertEquals("entrant456", fakeRepository.receivedEntrantId)
        assertEquals("John Doe", fakeRepository.receivedEntrantName)
        assertEquals("2023-06-01", fakeRepository.receivedStartDate)
        assertEquals("2023-06-05", fakeRepository.receivedEndDate)
        assertEquals(4, fakeRepository.receivedGuests)
    }

    @Test
    fun correctReservationID() {
        val fakeRepository = FakeRepository()
        val controller = ReservationController(fakeRepository)

        controller.cancelReservation(
            "reservation789",
            onSuccess = {},
            onFailure = {}
        )

        assertEquals("reservation789", fakeRepository.receivedReservationId)
    }
}