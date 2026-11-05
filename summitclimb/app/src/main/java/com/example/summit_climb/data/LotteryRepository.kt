package com.example.summit_climb.data

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult

// Everything the app can do with lotteries.
interface LotteryRepository {

    // all lotteries, updates by itself when Firestore changes
    val lotteries: List<Lottery>

    // creates a new lottery in Firestore. Firestore makes the ID
    fun add(lottery: Lottery)

    // saves an owner's edits to a lottery (does not touch entrantCount or status)
    fun update(lottery: Lottery)

    // one lottery by its ID or null if it doesn't exist
    fun get(lotteryId: String): Lottery?

    // all lotteries created by one owner
    fun forOwner(ownerId: String): List<Lottery>

    // all lotteries that include one campsite
    fun forCampsite(campsiteId: String): List<Lottery>

    // changes a lottery's stage used by the draw service
    fun setStatus(lotteryId: String, status: LotteryStatus)

    // adds an entrant to a lottery's waitlist making onDone(true) if they're on it
    fun join(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // removes an entrant from the waitlist only while registration is open, onDone(true) if removed
    fun leave(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // live list of one lottery's waitlist and returns a function that stops listening
    fun listenToEntries(lotteryId: String, onUpdate: (List<WaitlistEntry>) -> Unit): () -> Unit

    // every lottery entry one entrant has their lotteries and results
    fun entriesFor(entrantId: String, onResult: (List<WaitlistEntry>) -> Unit)

    // sets an entry's result
    fun setResult(lotteryId: String, entrantId: String, result: WaitlistResult)
}