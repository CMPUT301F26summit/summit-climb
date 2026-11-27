package com.example.summit_climb.data

import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

/**
 * Stores private messages and retrieves conversation
 * history between two users for a specific campsite.
 */
class MessageRepository(
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {
    private val messagesRef = db.collection("messages")

    fun sendMessage(
        senderId: String,
        recipientId: String,
        campsiteId: String,
        text: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (
            FirebaseAuth.getInstance().currentUser?.uid != senderId ||
            recipientId.isBlank() ||
            campsiteId.isBlank() ||
            text.isBlank()
        ) {
            onFailure(Exception("Invalid message"))
            return
        }

        val document = messagesRef.document()

        val message = Notification(
            id = document.id,
            senderId = senderId,
            recipientId = recipientId,
            campsiteId = campsiteId,
            type = NotificationType.MESSAGE,
            title = "Private message",
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            read = false
        )

        document.set(message)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    /**
     * Listens to incoming and outgoing messages.
     * Returns a function that removes both listeners.
     */
    fun getConversationMessages(
        currentUserId: String,
        recipientId: String,
        campsiteId: String,
        onMessages: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ): () -> Unit {
        var incoming = emptyList<Notification>()
        var outgoing = emptyList<Notification>()

        fun publishMessages() {
            val allMessages = incoming + outgoing
            val uniqueMessages = allMessages.distinctBy { it.id }
            val sortedMessages = uniqueMessages.sortedByDescending { it.timestamp }

            onMessages(sortedMessages)
        }

        val incomingListener = messagesRef
            .whereEqualTo("senderId", recipientId)
            .whereEqualTo("recipientId", currentUserId)
            .whereEqualTo("campsiteId", campsiteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConversationDebug", "Message retrieval failed", error)
                    onFailure(error)
                    return@addSnapshotListener
                }

                val messages = mutableListOf<Notification>()

                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val message = document.toObject(Notification::class.java)

                        if (message != null) {
                            messages.add(message.copy(id = document.id))
                        }
                    }
                }
                incoming = messages
                publishMessages()
            }

        val outgoingListener = messagesRef
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("recipientId", recipientId)
            .whereEqualTo("campsiteId", campsiteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val messages = mutableListOf<Notification>()

                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val message = document.toObject(Notification::class.java)

                        if (message != null) {
                            messages.add(message.copy(id = document.id))
                        }
                    }
                }
                incoming = messages
                publishMessages()
            }

        return {
            incomingListener.remove()
            outgoingListener.remove()
        }
    }

    // Gets all messages sent or received by the current user
    fun getUserMessages(
        currentUserId: String,
        onMessages: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ): () -> Unit {

    var incoming = emptyList<Notification>()
    var outgoing = emptyList<Notification>()

    // Combine messages and remove duplicates
    fun publishMessages() {
        val allMessages = incoming + outgoing
        val uniqueMessages = allMessages.distinctBy { it.id }
        val sortedMessages = uniqueMessages.sortedByDescending { it.timestamp }

        onMessages(sortedMessages)
    }

    // Get received messages
    val incomingListener = messagesRef
        .whereEqualTo("recipientId", currentUserId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onFailure(error)
                return@addSnapshotListener
            }

            val messages = mutableListOf<Notification>()

            if (snapshot != null) {
                for (document in snapshot.documents) {
                    val message = document.toObject(Notification::class.java)

                    if (message != null) {
                        messages.add(message.copy(id = document.id))
                    }
                }
            }

            incoming = messages
            publishMessages()
        }

    // Get sent messages
    val outgoingListener = messagesRef
        .whereEqualTo("senderId", currentUserId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onFailure(error)
                return@addSnapshotListener
            }

            val messages = mutableListOf<Notification>()

            if (snapshot != null) {
                for (document in snapshot.documents) {
                    val message = document.toObject(Notification::class.java)

                    if (message != null) {
                        messages.add(message.copy(id = document.id))
                    }
                }
            }

            outgoing = messages
            publishMessages()
        }

        // Stop listening when leaving the screen
        return {
            incomingListener.remove()
            outgoingListener.remove()
        }
    }
}