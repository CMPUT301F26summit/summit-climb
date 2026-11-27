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
                Log.d("ConversationDebug", "Saved message: ${document.id}")
                onSuccess() }
            .addOnFailureListener {
                Log.e("ConversationDebug", "Failed to save message", it)
                onFailure(it) }
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
            Log.d(
                "ConversationDebug",
                "Incoming: ${incoming.size}, Outgoing: ${outgoing.size}"
            )

            onMessages(
                (incoming + outgoing)
                    .distinctBy { it.id }
                    .sortedBy { it.timestamp }
            )
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

                incoming = snapshot?.documents
                    ?.mapNotNull {
                        it.toObject(Notification::class.java)
                            ?.copy(id = it.id)
                    }.orEmpty()

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

                outgoing = snapshot?.documents
                    ?.mapNotNull {
                        it.toObject(Notification::class.java)
                            ?.copy(id = it.id)
                    }.orEmpty()

                publishMessages()
            }

        return {
            incomingListener.remove()
            outgoingListener.remove()
        }
    }
}