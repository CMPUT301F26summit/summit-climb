package com.example.summit_climb

import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationControllerTest {

    private class FakeRepository : NotificationRepository {

        override val notifications = mutableListOf<Notification>()

        var markedReadId = ""

        var listenedUserId = ""
        var listenerStopped = false

        private var onAdded: ((Notification) -> Unit)? = null
        private var onModified: ((Notification) -> Unit)? = null
        private var onRemoved: ((Notification) -> Unit)? = null

        override fun send(notification: Notification) {
            notifications.add(notification)
        }

        override fun markRead(notificationId: String) {
            markedReadId = notificationId
        }

        override fun forUser(userId: String): List<Notification> {
            return notifications.filter { it.recipientId == userId }
        }

        override fun sentBy(senderId: String): List<Notification> {
            return notifications.filter { it.senderId == senderId }
        }

        override fun listenToUser(
            userId: String,
            onAdded: (Notification) -> Unit,
            onModified: (Notification) -> Unit,
            onRemoved: (Notification) -> Unit
        ): () -> Unit {
            listenedUserId = userId
            this.onAdded = onAdded
            this.onModified = onModified
            this.onRemoved = onRemoved

            return {
                listenerStopped = true
            }
        }

        fun addNotification(notification: Notification) {
            onAdded?.invoke(notification)
        }

        fun modifyNotification(notification: Notification) {
            onModified?.invoke(notification)
        }

        fun removeNotification(notification: Notification) {
            onRemoved?.invoke(notification)
        }
    }

    @Test
    fun startListeningUsesCurrentUser() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        controller.startListening()

        assertEquals("user123", repository.listenedUserId)
    }

    @Test
    fun addedNotificationsAppearInList() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        controller.startListening()

        val notification = Notification(
            id = "notification1",
            recipientId = "user123",
            title = "Lottery result",
            text = "You won the lottery"
        )

        repository.addNotification(notification)

        assertEquals(1, controller.notifications.size)
        assertEquals("notification1", controller.notifications[0].id)
    }

    @Test
    fun unreadCountIsCorrect() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        controller.startListening()

        repository.addNotification(
            Notification(id = "1", read = false)
        )
        repository.addNotification(
            Notification(id = "2", read = true)
        )
        repository.addNotification(
            Notification(id = "3", read = false)
        )

        assertEquals(2, controller.unreadCount)
    }

    @Test
    fun markReadSendsCorrectNotificationId() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        controller.markRead("notification123")

        assertEquals("notification123", repository.markedReadId)
    }

    @Test
    fun removedNotificationDisappearsFromList() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        controller.startListening()

        val notification = Notification(id = "notification1")
        repository.addNotification(notification)
        repository.removeNotification(notification)

        assertEquals(0, controller.notifications.size)
    }

    @Test
    fun lotteryWinOpensEntrantLotteries() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        val notification = Notification(type = NotificationType.LOTTERY_WIN)

        assertEquals("entrantLotteries", controller.destinationFor(notification))
    }

    @Test
    fun reservationUpdateHasNoDestination() {
        val repository = FakeRepository()
        val controller = NotificationController(repository) { "user123" }

        val notification = Notification(type = NotificationType.RESERVATION_UPDATE)

        assertNull(controller.destinationFor(notification))
    }
}