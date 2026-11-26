package com.example.summit_climb

import com.example.summit_climb.controller.EntrantLotteryController
import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Purpose: unit tests for [com.example.summit_climb.controller.EntrantLotteryController].
 * Uses [FakeLotteryRepository], an in-memory [LotteryRepository], so no Firestore is needed.
 */
class EntrantLotteryControllerTest {

    private val currentUserId = "device-123"
    private val oneDay = 24L * 60 * 60 * 1000

    private lateinit var repository: FakeLotteryRepository
    private lateinit var controller: EntrantLotteryController

    @Before
    fun setUp() {
        repository = FakeLotteryRepository()
        controller = EntrantLotteryController(repository, currentUserId)
    }

    //  test data 

    /** Registration open now (started 2 days ago, ends in 14 days), no cap. */
    private fun openLottery(id: String = "open-lottery"): Lottery {
        val now = System.currentTimeMillis()
        return Lottery(
            id = id,
            campsiteIds = listOf("cedar-valley"),
            registrationStart = now - 2 * oneDay,
            registrationEnd = now + 14 * oneDay,
            bookingStart = now + 15 * oneDay,
            bookingEnd = now + 45 * oneDay,
            winnerCount = 5,
            status = LotteryStatus.OPEN
        )
    }

    /** Registration ended 6 days ago and winners are drawn; booking is open now. */
    private fun drawnLottery(id: String = "drawn-lottery"): Lottery {
        val now = System.currentTimeMillis()
        return Lottery(
            id = id,
            campsiteIds = listOf("summit-camp"),
            registrationStart = now - 20 * oneDay,
            registrationEnd = now - 6 * oneDay,
            bookingStart = now - 5 * oneDay,
            bookingEnd = now + 20 * oneDay,
            winnerCount = 5,
            status = LotteryStatus.DRAWN
        )
    }

    /** Booking period ended yesterday. */
    private fun finishedLottery(id: String = "finished-lottery"): Lottery {
        val now = System.currentTimeMillis()
        return drawnLottery(id).copy(
            bookingStart = now - 10 * oneDay,
            bookingEnd = now - oneDay,
            status = LotteryStatus.CLOSED
        )
    }

    private fun entryFor(lottery: Lottery, result: WaitlistResult = WaitlistResult.WAITING): WaitlistEntry {
        return WaitlistEntry(
            lotteryId = lottery.id,
            entrantId = currentUserId,
            joinedAt = System.currentTimeMillis() - oneDay,
            result = result
        )
    }

    // getLottery

    @Test
    fun getLottery_returnsLotteryFromRepository() {
        val lottery = openLottery()
        repository.addLottery(lottery)

        assertEquals(lottery, controller.getLottery("open-lottery"))
    }

    @Test
    fun getLottery_unknownId_returnsNull() {
        assertNull(controller.getLottery("no-such-lottery"))
    }

    //  joinLottery

    @Test
    fun joinLottery_openLottery_joinsAndReportsNull() {
        var result: String? = "not called"

        controller.joinLottery(openLottery(), emptyList()) { problem -> result = problem }

        assertNull(result)
        assertEquals(listOf("open-lottery" to currentUserId), repository.joinCalls)
    }

