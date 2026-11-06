package com.example.summit_climb

import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.controller.OwnerNotificationController
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

class OwnerNotificationTest {

    private class FakeNotificationRepository : NotificationRepository {
        val sentNotifications = mutableListOf<Notification>()

        override val notifications: List<Notification>
            get() = sentNotifications

        override fun send(notification: Notification) {
            sentNotifications.add(notification)
        }

        override fun markRead(notificationId: String) {
            // Not needed for these tests
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

    @Test
    fun messageEntrantSendsCorrectNotification() {
        val repository = FakeNotificationRepository()
        val controller = OwnerNotificationController(repository)

        controller.messageEntrant(
            ownerId = "owner1",
            entrantId = "entrant1",
            campsiteId = "camp1",
            message = "Your reservation has been updated."
        )

        assertEquals(1, repository.sentNotifications.size)

        val notification = repository.sentNotifications.first()

        assertEquals("owner1", notification.senderId)
        assertEquals("entrant1", notification.recipientId)
        assertEquals("camp1", notification.campsiteId)
        assertEquals(
            NotificationType.MESSAGE,
            notification.type
        )
        assertEquals(
            "Your reservation has been updated.",
            notification.text
        )
    }

    @Test
    fun notifyWaitlistSendsToUniqueEntrants() {
        val repository = FakeNotificationRepository()
        val controller = OwnerNotificationController(repository)

        controller.notifyWaitlist(
            ownerId = "owner1",
            entrantIds = listOf("entrant1", "entrant2", "entrant1"),
            campsiteId = "camp1",
            message = "The lottery closes tomorrow."
        )

        assertEquals(2, repository.sentNotifications.size)

        val recipientIds = repository.sentNotifications.map {
            it.recipientId
        }

        assertEquals(
            setOf("entrant1", "entrant2"),
            recipientIds.toSet()
        )

        val batchIds = repository.sentNotifications.map {
            it.batchId
        }

        assertEquals(1, batchIds.distinct().size)
        assertTrue(batchIds.first().isNotBlank())
    }

    @Test
    fun getSentNotificationsReturnsOnlyOwnersMessages() {
        val repository = FakeNotificationRepository()
        val controller = OwnerNotificationController(repository)

        controller.messageEntrant(
            ownerId = "owner1",
            entrantId = "entrant1",
            campsiteId = "camp1",
            message = "Hello from owner 1."
        )

        controller.messageEntrant(
            ownerId = "owner2",
            entrantId = "entrant2",
            campsiteId = "camp2",
            message = "Hello from owner 2."
        )

        val results = controller.getSentNotifications("owner1")

        assertEquals(1, results.size)
        assertEquals("owner1", results.first().senderId)
        assertEquals("Hello from owner 1.", results.first().text)
    }

    @Test
    fun messageEntrantRejectsBlankMessage() {
        val repository = FakeNotificationRepository()
        val controller = OwnerNotificationController(repository)

        try {
            controller.messageEntrant(
                ownerId = "owner1",
                entrantId = "entrant1",
                campsiteId = "camp1",
                message = ""
            )

            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(repository.sentNotifications.isEmpty())
        }
    }
}