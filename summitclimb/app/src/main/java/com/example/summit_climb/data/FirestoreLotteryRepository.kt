package com.example.summit_climb.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Purpose: saves and loads lotteries using the Firestore "lotteries" collection.
 * Entrants are stored in each lottery's "entries" subcollection (added later).
 * Known problems:
 * It downloads ALL lotteries, then filters on the phone.
 * It starts watching Firestore when created and never stops.
 */

//Firestore version of LotteryRepository.
class FirestoreLotteryRepository(db: FirebaseFirestore) : LotteryRepository{

    private val lotteriesRef = db.collection("lotteries")

    // local copy of the lotteries
    private val _lotteries = mutableStateListOf<Lottery>()

    override val lotteries: List<Lottery>
        get() = _lotteries

    // runs once when the repository is created, starts watching Firestore
    init {
        lotteriesRef.addSnapshotListener { snapshot, error ->

            // something went wrong: print the error to Logcat and stop
            if (error != null) {
                Log.e("LotteryRepo", "Listen failed", error)
                return@addSnapshotListener
            }
            // otherwise replace local copy with the newest data from Firestore
            if (snapshot != null) {
                val fresh = snapshot.documents.mapNotNull { document ->
                    document.toObject(Lottery::class.java)
                        ?.copy(id = document.id)
                }

                _lotteries.clear()
                _lotteries.addAll(fresh)
            }
        }
    }

    // creates a new lottery. Firestore makes the ID
    override fun add(lottery: Lottery) {
        val doc = lotteriesRef.document()
        doc.set(lottery.copy(id = doc.id))
    }

    // saves only the fields an owner can edit
    override fun update(lottery: Lottery) {
        lotteriesRef.document(lottery.id).set(
            lottery,
            SetOptions.mergeFields(
                "campsiteIds",
                "registrationStart",
                "registrationEnd",
                "bookingStart",
                "bookingEnd",
                "winnerCount",
                "waitlistCap"
            )
        )
    }

    // one lottery by its ID
    override fun get(lotteryId: String): Lottery? =
        _lotteries.find { it.id == lotteryId }

    // lotteries created by this owner
    override fun forOwner(ownerId: String): List<Lottery> =
        _lotteries.filter { it.ownerId == ownerId }

    // lotteries that include this campsite
    override fun forCampsite(campsiteId: String): List<Lottery> =
        _lotteries.filter { campsiteId in it.campsiteIds }

    // changes the lottery's stage
    override fun setStatus(lotteryId: String, status: LotteryStatus) {
        lotteriesRef.document(lotteryId).update("status", status)
    }
}