
package com.example.summit_climb

import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NotificationControllerTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var controller: NotificationController

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
        controller = NotificationController(repository) { "user1" }
    }

    @Test
    fun startListening_addsNotificationsNewestFirst() {
        controller.startListening()

        repository.emitAdded(notification("old", 100))
        repository.emitAdded(notification("new", 200))

        assertEquals(
            listOf("new", "old"),
            controller.notifications.map { it.id }
        )
        assertEquals(2, controller.unreadCount)
    }

    @Test
    fun markRead_updatesUnreadCount() {
        controller.startListening()
        repository.emitAdded(notification("notification1", 100))

        assertEquals(1, controller.unreadCount)

        controller.markRead("notification1")

        assertEquals(0, controller.unreadCount)
    }

    @Test
    fun modifiedAndRemovedNotifications_updateInbox() {
        controller.startListening()

        repository.emitAdded(notification("notification1", 100))

        repository.emitModified(
            notification("notification1", 100).copy(text = "Updated message")
        )

        assertEquals(
            "Updated message",
            controller.notifications.first().text
        )

        repository.emitRemoved("notification1")

        assertEquals(0, controller.notifications.size)
    }

    @Test
    fun destinationFor_returnsCorrectScreen() {
        assertEquals(
            "entrantLotteries",
            controller.destinationFor(
                notification("win", 100).copy(type = NotificationType.LOTTERY_WIN)
            )
        )

        assertEquals(
            "entrantLotteries",
            controller.destinationFor(
                notification("loss", 100).copy(type = NotificationType.LOTTERY_LOSS)
            )
        )

        assertNull(
            controller.destinationFor(
                notification("reservation", 100)
                    .copy(type = NotificationType.RESERVATION_UPDATE)
            )
        )

        assertNull(
            controller.destinationFor(
                notification("message", 100)
                    .copy(type = NotificationType.MESSAGE)
            )
        )
    }

    private fun notification(id: String, timestamp: Long) = Notification(
        id = id,
        senderId = "owner1",
        recipientId = "user1",
        type = NotificationType.MESSAGE,
        title = "Test notification",
        text = "Test message",
        timestamp = timestamp
    )
}

/** Fake repository used to test the controller without Firebase. */
private class FakeNotificationRepository : NotificationRepository {

    private val storedNotifications = mutableListOf<Notification>()

    override val notifications: List<Notification>
        get() = storedNotifications

    private var onAdded: ((Notification) -> Unit)? = null
    private var onModified: ((Notification) -> Unit)? = null
    private var onRemoved: ((Notification) -> Unit)? = null

    override fun send(notification: Notification) {
        storedNotifications.add(notification)
    }

    override fun markRead(notificationId: String) {
        val index = storedNotifications.indexOfFirst { it.id == notificationId }

        if (index >= 0) {
            val updated = storedNotifications[index].copy(read = true)
            storedNotifications[index] = updated
            onModified?.invoke(updated)
        }
    }

    override fun forUser(userId: String): List<Notification> =
        storedNotifications.filter { it.recipientId == userId }

    override fun sentBy(senderId: String): List<Notification> =
        storedNotifications.filter { it.senderId == senderId }

    override fun listenToUser(
        userId: String,
        onAdded: (Notification) -> Unit,
        onModified: (Notification) -> Unit,
        onRemoved: (Notification) -> Unit
    ): () -> Unit {
        this.onAdded = onAdded
        this.onModified = onModified
        this.onRemoved = onRemoved

        return {
            this.onAdded = null
            this.onModified = null
            this.onRemoved = null
        }
    }

    fun emitAdded(notification: Notification) {
        storedNotifications.add(notification)
        onAdded?.invoke(notification)
    }

    fun emitModified(notification: Notification) {
        val index = storedNotifications.indexOfFirst { it.id == notification.id }

        if (index >= 0) {
            storedNotifications[index] = notification
        }

        onModified?.invoke(notification)
    }

    fun emitRemoved(notificationId: String) {
        storedNotifications.removeAll { it.id == notificationId }

        onRemoved?.invoke(
            Notification(id = notificationId)
        )
    }
}
