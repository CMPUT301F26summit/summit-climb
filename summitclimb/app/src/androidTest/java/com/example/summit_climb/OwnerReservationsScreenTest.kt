package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.summit_climb.ui.owner.OwnerReservationsScreen
import com.example.summit_climb.ui.owner.ReservationUiItem
import androidx.compose.ui.test.performClick

class OwnerReservationsScreenTest {

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
}
