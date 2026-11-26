package com.example.summit_climb.controller

import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerLotteryControllerTest {

    // stand-in repository that keeps lotteries in a list (no Firebase)
    private class FakeLotteryRepository : LotteryRepository {
        val saved = ArrayList<Lottery>()
        var updated: Lottery? = null

        override val lotteries: List<Lottery> get() = saved
        override fun add(lottery: Lottery) { saved.add(lottery) }
        override fun update(lottery: Lottery) { updated = lottery }
        override fun get(lotteryId: String): Lottery? = saved.find { it.id == lotteryId }
        override fun forOwner(ownerId: String): List<Lottery> = saved.filter { it.ownerId == ownerId }
        override fun forCampsite(campsiteId: String): List<Lottery> = saved.filter { campsiteId in it.campsiteIds }
        override fun setStatus(lotteryId: String, status: LotteryStatus) {}
        override fun join(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun leave(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun listenToEntries(lotteryId: String, onUpdate: (List<WaitlistEntry>) -> Unit): () -> Unit {
            onUpdate(listOf(WaitlistEntry(lotteryId, "entrant1")))
            return {}
        }
        override fun entriesFor(entrantId: String, onResult: (List<WaitlistEntry>) -> Unit) {}
        override fun setResult(lotteryId: String, entrantId: String, result: WaitlistResult) {}
    }

    // "now" is 1000, registration 2000–3000, booking 4000–5000
    private val now = 1000L
    private val valid = Lottery(
        id = "L1",
        ownerId = "owner1",
        campsiteIds = listOf("camp1"),
        registrationStart = 2000L,
        registrationEnd = 3000L,
        bookingStart = 4000L,
        bookingEnd = 5000L,
        winnerCount = 2,
        waitlistCap = 10
    )

    // validate tests

    @Test
    fun validLotteryHasNoProblems() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertNull(controller.validate(valid, now))
    }

    @Test
    fun noCampsitesIsInvalid() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("Pick at least one campsite",
            controller.validate(valid.copy(campsiteIds = emptyList()), now))
    }

    @Test
    fun registrationEndingBeforeItStartsIsInvalid() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("Registration must end after it starts",
            controller.validate(valid.copy(registrationEnd = 1500L), now))
    }

    @Test
    fun bookingStartingBeforeRegistrationEndsIsInvalid() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("Booking can't start before registration ends",
            controller.validate(valid.copy(bookingStart = 2500L), now))
    }

    @Test
    fun zeroWinnersIsInvalid() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("There must be at least one winner",
            controller.validate(valid.copy(winnerCount = 0), now))
    }

    @Test
    fun capSmallerThanWinnersIsInvalid() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("Waitlist limit can't be smaller than the number of winners",
            controller.validate(valid.copy(waitlistCap = 1), now))
    }

    @Test
    fun noCapIsAllowed() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertNull(controller.validate(valid.copy(waitlistCap = null), now))
    }

    // create lottery tests

    @Test
    fun createValidLotterySavesIt() {
        val repo = FakeLotteryRepository()
        var succeeded = false

        OwnerLotteryController(repo).createLottery(valid, { succeeded = true }, {}, now)

        assertTrue(succeeded)
        assertEquals(listOf(valid), repo.saved)
    }

    @Test
    fun createInvalidLotteryIsNotSaved() {
        val repo = FakeLotteryRepository()
        var error: String? = null

        OwnerLotteryController(repo).createLottery(valid.copy(winnerCount = 0), {}, { error = it.message }, now)

        assertEquals("There must be at least one winner", error)
        assertTrue(repo.saved.isEmpty())
    }

    // edit lottery tests

    @Test
    fun editBeforeRegistrationClosesSavesIt() {
        val repo = FakeLotteryRepository()
        repo.saved.add(valid)
        val edited = valid.copy(winnerCount = 3)
        var succeeded = false

        OwnerLotteryController(repo).editLottery(edited, { succeeded = true }, {}, now)

        assertTrue(succeeded)
        assertEquals(edited, repo.updated)
    }

    @Test
    fun editAfterRegistrationClosesIsRejected() {
        val repo = FakeLotteryRepository()
        repo.saved.add(valid)
        var error: String? = null

        OwnerLotteryController(repo).editLottery(valid, {}, { error = it.message }, now = 3500L)

        assertEquals("A lottery can't be edited after registration closes", error)
        assertNull(repo.updated)
    }

    @Test
    fun editUnknownLotteryIsRejected() {
        val repo = FakeLotteryRepository()
        var error: String? = null

        OwnerLotteryController(repo).editLottery(valid, {}, { error = it.message }, now)

        assertEquals("Lottery not found", error)
    }

    // lists, waitlist, status tests

    @Test
    fun lotteriesForOnlyReturnsThatOwnersLotteries() {
        val repo = FakeLotteryRepository()
        val other = valid.copy(id = "L2", ownerId = "owner2")
        repo.saved.addAll(listOf(valid, other))

        assertEquals(listOf(valid), OwnerLotteryController(repo).lotteriesFor("owner1"))
    }

    @Test
    fun listenToWaitlistPassesEntriesThrough() {
        var entrants = emptyList<String>()

        OwnerLotteryController(FakeLotteryRepository())
            .listenToWaitlist("L1") { entries -> entrants = entries.map { it.entrantId } }

        assertEquals(listOf("entrant1"), entrants)
    }

    @Test
    fun statusLabelFollowsTheDates() {
        val controller = OwnerLotteryController(FakeLotteryRepository())
        assertEquals("Upcoming", controller.statusLabel(valid, 1000L))
        assertEquals("Lottery Open", controller.statusLabel(valid, 2500L))
        assertEquals("Closed", controller.statusLabel(valid, 3500L))
    }
}