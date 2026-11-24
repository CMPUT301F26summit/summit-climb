
package com.example.summit_climb

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
// import androidx.compose.ui.test.assertDoesNotExist
// import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.ui.entrant.NotificationsScreen
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test

class NotificationsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeNotificationRepository : NotificationRepository {

        val sentNotifications = mutableListOf<Notification>()
        val markedReadIds = mutableListOf<String>()

        override val notifications: List<Notification>
            get() = sentNotifications

        override fun send(notification: Notification) {
            sentNotifications.add(notification)
        }

        override fun markRead(notificationId: String) {
            markedReadIds.add(notificationId)
        }

        override fun forUser(userId: String): List<Notification> {
            return sentNotifications.filter {
                it.recipientId == userId
            }
        }

        override fun sentBy(senderId: String): List<Notification> {
            return sentNotifications.filter {
                it.senderId == senderId
            }
        }

        override fun listenToUser(
            userId: String,
            onAdded: (Notification) -> Unit,
            onModified: (Notification) -> Unit,
            onRemoved: (Notification) -> Unit
        ): () -> Unit {
            return {}
        }
    }

    private fun createNotification(
        id: String,
        type: NotificationType = NotificationType.MESSAGE,
        read: Boolean = false
    ) = Notification(
        id = id,
        senderId = "system",
        recipientId = "test-user",
        type = type,
        title = "Test Notification",
        text = "This is a test notification.",
        timestamp = System.currentTimeMillis(),
        read = read
    )

    private fun createController(
        repository: FakeNotificationRepository,
        notifications: List<Notification> = emptyList()
    ): NotificationController {
        return NotificationController(repository) {
            "test-user"
        }.apply {
            this.notifications.addAll(notifications)
        }
    }

    private fun setUpScreen(
        controller: NotificationController,
        notificationsEnabled: Boolean = true,
        onEnableNotifications: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                NotificationsScreen(
                    controller = controller,
                    navController = rememberNavController(),
                    notificationsEnabled = notificationsEnabled,
                    onEnableNotifications = onEnableNotifications
                )
            }
        }
    }

    // Tests

    @Test
    fun disableNotification_showsDisabledScreen() {
        val repository = FakeNotificationRepository()
        val controller = createController(repository)

        setUpScreen(
            controller = controller,
            notificationsEnabled = false
        )

        composeTestRule
            .onNodeWithText("Notifications Disabled")
            .assertExists()

        composeTestRule
            .onNodeWithText(
                "You have opted out of notifications. You will not receive new notifications."
            )
            .assertExists()

        composeTestRule
            .onNodeWithText("Turn notifications back on")
            .assertExists()
    }

    @Test
    fun disableNotifications_enableButtonCallsCallBack() {
        val repository = FakeNotificationRepository()
        val controller = createController(repository)

        var callbackCalled = false

        setUpScreen(
            controller = controller,
            notificationsEnabled = false,
            onEnableNotifications = {
                callbackCalled = true
            }
        )

        composeTestRule
            .onNodeWithText("Turn notifications back on")
            .performClick()

        TestCase.assertEquals(true, callbackCalled)
    }

    @Test
    fun enabledNotifications_emptyListShowsEmptyMessage() {
        val repository = FakeNotificationRepository()
        val controller = createController(repository)

        setUpScreen(
            controller = controller,
            notificationsEnabled = true
        )

        composeTestRule
            .onNodeWithText("Notifications")
            .assertExists()

        composeTestRule
            .onNodeWithText("No notifications yet")
            .assertExists()

        composeTestRule
            .onNodeWithText("0 unread")
            .assertExists()
    }

    @Test
    fun notificationsExist_displayNotificationMessage() {
        val repository = FakeNotificationRepository()

        val notification = createNotification("1")

        val controller = createController(
            repository,
            listOf(notification)
        )

        setUpScreen(controller)

        composeTestRule
            .onNodeWithText("New Message")
            .assertExists()

        composeTestRule
            .onNodeWithText("This is a test notification.")
            .assertExists()
    }

    @Test
    fun unreadNotification_displaysNew() {
        val repository = FakeNotificationRepository()

        val notification = createNotification(
            "1",
            read = false
        )

        val controller = createController(
            repository,
            listOf(notification)
        )

        setUpScreen(controller)

        composeTestRule
            .onNodeWithText("NEW")
            .assertExists()

        composeTestRule
            .onNodeWithText("1 unread")
            .assertExists()
    }

    @Test
    fun readNotification_doesNotDisplayNewLabel() {
        val repository = FakeNotificationRepository()

        val notification = createNotification(
            "1",
            read = true
        )

        val controller = createController(
            repository,
            listOf(notification)
        )

        setUpScreen(controller = controller)

        composeTestRule
            .onNodeWithText("NEW")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("0 unread")
            .assertExists()
    }

    @Test
    fun multipleNotifications_displayCorrectUnreadCount() {
        val repository = FakeNotificationRepository()

        val notifications = listOf(
            createNotification("1", read = false),
            createNotification("2", read = false),
            createNotification("3", read = true)
        )

        val controller = createController(
            repository,
            notifications
        )

        setUpScreen(controller)

        composeTestRule
            .onNodeWithText("2 unread")
            .assertExists()
    }

    @Test
    fun clickingUnreadNotification_marksItAsRead() {
        val repository = FakeNotificationRepository()

        val notification = createNotification("n-1", read = false)

        val controller = createController(
            repository,
            listOf(notification)
        )

        setUpScreen(controller = controller)

        composeTestRule
            .onNodeWithText("This is a test notification.")
            .performClick()

        TestCase.assertEquals(
            listOf("n-1"),
            repository.markedReadIds
        )
    }

    @Test
    fun markAllAsRead_marksEveryUnreadNotification() {
        val repository = FakeNotificationRepository()

        val notifications = listOf(
            createNotification("1", read = false),
            createNotification("2", read = false),
            createNotification("3", read = true)
        )

        val controller = createController(
            repository,
            notifications
        )

        setUpScreen(controller)

        composeTestRule
            .onNodeWithText("Mark all as read")
            .performClick()

        TestCase.assertEquals(
            listOf("1", "2"),
            repository.markedReadIds
        )
    }

    @Test
    fun disabledNotificationsWithExistingNotifications_showsNotificationList() {
        val repository = FakeNotificationRepository()

        val notification = createNotification("1")

        val controller = createController(
            repository,
            listOf(notification)
        )

        setUpScreen(
            controller = controller,
            notificationsEnabled = false
        )

        composeTestRule
            .onNodeWithText("Notifications")
            .assertExists()

        composeTestRule
            .onNodeWithText("Notifications Disabled")
            .assertExists()

        composeTestRule
            .onNodeWithText("This is a test notification.")
            .assertExists()
    }

    @Test
    fun notificationTypes_displayCorrectHeaders() {
        val repository = FakeNotificationRepository()

        val notifications = listOf(
            createNotification("1", NotificationType.LOTTERY_WIN),
            createNotification("2", NotificationType.LOTTERY_LOSS),
            createNotification("3", NotificationType.RESERVATION_UPDATE),
            createNotification("4", NotificationType.MESSAGE)
        )

        val controller = createController(
            repository,
            notifications
        )

        setUpScreen(controller)

        composeTestRule
            .onNodeWithText("You won the lottery!")
            .assertExists()

        composeTestRule
            .onNodeWithText("Lottery Not Selected")
            .assertExists()

        composeTestRule
            .onNodeWithText("Reservation Notice")
            .assertExists()

        composeTestRule
            .onNodeWithText("New Message")
            .assertExists()
    }
}
