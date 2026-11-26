package com.example.summit_climb

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.ListingStatus
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.ui.owner.OwnerCampsiteDetails
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerCampDetailsIntentTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController

    // Has ratings, a booked date and amenities
    val lakeside = Campsite(
        id = "0Fg6o1BEFVOumnrKQ8m9ZsqAbHP2",
        name = "Lakeside Campsite",
        location = "Alberta Beach, AB",
        maxOccupancy = 6,
        price = 78.0,
        description = "Super cool and super swag area, it has bears and stuff which is pretty cool if you like that kinda stuff.",
        amenities = listOf("Parking", "Washrooms", "Fire", "Picnic tables"),
        bookedDates = listOf("2026-07-12"),
        ratings = mapOf("user1" to 4, "user2" to 5),
        avgRating = 4.5,
        listingStatus = ListingStatus.APPROVED
    )

    // No ratings, no booked dates
    val lakeview = Campsite(
        id = "DwH0Fa1Av4MoDtTFhmg0xymaZZ83",
        name = "Lakeview Camp",
        location = "Jasper, AB",
        maxOccupancy = 4,
        price = 120.0,
        description = "Very nice area, please visit.",
        amenities = listOf("Trails", "Fire"),
        listingStatus = ListingStatus.APPROVED
    )
    fun showDetails(
        campsite: Campsite,
        status: LotteryStatus? = null,
        prevScreen: Boolean = false
    ) {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
                .apply { navigatorProvider.addNavigator(ComposeNavigator()) }

            NavHost(
                navController,
                startDestination =
                    if (prevScreen)
                        "ownerCampsite"
                    else
                        "ownerCampsiteDetails"
            ) {
                composable("ownerCampsite") { }
                composable("ownerCampsiteDetails") {
                    OwnerCampsiteDetails(
                        navController = navController,
                        campsite = campsite,
                        lotteryStatus = status
                    )
                }
                composable("ownerEditCamp") { }
                composable("manageCampsiteScreen/{id}") { }
                composable("ownerListLottery") { }
            }
        }

        if (prevScreen) {
            composeTestRule.runOnUiThread { navController.navigate("ownerCampsiteDetails") }
            composeTestRule.waitForIdle()
        }
    }
    fun navigation() = navController.currentBackStackEntry?.destination?.route

    // campsite details

    // US 02.01.04: name, location, price and occupancy are shown
    @Test
    fun displaysNameLocationPriceAndOccupancy() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("Lakeside Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alberta Beach, AB").assertIsDisplayed()
        composeTestRule.onNodeWithText("78", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("up to 6 guests", substring = true).assertIsDisplayed()
    }

    // US 02.01.04: the description is shown
    @Test
    fun displaysCampsiteDescription() {
        showDetails(lakeview)

        composeTestRule.onNodeWithText("About this Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Very nice area, please visit.").assertIsDisplayed()
    }

    // US 02.01.04: a rated campsite shows its rating or "no ratings yet" if none
    @Test
    fun displaysRating() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("4.5", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("No ratings yet", substring = true).assertDoesNotExist()
    }

    // US 02.01.04: a campsite with no ratings says so
    @Test
    fun displaysNoRatingMessage() {
        showDetails(lakeview)

        composeTestRule.onNodeWithText("No ratings yet", substring = true).assertIsDisplayed()
    }

    // US 02.01.04: a campsite with amenities does not show the empty message
    @Test
    fun displaysAmenities() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("No amenities listed").assertDoesNotExist()
    }

    // US 02.01.04: a campsite with no amenities shows the empty message
    @Test
    fun displaysWithoutAmenitiesMessage() {
        showDetails(lakeview.copy(amenities = emptyList()))

        composeTestRule.onNodeWithText("No amenities listed").performScrollTo().assertIsDisplayed()
    }


    // US 02.01.04: no lottery
    @Test
    fun campsiteNoLottery() {
        showDetails(lakeview, status = null)
        composeTestRule.onNodeWithText("No Lottery").assertIsDisplayed()
    }

    // US 02.01.04: open lottery
    @Test
    fun campsiteOpenLottery() {
        showDetails(lakeside, status = LotteryStatus.OPEN)
        composeTestRule.onNodeWithText("Lottery Open").assertIsDisplayed()
    }

    // US 02.01.04: drawn lottery
    @Test
    fun campsiteDrawnLottery() {
        showDetails(lakeside, status = LotteryStatus.DRAWN)
        composeTestRule.onNodeWithText("Drawn").assertIsDisplayed()
    }

    // US 02.01.04: closed lottery
    @Test
    fun campsiteClosedLottery() {
        showDetails(lakeside, status = LotteryStatus.CLOSED)
        composeTestRule.onNodeWithText("Closed").assertIsDisplayed()
    }

    // US 02.01.03, US 02.01.01, US 02.04.01, US 02.04.06, US 02.06.01

    // Back returns to the list
    @Test
    fun backButton_returnsToPrevScreen() {
        showDetails(lakeside, prevScreen = true)

        composeTestRule.onNodeWithText("Back").performClick()

        assertEquals("ownerCampsite", navigation())
    }

    // Covers US 02.01.03: Edit Campsite opens the edit screen
    /*@Test
    fun tappingEditCampsite_opensEditScreen() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("Edit Campsite").performClick()

        assertEquals("ownerEditCamp", navigation())
    }

    // Covers US 02.04.01: Edit Lottery opens the lottery screen
    @Test
    fun tappingEditLottery_opensLotteryScreen() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("Edit Lottery").performScrollTo().performClick()

        assertEquals("ownerListLottery", navigation())
    }

    // Covers US 02.01.04: Manage opens the manage screen for this campsite
    @Test
    fun tappingManage_opensManageScreenForThisCampsite() {
        showDetails(lakeside)

        composeTestRule.onNodeWithText("Manage").performScrollTo().performClick()

        assertEquals("manageCampsiteScreen/{id}", navigation())
        assertEquals(
            "0Fg6o1BEFVOumnrKQ8m9ZsqAbHP2",
            navController.currentBackStackEntry?.arguments?.getString("id")
        )
    }*/
}