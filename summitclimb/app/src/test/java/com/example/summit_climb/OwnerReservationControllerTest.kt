package com.example.summit_climb

import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.data.ReservationRepository
import com.example.summit_climb.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue

class OwnerReservationControllerTest {

    @Test
    fun getBookedDatesPassesCampsiteId() {

        var receivedId = ""

        val fakeRepository = object : ReservationRepository {

            override fun getBookedDates(
                campsiteId: String,
                onSuccess: (List<Pair<String, String>>) -> Unit,
                onFailure: (Exception) -> Unit
            ) {
                receivedId = campsiteId
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
                onSuccess()
            }

            override fun cancelReservation(
                reservationId: String,
                onSuccess: () -> Unit,
                onFailure: (Exception) -> Unit
            ) {
                onSuccess()
            }
        }

        val controller = OwnerReservationController(fakeRepository)

        controller.getBookedDates(
            "campsite123",
            onSuccess = {},
            onFailure = {}
        )

        assertEquals("campsite123", receivedId)
    }

    @Test
    fun getBookedDatesFailureCallback() {
        var failureCalled = false

        val fakeRepository = object : ReservationRepository {

            override fun getBookedDates(
                campsiteId: String,
                onSuccess: (List<Pair<String, String>>) -> Unit,
                onFailure: (Exception) -> Unit
            ) {
                onFailure(Exception("Test error"))
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
                onSuccess()
            }

            override fun cancelReservation(
                reservationId: String,
                onSuccess: () -> Unit,
                onFailure: (Exception) -> Unit
            ) {
                onSuccess()
            }
        }

        val controller = OwnerReservationController(fakeRepository)

        controller.getBookedDates(
            "campsite123",
            onSuccess = {},
            onFailure = { failureCalled = true }
        )

        assertTrue(failureCalled)
    }


}