    @Test
    fun joinLottery_alreadyJoined_isRejectedWithoutCallingRepository() {
        val lottery = openLottery()
        var result: String? = null

        controller.joinLottery(lottery, listOf(entryFor(lottery))) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_ALREADY_JOINED, result)
        assertTrue(repository.joinCalls.isEmpty())
    }

    @Test
    fun joinLottery_registrationEnded_isRejectedAsClosed() {
        var result: String? = null

        controller.joinLottery(drawnLottery(), emptyList()) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_CLOSED, result)
        assertTrue(repository.joinCalls.isEmpty())
    }

    @Test
    fun joinLottery_registrationNotStartedYet_isRejectedAsClosed() {
        val now = System.currentTimeMillis()
        val upcoming = openLottery().copy(registrationStart = now + oneDay, registrationEnd = now + 10 * oneDay)
        var result: String? = null

        controller.joinLottery(upcoming, emptyList()) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_CLOSED, result)
    }

    @Test
    fun joinLottery_datesOpenButAlreadyDrawn_isRejectedAsClosed() {
        val drawnEarly = openLottery().copy(status = LotteryStatus.DRAWN)
        var result: String? = null

        controller.joinLottery(drawnEarly, emptyList()) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_CLOSED, result)
    }

    @Test
    fun joinLottery_waitlistFull_isRejectedAsFull() {
        val full = openLottery().copy(waitlistCap = 2, entrantCount = 2)
        var result: String? = null

        controller.joinLottery(full, emptyList()) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_FULL, result)
        assertTrue(repository.joinCalls.isEmpty())
    }

    @Test
    fun joinLottery_repositoryFails_reportsJoinFailed() {
        repository.failNextCall = true
        var result: String? = null

        controller.joinLottery(openLottery(), emptyList()) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_JOIN_FAILED, result)
    }

    // leaveLottery 

    @Test
    fun leaveLottery_registrationOpen_leavesAndReportsNull() {
        val lottery = openLottery()
        var result: String? = "not called"

        controller.leaveLottery(lottery, entryFor(lottery)) { problem -> result = problem }

        assertNull(result)
        assertEquals(listOf("open-lottery" to currentUserId), repository.leaveCalls)
    }

    @Test
    fun leaveLottery_registrationClosed_isRejectedWithoutCallingRepository() {
        val lottery = drawnLottery()
        var result: String? = null

        controller.leaveLottery(lottery, entryFor(lottery)) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_CANNOT_LEAVE, result)
        assertTrue(repository.leaveCalls.isEmpty())
    }

    @Test
    fun leaveLottery_repositoryFails_reportsLeaveFailed() {
        val lottery = openLottery()
        repository.failNextCall = true
        var result: String? = null

        controller.leaveLottery(lottery, entryFor(lottery)) { problem -> result = problem }

        assertEquals(EntrantLotteryController.MESSAGE_LEAVE_FAILED, result)
    }

    //  getMyLotteries 

    @Test
    fun getMyLotteries_returnsOnlyCurrentUsersEntries() {
        val lottery = openLottery()
        repository.entries.add(entryFor(lottery))
        repository.entries.add(WaitlistEntry(lotteryId = lottery.id, entrantId = "someone-else"))
        var myEntries: List<WaitlistEntry> = emptyList()

        controller.getMyLotteries { entries -> myEntries = entries }

        assertEquals(1, myEntries.size)
        assertEquals(currentUserId, myEntries[0].entrantId)
    }

    @Test
    fun getMyLotteries_nothingJoined_returnsEmptyList() {
        var myEntries: List<WaitlistEntry>? = null

        controller.getMyLotteries { entries -> myEntries = entries }

        assertEquals(emptyList<WaitlistEntry>(), myEntries)
    }

    //  getWonLotteriesInBookingPeriod 

    @Test
    fun getWonLotteriesInBookingPeriod_keepsOnlyWinsStillOpenForBooking() {
        val wonOpen = drawnLottery("won-open")
        val wonFinished = finishedLottery("won-finished")
        val lost = drawnLottery("lost")
        repository.addLottery(wonOpen)
        repository.addLottery(wonFinished)
        repository.addLottery(lost)
        val myEntries = listOf(
            entryFor(wonOpen, WaitlistResult.WON),
            entryFor(wonFinished, WaitlistResult.WON),
            entryFor(lost, WaitlistResult.LOST)
        )

        val wins = controller.getWonLotteriesInBookingPeriod(myEntries)

        assertEquals(listOf(wonOpen), wins)
    }

    @Test
    fun getWonLotteriesInBookingPeriod_skipsLotteriesThatNoLongerExist() {
        val deleted = drawnLottery("deleted")
        val myEntries = listOf(entryFor(deleted, WaitlistResult.WON))

        val wins = controller.getWonLotteriesInBookingPeriod(myEntries)

        assertTrue(wins.isEmpty())
    }

    //  declineWonLottery 

    @Test
    fun declineWonLottery_wonDuringBooking_setsDeclinedAndReturnsNull() {
        val lottery = drawnLottery()

        val result = controller.declineWonLottery(lottery, entryFor(lottery, WaitlistResult.WON))

        assertNull(result)
        assertEquals(
            listOf(Triple("drawn-lottery", currentUserId, WaitlistResult.DECLINED)),
            repository.setResultCalls
        )
    }

    @Test
    fun declineWonLottery_entryNotWon_isRejected() {
        val lottery = drawnLottery()

        val result = controller.declineWonLottery(lottery, entryFor(lottery, WaitlistResult.LOST))

        assertEquals(EntrantLotteryController.MESSAGE_CANNOT_DECLINE, result)
        assertTrue(repository.setResultCalls.isEmpty())
    }

    @Test
    fun declineWonLottery_afterBookingPeriod_isRejected() {
        val lottery = finishedLottery()

        val result = controller.declineWonLottery(lottery, entryFor(lottery, WaitlistResult.WON))

        assertEquals(EntrantLotteryController.MESSAGE_CANNOT_DECLINE, result)
    }

    //  companion object rules 

    @Test
    fun hasJoined_trueOnlyForMatchingLottery() {
        val lottery = openLottery()
        val other = openLottery("other-lottery")

        assertTrue(EntrantLotteryController.hasJoined(lottery, listOf(entryFor(lottery))))
        assertFalse(EntrantLotteryController.hasJoined(lottery, listOf(entryFor(other))))
        assertFalse(EntrantLotteryController.hasJoined(lottery, emptyList()))
    }

    @Test
    fun isWaitlistFull_checksCap() {
        assertTrue(EntrantLotteryController.isWaitlistFull(Lottery(waitlistCap = 50, entrantCount = 50)))
        assertFalse(EntrantLotteryController.isWaitlistFull(Lottery(waitlistCap = 50, entrantCount = 49)))
        assertFalse(EntrantLotteryController.isWaitlistFull(Lottery(waitlistCap = null, entrantCount = 300)))
    }

    @Test
    fun getJoinRejectionReason_usesGivenTime() {
        val lottery = Lottery(registrationStart = 1_000L, registrationEnd = 2_000L, status = LotteryStatus.OPEN)

        assertNull(EntrantLotteryController.getJoinRejectionReason(lottery, alreadyJoined = false, now = 1_500L))
        assertEquals(
            EntrantLotteryController.MESSAGE_CLOSED,
            EntrantLotteryController.getJoinRejectionReason(lottery, alreadyJoined = false, now = 2_000L)
        )
        assertEquals(
            EntrantLotteryController.MESSAGE_ALREADY_JOINED,
            EntrantLotteryController.getJoinRejectionReason(lottery, alreadyJoined = true, now = 1_500L)
        )
    }

    @Test
    fun filterWonLotteriesInBookingPeriod_usesGivenTime() {
        val lottery = Lottery(id = "l1", bookingStart = 1_000L, bookingEnd = 2_000L)
        val won = listOf(
            WaitlistEntry(
                lotteryId = "l1",
                entrantId = currentUserId,
                result = WaitlistResult.WON
            )
        )

        val during = EntrantLotteryController.filterWonLotteriesInBookingPeriod(won, listOf(lottery), now = 1_500L)
        val after = EntrantLotteryController.filterWonLotteriesInBookingPeriod(won, listOf(lottery), now = 2_500L)

        assertEquals(listOf(lottery), during)
        assertTrue(after.isEmpty())
    }
}

