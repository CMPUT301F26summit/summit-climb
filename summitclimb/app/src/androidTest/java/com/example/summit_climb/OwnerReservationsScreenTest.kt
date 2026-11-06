package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import com.example.summit_climb.ui.owner.OwnerReservationsScreen
import com.example.summit_climb.ui.owner.ReservationUiItem
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.model.Reservation
import com.example.summit_climb.ui.owner.OwnerReservationsFromFirestore
import com.example.summit_climb.model.Notification
import com.example.summit_climb.data.NotificationRepository

class OwnerReservationsScreenTest {

    private class FakeNotificationRepository : NotificationRepository {

        override val notifications: List<Notification> = emptyList()

        val sentNotifications = mutableListOf<Notification>()

        override fun send(notification: Notification) {
            sentNotifications.add(notification)
        }

        override fun markRead(notificationId: String) {
        }

        override fun forUser(userId: String): List<Notification> {
            return sentNotifications.filter { it.recipientId == userId }
        }

        override fun sentBy(senderId: String): List<Notification> {
            return sentNotifications.filter { it.senderId == senderId }
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

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeReservationsDisplayed() {

        val sampleReservations = listOf(
            ReservationUiItem(
                id = "1",
                entrantName = "Ava Thompson",
                dates = "November 9–11, 2026",
                guests = 4,
                status = "ACTIVE"
            ),

            ReservationUiItem(
                id = "2",
                entrantName = "Liam Chen",
                dates = "November 17–19, 2026",
                guests = 2,
                status = "ACTIVE"
            )
        )

        composeTestRule.setContent {
            OwnerReservationsScreen(
                reservations = sampleReservations,
                onReservationClick = {},
                onRejectClick = {}
            )
        }

        composeTestRule.onNodeWithText("Ava Thompson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Liam Chen").assertIsDisplayed()
    }

    @Test
    fun pastReservationsDisplayed() {

        val sampleReservations = listOf(
            ReservationUiItem(
                id = "3",
                entrantName = "Sophia Martin",
                dates = "September 12–14, 2026",
                guests = 3,
                status = "COMPLETED"
            )
        )

        composeTestRule.setContent {
            OwnerReservationsScreen(
                reservations = sampleReservations,
                onReservationClick = {},
                onRejectClick = {}
            )
        }

        composeTestRule.onNodeWithText("Past").performClick()

        composeTestRule.onNodeWithText("Sophia Martin").assertIsDisplayed()
    }

    @Test

    fun rejectedReservationsDisplayed() {

        val sampleReservations = listOf(
            ReservationUiItem(
                id = "5",
                entrantName = "Noah Patel",
                dates = "October 20–22, 2026",
                guests = 4,
                status = "REJECTED"
            )
        )

        composeTestRule.setContent {
            OwnerReservationsScreen(
                reservations = sampleReservations,
                onReservationClick = {},
                onRejectClick = {}
            )
        }

        composeTestRule.onNodeWithText("Cancelled").performClick()

        composeTestRule.onNodeWithText("Noah Patel").assertIsDisplayed()
    }

    @Test
    fun rejectReservationConfirmed(){

        var rejectedId: String? = null
        val sampleReservations = listOf(
            ReservationUiItem(
                id = "5",
                entrantName = "Noah Patel",
                dates = "October 20–22, 2026",
                guests = 4,
                status = "ACTIVE"
            )
        )
        composeTestRule.setContent {
            OwnerReservationsScreen(
                reservations = sampleReservations,
                onReservationClick = {},
                onRejectClick = {reservation -> rejectedId = reservation.id}
            )
        }
        composeTestRule.onNodeWithText("Reject").performClick()
        composeTestRule.onNodeWithText("Reject reservation?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Noah Patel · October 20–22, 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("The guest will be notified and the dates " +
                "will open up again.").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Reject")
            .onLast()
            .performClick()
        assertEquals("5", rejectedId)

    }

    @Test

    fun rejectReservationCancelled(){
        var rejectedId: String? = null

        val sampleReservations = listOf(
            ReservationUiItem(
                id = "5",
                entrantName = "Noah Patel",
                dates = "October 20–22, 2026",
                guests = 4,
                status = "ACTIVE"
            )
        )

        composeTestRule.setContent {
            OwnerReservationsScreen(
                reservations = sampleReservations,
                onReservationClick = {},
                onRejectClick = {reservation-> rejectedId = reservation.id}
            )
        }

        // Open confirmation dialog
        composeTestRule.onNodeWithText("Reject").performClick()

        // cancel rejection
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Reject reservation?").assertDoesNotExist()

        assertNull(rejectedId)
    }

    @Test
    fun expiredReservationDisplayedAsPast() {

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
                onSuccess(
                    listOf(
                        Reservation(
                            id = "1",
                            campsiteId = campsiteId,
                            entrantName = "Test Entrant",
                            startDate = "2025-06-01",
                            endDate = "2025-06-03",
                            guests = 2,
                            status = "ACTIVE"
                        )
                    )
                )
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
                onFailure: (Exception) -> Unit)
            {
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

        composeTestRule.setContent {
            OwnerReservationsFromFirestore(
                campsiteId = "test-campsite",
                controller = controller,
                onReservationClick = {}
            )
        }

        composeTestRule.onNodeWithText("Past").performClick()

        composeTestRule.onNodeWithText("Test Entrant")
            .assertIsDisplayed()
    }
}
