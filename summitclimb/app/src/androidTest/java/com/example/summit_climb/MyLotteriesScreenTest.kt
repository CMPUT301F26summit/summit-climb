package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// UI tests for the My Lotteries screen
class MyLotteriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // one lottery of each status
    private val sample = listOf(
        LotteryUiItem("1", "Summit Camp", "Banff, AB", "Pending", "Lottery closes October 30, 2026"),
        LotteryUiItem("2", "Cedar Valley", "Banff, AB", "Won", "You've been selected!"),
        LotteryUiItem("4", "Pine Ridge", "Canmore, AB", "Lost", "You weren't selected this time.")
    )

    // what the screen reported back (null = nothing reported)
    private var leftId: String? = null
    private var confirmedId: String? = null
    private var viewedId: String? = null
    private var messageCleared = false

    // shows the screen with the given lotteries and message
    private fun showScreen(lotteries: List<LotteryUiItem> = sample, message: String = "") {
        composeTestRule.setContent {
            MyLotteriesContent(
                lotteries = lotteries,
                message = message,
                onMessageShown = { messageCleared = true },
                onLeave = { id -> leftId = id },
                onConfirmReservation = { id -> confirmedId = id },
            )
        }
    }

    @Test
    fun everyLotteryIsShown() {
        showScreen()
        composeTestRule.onNodeWithText("Summit Camp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cedar Valley").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pine Ridge").assertIsDisplayed()
    }

    @Test
    fun eachStatusShowsItsButton() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.leave("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MyLotteriesTags.confirm("2")).assertIsDisplayed()
    }

    @Test
    fun lostLotteryHasNoButton() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.leave("4")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MyLotteriesTags.confirm("4")).assertDoesNotExist()
    }

    @Test
    fun noLotteriesShowsEmptyMessage() {
        showScreen(lotteries = emptyList())
        composeTestRule.onNodeWithText(MyLotteriesText.NONE).assertIsDisplayed()
    }

    // ---------- leaving (US 01.02.02) ----------

    @Test
    fun leaveOpensPopupFirst() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.leave("1")).performClick()

        composeTestRule.onNodeWithText(MyLotteriesText.LEAVE_TITLE).assertIsDisplayed()
        assertNull(leftId)   // not left yet, only asked
    }

    @Test
    fun confirmingLeaveReportsTheLottery() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.leave("1")).performClick()
        composeTestRule.onNodeWithTag(MyLotteriesTags.CONFIRM_LEAVE).performClick()

        assertEquals("1", leftId)
        composeTestRule.onNodeWithText(MyLotteriesText.LEAVE_TITLE).assertDoesNotExist()
    }

    @Test
    fun cancellingLeaveDoesNothing() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.leave("1")).performClick()
        composeTestRule.onNodeWithText(MyLotteriesText.CANCEL).performClick()

        assertNull(leftId)
        composeTestRule.onNodeWithText(MyLotteriesText.LEAVE_TITLE).assertDoesNotExist()
    }

    // ---------- reservations (US 01.04.01, 01.04.02) ----------

    @Test
    fun confirmReservationReportsTheLottery() {
        showScreen()
        composeTestRule.onNodeWithTag(MyLotteriesTags.confirm("2")).performClick()
        assertEquals("2", confirmedId)
    }

    // message tests

    @Test
    fun messageIsShown() {
        showScreen(message = MyLotteriesText.left("Summit Camp"))
        composeTestRule.onNodeWithText("You left the lottery for Summit Camp.").assertIsDisplayed()
    }

    @Test
    fun messageClearsAfterThreeSeconds() {
        composeTestRule.mainClock.autoAdvance = false   // control time ourselves
        showScreen(message = MyLotteriesText.left("Summit Camp"))

        composeTestRule.mainClock.advanceTimeBy(3100)

        assertTrue(messageCleared)
    }
}