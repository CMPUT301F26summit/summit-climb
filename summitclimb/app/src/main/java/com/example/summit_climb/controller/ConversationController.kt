package com.example.summit_climb.controller

import com.example.summit_climb.data.MessageRepository
import com.example.summit_climb.model.Notification
import android.util.Log

class ConversationController(
    private val repository: MessageRepository = MessageRepository()
) {
    fun sendMessage(
        currentUserId: String,
        recipientId: String,
        campsiteId: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit)
    {
        repository.sendMessage(
            senderId = currentUserId,
            recipientId = recipientId,
            campsiteId = campsiteId,
            text = message,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getConversationMessages(
        currentUserId: String,
        recipientId: String,
        campsiteId: String,
        onMessages: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) : () -> Unit {
        return repository.getConversationMessages(
            currentUserId = currentUserId,
            recipientId = recipientId,
            campsiteId = campsiteId,
            onMessages = onMessages,
            onFailure = onFailure
        )
    }

    fun getUserMessages(
        currentUserId: String,
        onMessages: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) : () -> Unit {
        return repository.getUserMessages(
            currentUserId = currentUserId,
            onMessages = onMessages,
            onFailure = onFailure
        )
    }

    fun groupConversations(
        messages: List<Notification>,
        currentUserId: String
    ): List<Notification> {

        val latestMessages = mutableListOf<Notification>()

        val conversations = messages.groupBy { message ->
            val otherUserId = if (message.senderId == currentUserId) {
                message.recipientId
            } else {
                message.senderId
            }

            Pair(otherUserId, message.campsiteId)
        }

        for (conversation in conversations.values) {
            val latestMessage = conversation.maxByOrNull { it.timestamp }

            if (latestMessage != null) {
                latestMessages.add(latestMessage)
            }
        }

        return latestMessages.sortedByDescending { it.timestamp }
    }

    fun getAllMessages(
        onMessages: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ): () -> Unit {
        return repository.getAllMessages(onMessages, onFailure)
    }

    /**
     * Groups messages by campsite and participants for admin review.
     */
    fun groupAdminConversations(
        messages: List<Notification>
    ): List<List<Notification>> {
        val conversations = messages.groupBy { message ->
            val participants = listOf(
                message.senderId,
                message.recipientId
            ).sorted()

            Pair(message.campsiteId, participants)
        }

        val groupedMessages = mutableListOf<List<Notification>>()

        for (conversation in conversations.values) {
            val sortedMessages = conversation.sortedBy { it.timestamp }
            groupedMessages.add(sortedMessages)
        }

        return groupedMessages.sortedByDescending {
            it.last().timestamp
        }
    }
}