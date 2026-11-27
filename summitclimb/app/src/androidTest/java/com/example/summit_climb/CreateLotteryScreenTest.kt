package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.ui.owner.CreateLotteryContent
import com.example.summit_climb.ui.owner.CreateLotteryTags
import com.example.summit_climb.ui.owner.CreateLotteryText
import com.example.summit_climb.ui.owner.LotteryCampsiteOption
import com.example.summit_climb.ui.owner.endOfDay
import com.example.summit_climb.ui.owner.startOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

// UI tests for the List in Lottery screen (F7)
class CreateLotteryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val campsites = listOf(
        LotteryCampsiteOption("1", "Cedar Valley"),
        LotteryCampsiteOption("2", "Lakeside Camp")
    )

    // an open lottery with every field filled in, for edit mode
    private val day = LocalDate.of(2026, 12, 10)
    private val existing = Lottery(
        id = "L1",
        ownerId = "owner",
        campsiteIds = listOf("1"),
        registrationStart = startOfDay(day),
        registrationEnd = endOfDay(day.plusDays(5)),
        bookingStart = startOfDay(day.plusDays(6)),
        bookingEnd = endOfDay(day.plusDays(20)),
        winnerCount = 7,
        status = LotteryStatus.OPEN
    )

    private fun show(
        lottery: Lottery? = null,
        message: String = "",
        onBack: () -> Unit = {},
        onSave: (Lottery) -> Unit = {}
    ) {
        composeRule.setContent {
            CreateLotteryContent(
                campsites = campsites,
                existing = lottery,
                preselectedCampsiteId = "1",
                message = message,
                saving = false,
                onBack = onBack,
                onSave = onSave
            )
        }
    }

    @Test
    fun createModeShowsListTitleAndButton() {
        show()

        composeRule.onNodeWithTag(CreateLotteryTags.TITLE).assertTextContains(CreateLotteryText.TITLE)
        composeRule.onNodeWithText(CreateLotteryText.SAVE).assertIsDisplayed()
    }

    @Test
    fun campsiteWeCameFromIsAlreadyTicked() {
        show()

        composeRule.onNodeWithText("${CreateLotteryText.CAMPSITES} · 1 selected").assertIsDisplayed()
    }

    @Test
    fun tickingAnotherCampsiteAddsIt() {
        show()

        composeRule.onNodeWithTag(CreateLotteryTags.campsite("2")).performClick()

        composeRule.onNodeWithText("${CreateLotteryText.CAMPSITES} · 2 selected").assertIsDisplayed()
    }

    @Test
    fun saveIsDisabledUntilTheFormIsFilledIn() {
        show()

        composeRule.onNodeWithTag(CreateLotteryTags.SAVE).assertIsNotEnabled()
    }

    @Test
    fun waitlistCapFieldOnlyShowsWhenSwitchedOn() {
        show()

        composeRule.onNodeWithTag(CreateLotteryTags.CAP).assertDoesNotExist()
        composeRule.onNodeWithTag(CreateLotteryTags.CAP_SWITCH).performClick()
        composeRule.onNodeWithTag(CreateLotteryTags.CAP).assertIsDisplayed()
    }

    @Test
    fun messageFromValidateIsShown() {
        show(message = "Booking can't start before registration ends")

        composeRule.onNodeWithTag(CreateLotteryTags.MESSAGE)
            .assertTextContains("Booking can't start before registration ends")
    }

    @Test
    fun backCallsOnBack() {
        var wentBack = false
        show(onBack = { wentBack = true })

        composeRule.onNodeWithTag(CreateLotteryTags.BACK).performClick()

        assertTrue(wentBack)
    }

    @Test
    fun editModeShowsEditTitleAndSavedValues() {
        show(lottery = existing)

        composeRule.onNodeWithTag(CreateLotteryTags.TITLE).assertTextContains(CreateLotteryText.TITLE_EDIT)
        composeRule.onNodeWithText(CreateLotteryText.SAVE_EDIT).assertIsDisplayed()
        composeRule.onNodeWithTag(CreateLotteryTags.WINNERS).assertTextContains("7")
    }

    @Test
    fun editModeSavesTheSameLottery() {
        var saved: Lottery? = null
        show(lottery = existing, onSave = { saved = it })

        composeRule.onNodeWithTag(CreateLotteryTags.SAVE).assertIsEnabled().performClick()

        // same lottery id and values, so editLottery updates it instead of making a new one
        assertEquals("L1", saved?.id)
        assertEquals(7, saved?.winnerCount)
        assertEquals(existing.registrationEnd, saved?.registrationEnd)
        assertEquals(existing.bookingStart, saved?.bookingStart)
    }
}