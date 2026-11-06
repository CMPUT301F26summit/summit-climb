package com.example.summit_climb

import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue
import com.example.summit_climb.model.Notification
import java.lang.Exception


class OwnerReservationControllerTest {
    private class FakeNotificationRepository: NotificationRepository {

        override val notifications: List<Notification> = emptyList()

        val sentNotifications = mutableListOf<Notification>()

        override fun send(notification: Notification) {
            sentNotifications.add(notification)
        }

        override fun markRead(notificationId: String) {
        }

        override fun forUser(userId: String): List<Notification> {
            return sentNotifications.filter { it.recipientId == userId}
        }

        override fun sentBy(senderId: String): List<Notification> {
            return sentNotifications.filter { it.senderId == senderId}
        }

        override fun listenToUser(
            userId: String,
            onAdded: (Notification) -> Unit,
            onModified: (Notification) -> Unit,
            onRemoved: (Notification) -> Unit
        ): () -> Unit {
            return {}
        }

    }

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

        val controller = OwnerReservationController(fakeRepository, FakeNotificationRepository())

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

        val controller = OwnerReservationController(fakeRepository, FakeNotificationRepository())

        controller.getBookedDates(
            "campsite123",
            onSuccess = {},
            onFailure = { failureCalled = true }
        )

        assertTrue(failureCalled)
    }

    @Test
    fun rejectingReservationSendsNotification() {
        val fakeNotificationRepository = FakeNotificationRepository()

        val fakeRepository = object : ReservationRepository {

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

        val controller = OwnerReservationController(
            fakeRepository,
            fakeNotificationRepository
        )

        controller.rejectReservation(
            reservationId = "reservation123",
            entrantId = "entrant456",
            campsiteId = "campsite789",
            onSuccess = {},
            onFailure = {}
        )

        assertEquals(1, fakeNotificationRepository.sentNotifications.size)

        val notification = fakeNotificationRepository.sentNotifications.first()

        assertEquals("entrant456", notification.recipientId)
        assertEquals("campsite789", notification.campsiteId)
        assertEquals("reservation123", notification.relatedId)
    }

    @Test
    fun failedRejectionDoesNotSendNotification() {
        val fakeNotificationRepository = FakeNotificationRepository()

        val fakeRepository = object : ReservationRepository {

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
                onFailure(Exception("Rejection failed"))
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

        val controller = OwnerReservationController(
            fakeRepository,
            fakeNotificationRepository
        )

        var failureCalled = false

        controller.rejectReservation(
            reservationId = "reservation123",
            entrantId = "entrant456",
            campsiteId = "campsite789",
            onSuccess = {},
            onFailure = { failureCalled = true }
        )

        assertTrue(failureCalled)
        assertTrue(fakeNotificationRepository.sentNotifications.isEmpty())
    }
}
