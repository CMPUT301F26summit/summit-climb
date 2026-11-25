package com.example.summit_climb

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.summit_climb.ui.owner.ManageCampsiteScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [com.example.summit_climb.ui.owner.ManageCampsiteScreen]
 *
 * The screen is shown with sample campsite information instead of Firestore.
 */
@RunWith(AndroidJUnit4::class)
class ManageCampsiteScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun manage_screen_shows_campsite_information_and_menu_options() {
        composeTestRule.setContent {
            ManageCampsiteScreen(
                navController = rememberNavController(),
                campsiteId = "test-campsite-id",
                campsiteName = "Climbing Camp",
                location = "Banff, AB",
                lotteryStatus = "Winners Drawn",
                activeReservations = 3,
                entrantCount = 12
            )
        }

        // Verify campsite information.
        composeTestRule
            .onNodeWithText("Manage")
            .assertExists()

        composeTestRule
            .onNodeWithText("Climbing Camp")
            .assertExists()

        composeTestRule
            .onNodeWithText("Banff, AB")
            .assertExists()

        composeTestRule
            .onNodeWithText("Winners Drawn")
            .assertExists()

        // Verify reservations option and count.
        composeTestRule
            .onNodeWithText("Reservations")
            .assertExists()

        composeTestRule
            .onNodeWithText("3 active")
            .assertExists()

        // Verify waitlist option and entrant count.
        composeTestRule
            .onNodeWithText("Waitlist")
            .assertExists()

        composeTestRule
            .onNodeWithText("12 entrants")
            .assertExists()

        // Verify sent notifications option.
        composeTestRule
            .onNodeWithText("Sent notifications")
            .assertExists()

        // Verify QR code option.
        composeTestRule
            .onNodeWithText("QR code")
            .assertExists()
    }
}