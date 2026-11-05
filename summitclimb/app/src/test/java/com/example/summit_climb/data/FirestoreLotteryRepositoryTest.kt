package com.example.summit_climb.data

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**Purpose: tests FirestoreLotteryRepository without touching real Firestore.
 * Firestore is replaced with fake (mock) objects. We grab the snapshot listener the
 * repository registers, then send it a fake data as if Firestore had changed.
 */

class FirestoreLotteryRepositoryTest {

    private lateinit var db: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var repo: FirestoreLotteryRepository
    private lateinit var listener: EventListener<QuerySnapshot>

    private val lotteryA = Lottery(id = "A", ownerId = "owner1", campsiteIds = listOf("camp1", "camp2"))
    private val lotteryB = Lottery(id = "B", ownerId = "owner1", campsiteIds = listOf("camp3"))
    private val lotteryC = Lottery(id = "C", ownerId = "owner2", campsiteIds = listOf("camp1"))

    //runs before every test where we have a fake Firestore + a fresh repository
    @Before
    fun setUp() {
        db = mock()
        collection = mock()
        whenever(db.collection("lotteries")).thenReturn(collection)

        repo = FirestoreLotteryRepository(db)

        //catch the listener the repository registered
        val captor = argumentCaptor<EventListener<QuerySnapshot>>()
        verify(collection).addSnapshotListener(captor.capture())
        listener = captor.firstValue
    }

    // pretends Firestore sent these lotteries
    private fun sendFromFirestore(vararg lotteries: Lottery) {
        val docs = lotteries.map { lottery ->
            val doc: DocumentSnapshot = mock()
            whenever(doc.id).thenReturn(lottery.id)
            whenever(doc.toObject(Lottery::class.java)).thenReturn(lottery.copy(id = ""))
            doc
        }
        val snapshot: QuerySnapshot = mock()
        whenever(snapshot.documents).thenReturn(docs)
        listener.onEvent(snapshot, null)
    }


    //testing live list
    @Test
    fun startsEmpty() {
        assertTrue(repo.lotteries.isEmpty())
    }

    @Test
    fun snapshotFillsListAndCopiesDocumentIds() {
        sendFromFirestore(lotteryA, lotteryB)

        assertEquals(listOf(lotteryA, lotteryB), repo.lotteries)
    }

    @Test
    fun newSnapshotReplacesOldList() {
        sendFromFirestore(lotteryA, lotteryB)
        sendFromFirestore(lotteryC)

        assertEquals(listOf(lotteryC), repo.lotteries)
    }

    @Test
    fun errorKeepsOldList() {
        sendFromFirestore(lotteryA)
        listener.onEvent(
            null,
            FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE)
        )

        assertEquals(listOf(lotteryA), repo.lotteries)
    }


    //Testing looking up in repo
    @Test
    fun getFindsLotteryById() {
        sendFromFirestore(lotteryA, lotteryB)

        assertEquals(lotteryB, repo.get("B"))
    }

    @Test
    fun getUnknownIdReturnsNull() {
        sendFromFirestore(lotteryA)

        assertNull(repo.get("nope"))
    }

    @Test
    fun forOwnerOnlyReturnsThatOwnersLotteries() {
        sendFromFirestore(lotteryA, lotteryB, lotteryC)

        assertEquals(listOf(lotteryA, lotteryB), repo.forOwner("owner1"))
    }

    @Test
    fun forCampsiteFindsEveryLotteryIncludingIt() {
        sendFromFirestore(lotteryA, lotteryB, lotteryC)

        assertEquals(listOf(lotteryA, lotteryC), repo.forCampsite("camp1"))
    }


    //Testing writing in a repo
    @Test
    fun addSavesWithFirestoreGeneratedId() {
        val newDoc: DocumentReference = mock()
        whenever(collection.document()).thenReturn(newDoc)
        whenever(newDoc.id).thenReturn("generated123")

        repo.add(Lottery(ownerId = "owner1"))

        verify(newDoc).set(Lottery(id = "generated123", ownerId = "owner1"))
    }

    @Test
    fun updateOnlySavesOwnerEditableFields() {
        val docRef: DocumentReference = mock()
        whenever(collection.document("A")).thenReturn(docRef)

        repo.update(lotteryA)

        verify(docRef).set(
            eq(lotteryA),
            eq(
                SetOptions.mergeFields(
                    "campsiteIds", "registrationStart", "registrationEnd",
                    "bookingStart", "bookingEnd", "winnerCount", "waitlistCap"
                )
            )
        )
    }

    @Test
    fun setStatusOnlyChangesStatusField() {
        val docRef: DocumentReference = mock()
        whenever(collection.document("A")).thenReturn(docRef)

        repo.setStatus("A", LotteryStatus.DRAWN)

        verify(docRef).update("status", LotteryStatus.DRAWN)
    }
}