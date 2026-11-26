package com.example.summit_climb.controller

import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import java.util.UUID

/**
 * Handles notifications sent by campsite owners.
 *
 * Uses NotificationRepository to send messages to entrants,
 * notify waitlist members, and retrieve sent notifications.
 */
class OwnerNotificationController(
    private val notificationRepository: NotificationRepository
) {

    // Sends a message from a campsite owner to one entrant.
    fun messageEntrant(
        ownerId: String,
        entrantId: String,
        campsiteId: String,
        message: String
    ) {
        require(ownerId.isNotBlank()) { "Owner ID cannot be empty" }
        require(entrantId.isNotBlank()) { "Entrant ID cannot be empty" }
        require(message.isNotBlank()) { "Message cannot be empty" }

        notificationRepository.send(
            Notification(
                senderId = ownerId,
                recipientId = entrantId,
                type = NotificationType.MESSAGE,
                title = "Message from campsite owner",
                text = message,
                campsiteId = campsiteId
            )
        )
    }

    /**
     * Sends the same notification to multiple waitlist entrants.
     *
     * Recipient IDs are provided by the waitlist feature.
     * Notifications share a batch ID to identify them as one message.
     */
    fun notifyWaitlist(
        ownerId: String,
        entrantIds: List<String>,
        campsiteId: String,
        message: String
    ) {
        require(ownerId.isNotBlank()) { "Owner ID cannot be empty" }
        require(message.isNotBlank()) { "Message cannot be empty" }
        require(entrantIds.all { it.isNotBlank() }) {
            "Entrant IDs cannot be empty"
        }

        val batchId = UUID.randomUUID().toString()

        entrantIds.distinct().forEach { entrantId ->
            notificationRepository.send(
                Notification(
                    senderId = ownerId,
                    recipientId = entrantId,
                    type = NotificationType.MESSAGE,
                    title = "Campsite waitlist update",
                    text = message,
                    campsiteId = campsiteId,
                    batchId = batchId
                )
            )
        }
    }

    // Retrieves notifications sent by a campsite owner, newest first.

    fun getSentNotifications(ownerId: String): List<Notification> {
        require(ownerId.isNotBlank()) { "Owner ID cannot be empty" }

        return notificationRepository.sentBy(ownerId)
    }
}