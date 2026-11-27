package com.example.summit_climb

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.ui.owner.SentNotificationsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SentNotificationsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createNotification(
        id: String,
        senderId: String = "owner123",
        recipientId: String,
        campsiteId: String = "campsite123",
        batchId: String = "batch1",
        title: String = "Lottery Results",
        message: String = "The lottery results are available."
    ): Notification {
        return Notification(
            id = id,
            senderId = senderId,
            recipientId = recipientId,
            type = NotificationType.MESSAGE,
            title = title,
            text = message,
            campsiteId = campsiteId,
            batchId = batchId,
            timestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun sentNotificationsScreen_displaysHeaderInformation() {
        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Mountain View Campground",
                    currentUserId = "owner123",
                    notifications = emptyList(),
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sent Notifications")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Mountain View Campground")
            .assertIsDisplayed()
    }

    @Test
    fun sentNotificationsScreen_displaysEmptyMessageWhenNoNotificationsExist() {
        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = emptyList(),
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            "No notifications have been sent yet."
        ).assertIsDisplayed()
    }

    @Test
    fun sentNotificationsScreen_displaysSentNotification() {
        val notifications = listOf(
            createNotification(
                id = "notification1",
                recipientId = "entrant1"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = notifications,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Lottery Results")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(
            "The lottery results are available."
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("Sent to 1 entrants")
            .assertIsDisplayed()
    }

    @Test
    fun sentNotificationsScreen_groupsNotificationsFromSameBatch() {
        val notifications = listOf(
            createNotification(
                id = "notification1",
                recipientId = "entrant1",
                batchId = "batch1"
            ),
            createNotification(
                id = "notification2",
                recipientId = "entrant2",
                batchId = "batch1"
            ),
            createNotification(
                id = "notification3",
                recipientId = "entrant3",
                batchId = "batch1"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = notifications,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Lottery Results")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Sent to 3 entrants")
            .assertIsDisplayed()
    }

    @Test
    fun sentNotificationsScreen_countsUniqueRecipientsInBatch() {
        val notifications = listOf(
            createNotification(
                id = "notification1",
                recipientId = "entrant1",
                batchId = "batch1"
            ),
            createNotification(
                id = "notification2",
                recipientId = "entrant1",
                batchId = "batch1"
            ),
            createNotification(
                id = "notification3",
                recipientId = "entrant2",
                batchId = "batch1"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = notifications,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sent to 2 entrants")
            .assertIsDisplayed()
    }

    @Test
    fun sentNotificationsScreen_onlyDisplaysCurrentOwnersNotifications() {
        val notifications = listOf(
            createNotification(
                id = "notification1",
                recipientId = "entrant1",
                title = "My Notification"
            ),
            createNotification(
                id = "notification2",
                senderId = "anotherOwner",
                recipientId = "entrant2",
                title = "Other Owner Notification"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = notifications,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("My Notification")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Other Owner Notification")
            .assertDoesNotExist()
    }

    @Test
    fun sentNotificationsScreen_onlyDisplaysSelectedCampsiteNotifications() {
        val notifications = listOf(
            createNotification(
                id = "notification1",
                recipientId = "entrant1",
                campsiteId = "campsite123",
                title = "Selected Campsite Notification"
            ),
            createNotification(
                id = "notification2",
                recipientId = "entrant2",
                campsiteId = "anotherCampsite",
                title = "Different Campsite Notification"
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = notifications,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Selected Campsite Notification")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Different Campsite Notification")
            .assertDoesNotExist()
    }

    @Test
    fun sentNotificationsScreen_backButtonCallsOnBack() {
        var backClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                SentNotificationsScreen(
                    campsiteId = "campsite123",
                    campsiteName = "Summit Camp",
                    currentUserId = "owner123",
                    notifications = emptyList(),
                    onBack = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        assertEquals(true, backClicked)
    }
}
