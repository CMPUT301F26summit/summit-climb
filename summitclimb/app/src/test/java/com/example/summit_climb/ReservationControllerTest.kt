package com.example.summit_climb

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import com.example.summit_climb.controller.ReservationController
import com.example.summit_climb.model.Reservation
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.data.ReservationRepository
import com.example.summit_climb.data.NotificationRepository

class ReservationControllerTest {

    private class FakeNotificationRepository : NotificationRepository {

        val sentNotifications = mutableListOf<Notification>()

        override val notifications: List<Notification>
            get() = sentNotifications

        override fun send(notification: Notification) {
            sentNotifications.add(notification)
        }

        override fun markRead(notificationId: String) {}

        override fun forUser(userId: String): List<Notification> {
            return sentNotifications.filter {
                it.recipientId == userId
            }
        }

        override fun sentBy(senderId: String): List<Notification> {
            return sentNotifications.filter {
                it.senderId == senderId
            }
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

    private class FakeRepository : ReservationRepository {

        var receivedCampsiteId = ""
        var receivedEntrantId = ""
        var receivedEntrantName = ""
        var receivedStartDate = ""
        var receivedEndDate = ""
        var receivedGuests = 0
        var receivedReservationId = ""

        var receivedTotalPrice: Double? = null
        var receivedRulesAccepted: Boolean? = null

        var shouldFail = false

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
            totalPrice: Double?,
            rulesAccepted: Boolean?,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            receivedCampsiteId = campsiteId
            receivedEntrantId = entrantId
            receivedEntrantName = entrantName
            receivedStartDate = startDate
            receivedEndDate = endDate
            receivedGuests = guests
            receivedTotalPrice = totalPrice
            receivedRulesAccepted = rulesAccepted

            if (shouldFail) {
                onFailure(Exception("Reservation failed"))
            } else {
                onSuccess()
            }
        }

        override fun cancelReservation(
            reservationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            receivedReservationId = reservationId

            if (shouldFail) {
                onFailure(Exception("Cancellation failed"))
            } else {
                onSuccess()
            }
        }

        override fun getReservationsByEntrant(
            entrantId: String,
            onSuccess: (List<Reservation>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(emptyList())
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
        val fakeNotifications = FakeNotificationRepository()

        val controller = ReservationController(
            fakeRepository,
            fakeNotifications
        )

        controller.createReservation(
            "campsite123",
            "entrant456",
            "John Doe",
            "2023-06-01",
            "2023-06-05",
            4,
            totalPrice = 160.0,
            rulesAccepted = true,
            onSuccess = {},
            onFailure = { throw AssertionError(it) }
        )

        assertEquals("campsite123", fakeRepository.receivedCampsiteId)
        assertEquals("entrant456", fakeRepository.receivedEntrantId)
        assertEquals("John Doe", fakeRepository.receivedEntrantName)
        assertEquals("2023-06-01", fakeRepository.receivedStartDate)
        assertEquals("2023-06-05", fakeRepository.receivedEndDate)
        assertEquals(4, fakeRepository.receivedGuests)
        assertEquals(160.0, fakeRepository.receivedTotalPrice!!, 0.001)
        assertEquals(true, fakeRepository.receivedRulesAccepted)

        // Verify the booking confirmation notification.
        assertEquals(1, fakeNotifications.sentNotifications.size)

        val notification = fakeNotifications.sentNotifications.single()

        assertEquals("entrant456", notification.recipientId)
        assertEquals("campsite123", notification.campsiteId)
        assertEquals(NotificationType.RESERVATION_UPDATE, notification.type)
        assertEquals("Reservation confirmed", notification.title)
    }

    @Test
    fun correctReservationID() {

        val fakeRepository = FakeRepository()
        val fakeNotifications = FakeNotificationRepository()

        val controller = ReservationController(
            fakeRepository,
            fakeNotifications
        )

        controller.cancelReservation(
            reservationId = "reservation789",
            entrantId = "entrant456",
            campsiteId = "campsite123",
            onSuccess = {},
            onFailure = { throw AssertionError(it) }
        )

        assertEquals("reservation789", fakeRepository.receivedReservationId)

        // Verify the cancellation confirmation notification.
        assertEquals(1, fakeNotifications.sentNotifications.size)

        val notification = fakeNotifications.sentNotifications.single()

        assertEquals("entrant456", notification.recipientId)
        assertEquals("campsite123", notification.campsiteId)
        assertEquals("reservation789", notification.relatedId)
        assertEquals(NotificationType.RESERVATION_UPDATE, notification.type)
        assertEquals("Reservation cancelled", notification.title)
    }

    @Test
    fun failedReservationDoesNotSendNotification() {

        val fakeRepository = FakeRepository()
        fakeRepository.shouldFail = true

        val fakeNotifications = FakeNotificationRepository()

        val controller = ReservationController(
            fakeRepository,
            fakeNotifications
        )

        var failureCalled = false
        var successCalled = false

        controller.createReservation(
            campsiteId = "campsite123",
            entrantId = "entrant456",
            entrantName = "John Doe",
            startDate = "2026-12-01",
            endDate = "2026-12-05",
            guests = 2,
            totalPrice = 160.0,
            rulesAccepted = true,
            onSuccess = { successCalled = true },
            onFailure = { failureCalled = true }
        )

        assertTrue(failureCalled)
        assertFalse(successCalled)
        assertTrue(fakeNotifications.sentNotifications.isEmpty())
    }

    @Test
    fun failedCancellationDoesNotSendNotification() {

        val fakeRepository = FakeRepository()
        fakeRepository.shouldFail = true

        val fakeNotifications = FakeNotificationRepository()

        val controller = ReservationController(
            fakeRepository,
            fakeNotifications
        )

        var failureCalled = false
        var successCalled = false

        controller.cancelReservation(
            reservationId = "reservation789",
            entrantId = "entrant456",
            campsiteId = "campsite123",
            onSuccess = { successCalled = true },
            onFailure = { failureCalled = true }
        )

        assertTrue(failureCalled)
        assertFalse(successCalled)
        assertTrue(fakeNotifications.sentNotifications.isEmpty())
    }
}