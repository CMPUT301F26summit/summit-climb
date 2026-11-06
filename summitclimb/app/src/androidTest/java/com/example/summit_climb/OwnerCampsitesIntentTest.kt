package com.example.summit_climb

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test
import kotlin.collections.emptyList
import com.example.summit_climb.model.LotteryStatus


class OwnerCampsitesIntentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val lotteryStatus = mapOf(
        "0Fg6o1BEFVOumnrKQ8m9ZsqAbHP2" to LotteryStatus.OPEN,
        "DwH0Fa1Av4MoDtTFhmg0xymaZZ83" to LotteryStatus.DRAWN
    )

    // US 02.03.01: owner with no campsites sees an empty-state message
    @Test
    fun noListedCampsitesShowsEmptyStateMessage() {
        composeTestRule.setContent {
            OwnerCampsites(
                navController = rememberNavController(),
                campsites = emptyList(),
                lotteryStatus = lotteryStatus
            )
        }
        composeTestRule
            .onNodeWithText("No campsites yet. Tap + to list a campsite.")
            .assertIsDisplayed()
    }

    // US 02.03.01: campsite listing with info
    @Test
    fun campsitesListedShowsEachListing() {

        // Has ratings, a booked date and amenities
        val campsites = listOf(
            Campsite(
                id = "0Fg6o1BEFVOumnrKQ8m9ZsqAbHP2",
                name = "Lakeside Campsite",
                location = "Alberta Beach, AB",
                maxOccupancy = 6,
                price = 78.0,
                description = "Super cool and super swag area, it has bears and stuff which is pretty cool if you like that kinda stuff.",
                amenities = listOf("Parking", "Washrooms", "Fire", "Picnic tables"),
                bookedDates = listOf("2026-07-12"),
                ratings = mapOf("user1" to 4, "user2" to 5),
                listingStatus = ListingStatus.APPROVED
            ),

            // No ratings, no booked dates
            Campsite(
                id = "DwH0Fa1Av4MoDtTFhmg0xymaZZ83",
                name = "Lakeview Camp",
                location = "Jasper, AB",
                maxOccupancy = 4,
                price = 120.0,
                description = "Very nice area, please visit.",
                amenities = listOf("Trails", "Fire"),
                listingStatus = ListingStatus.APPROVED
            )
        )
        composeTestRule.setContent {
            OwnerCampsites(navController = rememberNavController(), campsites = campsites)
        }

        composeTestRule.onNodeWithText("Lakeside Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lakeview Camp").assertIsDisplayed()
    }
}