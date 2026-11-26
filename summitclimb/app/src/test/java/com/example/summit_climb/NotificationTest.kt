package com.example.summit_climb

import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NotificationTest {

    @Test
    fun notificationHasCorrectDefaultValues() {
        val notification = Notification()

        assertEquals("", notification.id)
        assertEquals("", notification.senderId)
        assertEquals("", notification.recipientId)
        assertEquals(NotificationType.MESSAGE, notification.type)
        assertEquals("", notification.title)
        assertEquals("", notification.text)
        assertEquals("", notification.campsiteId)
        assertEquals("", notification.relatedId)
        assertEquals("", notification.batchId)
        assertFalse(notification.read)
    }

    @Test
    fun notificationStoresProvidedValues() {
        val notification = Notification(
            id = "notification1",
            senderId = "owner1",
            recipientId = "user1",
            type = NotificationType.LOTTERY_WIN,
            title = "Lottery Won",
            text = "You won the lottery!",
            campsiteId = "camp1",
            relatedId = "lottery1",
            batchId = "batch1",
            timestamp = 123456L,
            read = true
        )

        assertEquals("notification1", notification.id)
        assertEquals("owner1", notification.senderId)
        assertEquals("user1", notification.recipientId)
        assertEquals(NotificationType.LOTTERY_WIN, notification.type)
        assertEquals("Lottery Won", notification.title)
        assertEquals("You won the lottery!", notification.text)
        assertEquals("camp1", notification.campsiteId)
        assertEquals("lottery1", notification.relatedId)
        assertEquals("batch1", notification.batchId)
        assertEquals(123456L, notification.timestamp)
        assertEquals(true, notification.read)
    }

    @Test
    fun notificationCanBeCopiedWithUpdatedReadStatus() {
        val notification = Notification(
            id = "notification1",
            read = false
        )

        val updatedNotification = notification.copy(read = true)

        assertFalse(notification.read)
        assertEquals(true, updatedNotification.read)
        assertEquals(notification.id, updatedNotification.id)
    }
}