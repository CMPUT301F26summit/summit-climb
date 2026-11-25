package com.example.summit_climb.controller

import com.example.summit_climb.data.MessageRepository
import com.example.summit_climb.model.Notification

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

}