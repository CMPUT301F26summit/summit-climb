package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.summit_climb.model.Lottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [CampsiteDetailsScreen]
 * The screen is shown with fake campsites and lotteries from [TestCampsites] instead of Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun details_screen_shows_campsite_information() {
        composeTestRule.setContent { CampsiteDetailsForTest(campsite = TestCampsites.cedarValley) }

        // verify name, location and price
        composeTestRule
            .onNodeWithText("Cedar Valley")
            .assertExists()
        composeTestRule
            .onNodeWithText("Banff, AB")
            .assertExists()
        composeTestRule
            .onNodeWithText("$60")
            .assertExists()

        // verify description, rules and cancellation policy
        composeTestRule
            .onNodeWithText(TestCampsites.cedarValley.description)
            .assertExists()
        composeTestRule
            .onNodeWithText(TestCampsites.cedarValley.rules)
            .assertExists()
        composeTestRule
            .onNodeWithText(TestCampsites.cedarValley.cancellationPolicy)
            .assertExists()

        // verify amenity icons
        composeTestRule
            .onNodeWithContentDescription("Parking")
            .assertExists()
        composeTestRule
            .onNodeWithContentDescription("Fire")
            .assertExists()
        composeTestRule
            .onNodeWithContentDescription("Trails")
            .assertDoesNotExist()
    }

    @Test
    fun campsite_without_amenities_says_none_listed() {
        composeTestRule.setContent { CampsiteDetailsForTest(campsite = TestCampsites.summitCamp) }

        // verify the empty amenities message
        composeTestRule
            .onNodeWithText("No amenities listed")
            .assertExists()
    }

    @Test
    fun campsite_without_lottery_shows_no_join_button() {
        composeTestRule.setContent { CampsiteDetailsForTest(lottery = null) }

        // verify badge and message
        composeTestRule
            .onNodeWithText("No Lottery")
            .assertExists()
        composeTestRule
            .onNodeWithText(CampsiteDetailsText.NO_ACTIVE_LOTTERY)
            .assertExists()

        // verify there is nothing to join
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun open_lottery_shows_badge_and_waitlist_size() {
        composeTestRule.setContent { CampsiteDetailsForTest(lottery = TestCampsites.openLottery()) }

        // verify badge, winner count and waitlist size
        composeTestRule
            .onNodeWithText("Lottery Open")
            .assertExists()
        composeTestRule
            .onNodeWithText("Winners drawn: 5")
            .assertExists()
        composeTestRule
            .onNodeWithText("12 joined")
            .assertExists()
    }

    @Test
    fun join_open_lottery_then_leave_it() {
        composeTestRule.setContent { CampsiteDetailsForTest(lottery = TestCampsites.openLottery()) }

        // join the lottery
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .performScrollTo()
            .performClick()

        // verify joined message and leave button
        composeTestRule
            .onNodeWithText(CampsiteDetailsText.JOINED)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.LEAVE_BUTTON)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .assertDoesNotExist()

        // leave the lottery
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.LEAVE_BUTTON)
            .performScrollTo()
            .performClick()

        // verify left message and join button is back
        composeTestRule
            .onNodeWithText(CampsiteDetailsText.LEFT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .assertExists()
    }

    @Test
    fun joining_drawn_lottery_shows_registration_closed() {
        composeTestRule.setContent { CampsiteDetailsForTest(lottery = TestCampsites.drawnLottery()) }

        // verify badge
        composeTestRule
            .onNodeWithText("Drawn")
            .assertExists()

        // try to join
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .performScrollTo()
            .performClick()

        // verify closed message and the user did not join
        composeTestRule
            .onNodeWithText(EntrantLotteryController.MESSAGE_CLOSED)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.LEAVE_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun joining_full_lottery_shows_waitlist_full() {
        composeTestRule.setContent { CampsiteDetailsForTest(lottery = TestCampsites.fullLottery()) }

        // verify the cap is shown
        composeTestRule
            .onNodeWithText("2 of 2 spots taken")
            .assertExists()

        // try to join
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.JOIN_BUTTON)
            .performScrollTo()
            .performClick()

        // verify full message and the user did not join
        composeTestRule
            .onNodeWithText(EntrantLotteryController.MESSAGE_FULL)
            .assertExists()
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.LEAVE_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun rate_campsite_four_stars() {
        var ratedStars = 0
        composeTestRule.setContent {
            CampsiteDetailsForTest(onRate = { stars -> ratedStars = stars })
        }

        // tap 4th star
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.star(4))
            .performScrollTo()
            .performClick()

        // verify thank you message and the rating sent
        composeTestRule
            .onNodeWithText(CampsiteDetailsText.RATED)
            .assertExists()
        assertEquals(4, ratedStars)
    }

    @Test
    fun back_button_goes_back() {
        var backClicked = false
        composeTestRule.setContent {
            CampsiteDetailsForTest(onBack = { backClicked = true })
        }

        // tap back
        composeTestRule
            .onNodeWithTag(CampsiteDetailsTags.BACK_BUTTON)
            .performClick()

        // verify back was requested
        assertTrue(backClicked)
    }
}

/**
 * [CampsiteDetailsContent] with join, leave and rating
 *
 * @param campsite: campsite to show.
 * @param lottery: the campsite's active lottery, or `null` for none.
 * @param onRate: called with the star value the user tapped.
 * @param onBack: called when the user taps Back.
 */
@Composable
private fun CampsiteDetailsForTest(
    campsite: Campsite = TestCampsites.cedarValley,
    lottery: Lottery? = TestCampsites.openLottery(),
    onRate: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var hasJoined by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    CampsiteDetailsContent(
        campsite = campsite,
        lottery = lottery,
        hasJoined = hasJoined,
        message = message,
        onBackClick = onBack,
        onJoinClick = {
            if (lottery != null) {
                val problem = EntrantLotteryController.getJoinRejectionReason(lottery, hasJoined)
                if (problem == null) {
                    hasJoined = true
                    message = CampsiteDetailsText.JOINED
                } else {
                    message = problem
                }
            }
        },
        onLeaveClick = {
            hasJoined = false
            message = CampsiteDetailsText.LEFT
        },
        onRateClick = { stars ->
            onRate(stars)
            message = CampsiteDetailsText.RATED
        }
    )
}