/**
 * In memory [LotteryRepository] for unit tests.
 */
class FakeLotteryRepository : LotteryRepository {

    private val storedLotteries = mutableListOf<Lottery>()

    /** Waitlist entries returned by [entriesFor]. */
    val entries = mutableListOf<WaitlistEntry>()

    /** Each (lotteryId, entrantId) passed to [join]. */
    val joinCalls = mutableListOf<Pair<String, String>>()

    /** Each (lotteryId, entrantId) passed to [leave]. */
    val leaveCalls = mutableListOf<Pair<String, String>>()

    /** Each (lotteryId, entrantId, result) passed to [setResult]. */
    val setResultCalls = mutableListOf<Triple<String, String, WaitlistResult>>()

    /** When true, the next join or leave calls onFailure instead of onSuccess. */
    var failNextCall = false

    fun addLottery(lottery: Lottery) {
        storedLotteries.add(lottery)
    }

    override val lotteries: List<Lottery>
        get() = storedLotteries

    override fun add(lottery: Lottery) {
        storedLotteries.add(lottery)
    }

    override fun update(lottery: Lottery) {
        storedLotteries.removeAll { stored -> stored.id == lottery.id }
        storedLotteries.add(lottery)
    }

    override fun get(lotteryId: String): Lottery? {
        return storedLotteries.find { lottery -> lottery.id == lotteryId }
    }

    override fun forOwner(ownerId: String): List<Lottery> {
        return storedLotteries.filter { lottery -> lottery.ownerId == ownerId }
    }

    override fun forCampsite(campsiteId: String): List<Lottery> {
        return storedLotteries.filter { lottery -> campsiteId in lottery.campsiteIds }
    }

    override fun setStatus(lotteryId: String, status: LotteryStatus) {
        val lottery = get(lotteryId) ?: return
        update(lottery.copy(status = status))
    }

    override fun join(
        lotteryId: String,
        entrantId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        joinCalls.add(lotteryId to entrantId)
        reportResult(onSuccess, onFailure)
    }

    override fun leave(
        lotteryId: String,
        entrantId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        leaveCalls.add(lotteryId to entrantId)
        reportResult(onSuccess, onFailure)
    }

    override fun listenToEntries(lotteryId: String, onUpdate: (List<WaitlistEntry>) -> Unit): () -> Unit {
        onUpdate(entries.filter { entry -> entry.lotteryId == lotteryId })
        return {}
    }

    override fun entriesFor(entrantId: String, onResult: (List<WaitlistEntry>) -> Unit) {
        onResult(entries.filter { entry -> entry.entrantId == entrantId })
    }

    override fun setResult(lotteryId: String, entrantId: String, result: WaitlistResult) {
        setResultCalls.add(Triple(lotteryId, entrantId, result))
    }

    private fun reportResult(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (failNextCall) {
            failNextCall = false
            onFailure(Exception("Simulated Firestore failure"))
            return
        }
        onSuccess()
    }
}
