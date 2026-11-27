package com.example.summit_climb

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.summit_climb.ui.owner.Entrant
import com.example.summit_climb.ui.owner.WaitlistContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WaitlistScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testEntrants = listOf(
        Entrant("1", "Alex Morgan", "Nov 18"),
        Entrant("2", "Jamie Wilson", "Nov 17")
    )

    @Test
    fun waitlistContent_displaysCampsiteInformation() {
        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = {},
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Waitlist")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Summit Camp")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Banff, AB")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Lottery Open")
            .assertIsDisplayed()
    }

    @Test
    fun waitlistContent_displaysEntrants() {
        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = {},
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Entrants")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Alex Morgan")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Jamie Wilson")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Joined Nov 18")
            .assertIsDisplayed()
    }

    @Test
    fun waitlistContent_displaysEmptyMessageWhenNoEntrants() {
        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = emptyList(),
                    lotteryStatus = "No Lottery",
                    entrantCount = 0,
                    winnersDrawnDate = "—",
                    maxEntrants = null,
                    onBack = {},
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("No entrants yet")
            .assertIsDisplayed()
    }

    @Test
    fun waitlistContent_backButtonCallsOnBack() {
        var backClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = { backClicked = true },
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        assertEquals(true, backClicked)
    }

    @Test
    fun waitlistContent_notifyButtonOpensNotificationSheet() {
        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = {},
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Notify Entrants")
            .performClick()

        composeTestRule.onNodeWithText("All 2 entrants")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Enter notification title")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(
            "Write your message to the entrants..."
        ).assertIsDisplayed()
    }

    @Test
    fun waitlistContent_sendButtonDisabledWhenFieldsAreEmpty() {
        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = {},
                    onSendNotification = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Notify Entrants")
            .performClick()

        composeTestRule.onNodeWithText("Send to 2 entrants")
            .assertIsNotEnabled()
    }

    @Test
    fun waitlistContent_sendsNotificationWithEnteredInformation() {
        var sentTitle = ""
        var sentMessage = ""

        composeTestRule.setContent {
            MaterialTheme {
                WaitlistContent(
                    campsiteName = "Summit Camp",
                    location = "Banff, AB",
                    entrants = testEntrants,
                    lotteryStatus = "Lottery Open",
                    entrantCount = 2,
                    winnersDrawnDate = "Nov 24",
                    maxEntrants = 50,
                    onBack = {},
                    onSendNotification = { title, message ->
                        sentTitle = title
                        sentMessage = message
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Notify Entrants")
            .performClick()

        composeTestRule.onNodeWithText("Enter notification title")
            .performTextInput("Lottery Update")

        composeTestRule.onNodeWithText(
            "Write your message to the entrants..."
        ).performTextInput("The lottery results are available.")

        composeTestRule.onNodeWithText("Send to 2 entrants")
            .assertIsEnabled()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals("Lottery Update", sentTitle)
            assertEquals(
                "The lottery results are available.",
                sentMessage
            )
        }
    }
}
