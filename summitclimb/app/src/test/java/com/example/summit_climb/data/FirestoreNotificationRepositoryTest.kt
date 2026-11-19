package com.example.summit_climb.data

import com.example.summit_climb.model.Notification
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.v1.Document
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FirestoreNotificationRepositoryTest {

    private data class TestSetup(
        val repository: FirestoreNotificationRepository,
        val userDocument: DocumentSnapshot,
        val userTask: Task<DocumentSnapshot>,
        val notificationDocument: DocumentReference
    )

    /** Creates  a mocked Firestore setup for repo tests
     *
     */

    private fun createSetup(): TestSetup {
        val db = mock<FirebaseFirestore>()

        val notificationsCollection = mock<CollectionReference>()
        val usersCollection = mock<CollectionReference>()

        val userDocumentReference = mock<DocumentReference>()
        val notificationDocument = mock<DocumentReference>()

        val userDocument = mock<DocumentSnapshot>()
        val userTask = mock<Task<DocumentSnapshot>>()

        whenever(db.collection("notifications"))
            .thenReturn(notificationsCollection)

        whenever(db.collection("users"))
            .thenReturn(usersCollection)

        whenever(usersCollection.document("recipient-123"))
            .thenReturn(userDocumentReference)

        whenever(userDocumentReference.get())
            .thenReturn(userTask)

        whenever(notificationsCollection.document())
            .thenReturn(notificationDocument)

        whenever(notificationDocument.id)
            .thenReturn("generated-notification-id")

        return TestSetup(
            repository = FirestoreNotificationRepository(db),
            userDocument = userDocument,
            userTask = userTask,
            notificationDocument = notificationDocument
        )
    }

    /**
     * Makes the mocked Firestore preference lookup succeed.
     */
    private fun simulateSuccessfulLookup(setup: TestSetup) {
        whenever(
            setup.userTask.addOnSuccessListener(
                any<OnSuccessListener<DocumentSnapshot>>()
            )
        ).thenAnswer { invocation ->
            val listener =
                invocation.getArgument<OnSuccessListener<DocumentSnapshot>>(0)

            listener.onSuccess(setup.userDocument)
            setup.userTask
        }

        whenever(
            setup.userTask.addOnFailureListener(any<OnFailureListener>())
        ).thenReturn(setup.userTask)
    }

    /**
     * Makes the mocked Firestore preference lookup fail.
     */
    private fun simulateFailedLookup(setup: TestSetup) {
        whenever(
            setup.userTask.addOnSuccessListener(
                any<OnSuccessListener<DocumentSnapshot>>()
            )
        ).thenReturn(setup.userTask)

        whenever(
            setup.userTask.addOnFailureListener(any<OnFailureListener>())
        ).thenAnswer { invocation ->
            val listener =
                invocation.getArgument<OnFailureListener>(0)

            listener.onFailure(Exception("Firestore lookup failed"))
            setup.userTask
        }
    }

    /**
     * Creates a sample notification for testing.
     */
    private fun createNotification(): Notification {
        return Notification(
            senderId = "system",
            recipientId = "recipient-123",
            title = "Lottery Result",
            text = "You won the lottery!"
        )
    }

    @Test
    fun send_doesNotSaveNotification_whenRecipientOptedOut() {
        val setup = createSetup()

        whenever(
            setup.userDocument.getBoolean("notificationsOptedOut")
        ).thenReturn(true)

        simulateSuccessfulLookup(setup)

        setup.repository.send(createNotification())

        verify(setup.notificationDocument, never())
            .set(any<Notification>())
    }

    @Test
    fun sendSavesNotification_whenRecipientHasNotOptedOut() {
        val setup = createSetup()

        whenever(
            setup.userDocument.getBoolean("notificationsOptedOut")
        ).thenReturn(false)

        simulateSuccessfulLookup(setup)

        val notification = createNotification()

        setup.repository.send(notification)

        verify(setup.notificationDocument)
            .set(notification.copy(id = "generated-notification-id"))
    }

    @Test
    fun sendDoesNotSaveNotification_whenPreferenceLookupFails() {
        val setup = createSetup()

        simulateFailedLookup(setup)

        setup.repository.send(createNotification())

        verify(setup.notificationDocument, never())
            .set(any<Notification>())
    }
}