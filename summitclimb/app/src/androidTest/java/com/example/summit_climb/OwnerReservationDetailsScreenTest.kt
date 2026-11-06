package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.summit_climb.ui.owner.OwnerReservationDetailsScreen
import org.junit.Rule
import org.junit.Test

class OwnerReservationDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reservationDetailsDisplayed() {

        composeTestRule.setContent {
            OwnerReservationDetailsScreen(
                entrantName = "Ava Thompson",
                dates = "November 9–11, 2026",
                nights = 2,
                guests = 4,
                totalPrice = "$90",
                status = "ACTIVE",
                rulesAccepted = true,
                onMessageClick = {},
                onRejectClick = {}
            )
        }

        composeTestRule.onNodeWithText("Ava Thompson")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Dates: November 9–11, 2026")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Guests: 4")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Total: $90")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Campsite rules: Accepted")
            .assertIsDisplayed()
    }
}