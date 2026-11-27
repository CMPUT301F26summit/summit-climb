
package com.example.summit_climb.ui.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.User
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminLogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createNotification(
        id: String = "notification1",
        recipientId: String = "user1",
        campsiteId: String = "campsite1",
        title: String = "Lottery Results",
        timestamp: Long = 1764201600000L
    ): Notification {
        return Notification(
            id = id,
            senderId = "system",
            recipientId = recipientId,
            title = title,
            text = "Your lottery results are available.",
            campsiteId = campsiteId,
            timestamp = timestamp,
            read = false
        )
    }

    private fun createUser(
        userId: String = "user1",
        name: String = "Jane Doe",
        profileImageUrl: String = ""
    ): User {
        return User(
            userId = userId,
            name = name,
            profileImageUrl = profileImageUrl
        )
    }

    private fun setAdminLogsContent(
        campsites: List<Campsite> = emptyList(),
        users: List<User> = emptyList(),
        notifications: List<Notification> = emptyList(),
        onRemoveImage: (AdminImageItem, (Boolean) -> Unit) -> Unit = { _, callback ->
            callback(true)
        },
        onNotificationClick: (Notification) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                AdminLogsScreen(
                    campsites = campsites,
                    users = users,
                    notifications = notifications,
                    onRemoveImage = onRemoveImage,
                    onNotificationClick = onNotificationClick,
                    onBack = onBack
                )
            }
        }
    }

    @Test
    fun adminLogsScreen_displaysHeaderAndTabs() {
        setAdminLogsContent()

        composeTestRule.onNodeWithText("Review")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Images")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Notifications")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Messages")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Search...")
            .assertIsDisplayed()
    }

    @Test
    fun adminLogsScreen_backButtonCallsOnBack() {
        var backClicked = false

        setAdminLogsContent(
            onBack = { backClicked = true }
        )

        composeTestRule.onNodeWithText("← Back")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(true, backClicked)
        }
    }

    @Test
    fun adminLogsScreen_displaysMessagesTabContent() {
        setAdminLogsContent()

        composeTestRule.onNodeWithText("Messages")
            .performClick()

        composeTestRule.onNodeWithText(
            "Message logs will appear here."
        ).assertIsDisplayed()
    }

    @Test
    fun adminLogsScreen_displaysNotificationsWhenTabSelected() {
        setAdminLogsContent(
            users = listOf(createUser()),
            notifications = listOf(createNotification())
        )

        composeTestRule.onNodeWithText("Notifications")
            .performClick()

        composeTestRule.onNodeWithText("Lottery Results")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("To: Jane Doe * Unknown")
            .assertIsDisplayed()
    }

    @Test
    fun adminLogsScreen_notificationClickCallsCallback() {
        val notification = createNotification()
        var selectedNotification: Notification? = null

        setAdminLogsContent(
            users = listOf(createUser()),
            notifications = listOf(notification),
            onNotificationClick = {
                selectedNotification = it
            }
        )

        composeTestRule.onNodeWithText("Notifications")
            .performClick()

        composeTestRule.onNodeWithText("Lottery Results")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(notification, selectedNotification)
        }
    }

    @Test
    fun adminLogsScreen_searchFiltersNotificationsByTitle() {
        setAdminLogsContent(
            notifications = listOf(
                createNotification(
                    id = "1",
                    title = "Lottery Results"
                ),
                createNotification(
                    id = "2",
                    title = "Reservation Update"
                )
            )
        )

        composeTestRule.onNodeWithText("Notifications")
            .performClick()

        composeTestRule.onNodeWithText("Search...")
            .performTextInput("Lottery")

        composeTestRule.onNodeWithText("Lottery Results")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reservation Update")
            .assertDoesNotExist()
    }

    @Test
    fun adminLogsScreen_searchFiltersNotificationsByRecipientName() {
        setAdminLogsContent(
            users = listOf(
                createUser(name = "Jane Doe"),
                createUser(
                    userId = "user2",
                    name = "Alex Smith"
                )
            ),
            notifications = listOf(
                createNotification(
                    id = "1",
                    recipientId = "user1",
                    title = "First Notification"
                ),
                createNotification(
                    id = "2",
                    recipientId = "user2",
                    title = "Second Notification"
                )
            )
        )

        composeTestRule.onNodeWithText("Notifications")
            .performClick()

        composeTestRule.onNodeWithText("Search...")
            .performTextInput("Jane")

        composeTestRule.onNodeWithText("First Notification")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Second Notification")
            .assertDoesNotExist()
    }

    @Test
    fun adminLogsScreen_displaysImagesTabByDefault() {
        setAdminLogsContent()

        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Campsites")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Profiles")
            .assertIsDisplayed()
    }

    @Test
    fun adminLogsScreen_switchesBetweenImageFilters() {
        setAdminLogsContent()

        composeTestRule.onNodeWithText("Campsites")
            .performClick()

        composeTestRule.onNodeWithText("Profiles")
            .performClick()

        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()
    }
}
