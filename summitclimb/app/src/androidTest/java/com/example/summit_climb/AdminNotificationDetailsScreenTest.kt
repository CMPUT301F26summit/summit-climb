
package com.example.summit_climb.ui.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.summit_climb.model.Notification
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminNotificationDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createNotification(
        recipientId: String = "user123",
        campsiteId: String = "campsite123",
        title: String = "Lottery won",
        message: String = "Congratulations! You have been selected."
    ): Notification {
        return Notification(
            id = "notification123",
            senderId = "system",
            recipientId = recipientId,
            title = title,
            text = message,
            campsiteId = campsiteId,
            timestamp = 1764201600000L,
            read = false
        )
    }

    @Test
    fun adminNotificationDetails_displaysHeaderInformation() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Admin View")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Notification")
            .assertIsDisplayed()
    }

    @Test
    fun adminNotificationDetails_displaysNotificationInformation() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Lottery won")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Delivered")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Jane Doe")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Cedar Valley")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(
            "Congratulations! You have been selected."
        ).assertIsDisplayed()
    }

    @Test
    fun adminNotificationDetails_displaysInformationLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("To")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Sent")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Full text")
            .assertIsDisplayed()
    }

    @Test
    fun adminNotificationDetails_backButtonCallsOnBack() {
        var backClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = { backClicked = true },
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("← Back")
            .performClick()

        assertEquals(true, backClicked)
    }

    @Test
    fun adminNotificationDetails_viewUserCallsCallbackWithRecipientId() {
        var selectedUserId = ""

        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(
                        recipientId = "user123"
                    ),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = { selectedUserId = it },
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("View User")
            .performClick()

        assertEquals("user123", selectedUserId)
    }

    @Test
    fun adminNotificationDetails_viewCampsiteCallsCallbackWithCampsiteId() {
        var selectedCampsiteId = ""

        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(
                        campsiteId = "campsite123"
                    ),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = { selectedCampsiteId = it }
                )
            }
        }

        composeTestRule.onNodeWithText("View Campsite")
            .performClick()

        assertEquals("campsite123", selectedCampsiteId)
    }

    @Test
    fun adminNotificationDetails_viewUserDisabledWhenRecipientIdIsBlank() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(
                        recipientId = ""
                    ),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("View User")
            .assertIsNotEnabled()
    }

    @Test
    fun adminNotificationDetails_viewCampsiteDisabledWhenCampsiteIdIsBlank() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(
                        campsiteId = ""
                    ),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("View Campsite")
            .assertIsNotEnabled()
    }

    @Test
    fun adminNotificationDetails_displaysReadOnlyMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminNotificationDetailsScreen(
                    notification = createNotification(),
                    recipientName = "Jane Doe",
                    campsiteName = "Cedar Valley",
                    onBack = {},
                    onViewUser = {},
                    onViewCampsite = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Read-only. Logs can't be edited or deleted."
        ).assertIsDisplayed()
    }
}
