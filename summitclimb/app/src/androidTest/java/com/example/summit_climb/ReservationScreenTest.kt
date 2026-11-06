package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

//UI tests for the reservation screen
class ReservationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate.now()
    private val wonInfo = ReservationUiInfo(
        lotteryId = "1",
        campsiteName = "Cedar Valley",
        location = "Banff, AB",
        bookingStart = today.plusDays(2),
        bookingEnd = today.plusDays(25),
        bookedDates = emptySet(),
        reserved = false
    )

    // what the screen reported back
    private var wentBack = false
    private var declined = false

    // shows the screen with the given info
    private fun showScreen(info: ReservationUiInfo = wonInfo) {
        composeTestRule.setContent {
            ReservationContent(
                info = info,
                message = "",
                onBackClick = { wentBack = true },
                onConfirm = { _, _ -> },
                onDecline = { declined = true },
                onGuidelinesClick = {}
            )
        }
    }

    // Shows right thing tests
    @Test
    fun wonLotteryShowsCongratulations() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText(ReservationText.CONGRATS).assertIsDisplayed()
        composeTestRule.onNodeWithText("You've been selected for Cedar Valley.").assertIsDisplayed()
    }

    @Test
    fun confirmIsDisabledUntilDatesPicked() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.CONFIRM).assertIsNotEnabled()
        composeTestRule.onNodeWithText(ReservationText.PICK_CHECK_IN).assertIsDisplayed()
    }

    @Test
    fun reservedShowsViewOnly() {
        showScreen(wonInfo.copy(reserved = true))
        composeTestRule.onNodeWithText(ReservationText.TITLE_VIEW).assertIsDisplayed()
        composeTestRule.onNodeWithText(ReservationText.VIEW_ONLY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ReservationTags.CONFIRM).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ReservationTags.DECLINE).assertDoesNotExist()
    }

    // Goes back tests

    @Test
    fun backReportsTap() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.BACK).performClick()
        assertTrue(wentBack)
    }

    // decline tests

    @Test
    fun declineOpensPopupFirst() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.DECLINE).performClick()

        composeTestRule.onNodeWithText(ReservationText.DECLINE_TITLE).assertIsDisplayed()
        assertFalse(declined)   // only asked, not declined yet
    }

    @Test
    fun confirmingDeclineReportsIt() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.DECLINE).performClick()
        composeTestRule.onNodeWithTag(ReservationTags.DECLINE_POPUP).performClick()

        assertTrue(declined)
        composeTestRule.onNodeWithText(ReservationText.DECLINE_TITLE).assertDoesNotExist()
    }

    @Test
    fun cancellingDeclineDoesNothing() {
        showScreen()
        composeTestRule.onNodeWithTag(ReservationTags.DECLINE).performClick()
        composeTestRule.onNodeWithText(ReservationText.CANCEL).performClick()

        assertFalse(declined)
        composeTestRule.onNodeWithText(ReservationText.DECLINE_TITLE).assertDoesNotExist()
    }
}