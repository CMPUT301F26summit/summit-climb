package com.example.summit_climb.data

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus

// Everything the app can do with lotteries.
interface LotteryRepository {

    // all lotteries, updates by itself when Firestore changes
    val lotteries: List<Lottery>

    // creates a new lottery in Firestore. Firestore makes the ID
    fun add(lottery: Lottery)

    // saves an owner's edits to a lottery (does not touch entrantCount or status)
    fun update(lottery: Lottery)

    // one lottery by its ID, or null if it doesn't exist
    fun get(lotteryId: String): Lottery?

    // all lotteries created by one owner
    fun forOwner(ownerId: String): List<Lottery>

    // all lotteries that include one campsite
    fun forCampsite(campsiteId: String): List<Lottery>

    // changes a lottery's stage (used by the draw service)
    fun setStatus(lotteryId: String, status: LotteryStatus)
}