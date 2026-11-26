package com.example.summit_climb.service

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import org.junit.Assert.assertEquals

class LotteryDrawServiceTest {

    private val service = LotteryDrawService(Random(42))
    private val entrants = listOf("a", "b", "c", "d", "e")

    // picking winners
    @Test
    fun picksExactlyWinnerCount() {
        val result = service.pickWinners(entrants, 2)
        assertEquals(2, result.winners.size)
        assertEquals(3, result.losers.size)
    }

    @Test
    fun everyEntrantIsWinnerOrLoserExactlyOnce() {
        val result = service.pickWinners(entrants, 2)
        assertEquals(entrants.sorted(), (result.winners + result.losers).sorted())
    }

    @Test
    fun fewerEntrantsThanWinnerCountEveryoneWins() {
        val result = service.pickWinners(listOf("a", "b"), 5)
        assertEquals(listOf("a", "b"), result.winners.sorted())
        assertTrue(result.losers.isEmpty())
    }

    @Test
    fun noEntrantsMeansNoWinners() {
        val result = service.pickWinners(emptyList(), 3)
        assertTrue(result.winners.isEmpty())
        assertTrue(result.losers.isEmpty())
    }

    @Test
    fun zeroWinnerCountEveryoneLoses() {
        val result = service.pickWinners(entrants, 0)
        assertTrue(result.winners.isEmpty())
        assertEquals(5, result.losers.size)
    }

    @Test
    fun duplicateEntrantCountedOnce() {
        val result = service.pickWinners(listOf("a", "a", "b"), 2)
        assertEquals(listOf("a", "b"), result.winners.sorted())
    }

    @Test
    fun sameSeedGivesSameDraw() {
        val first = LotteryDrawService(Random(7)).pickWinners(entrants, 2)
        val second = LotteryDrawService(Random(7)).pickWinners(entrants, 2)
        assertEquals(first, second)
    }

    // due for draw tests
    private val lottery = Lottery(registrationEnd = 2000L, status = LotteryStatus.OPEN)

    @Test
    fun notDueBeforeRegistrationEnds() {
        assertFalse(service.isDueForDraw(lottery, 1999L))
    }

    @Test
    fun dueExactlyWhenRegistrationEnds() {
        assertTrue(service.isDueForDraw(lottery, 2000L))
    }

    @Test
    fun alreadyDrawnIsNotDueAgain() {
        assertFalse(service.isDueForDraw(lottery.copy(status = LotteryStatus.DRAWN), 5000L))
    }

    // a lottery whose registration ended at 1000, checked at 2000
    private fun dueLottery(status: LotteryStatus = LotteryStatus.OPEN) = Lottery(
        id = "L1",
        campsiteIds = listOf("C1"),
        registrationStart = 0L,
        registrationEnd = 1000L,
        winnerCount = 2,
        status = status
    )

    private val threeEntries = listOf(
        WaitlistEntry("L1", "a"),
        WaitlistEntry("L1", "b"),
        WaitlistEntry("L1", "c")
    )

    @Test
    fun winnerNotificationIsLotteryWin() {
        val service = LotteryDrawService(Random(42))
        val notification = service.resultNotification(dueLottery(), "a", true)

        assertEquals(NotificationType.LOTTERY_WIN, notification.type)
        assertEquals("a", notification.recipientId)
        assertEquals("system", notification.senderId)
        assertEquals("L1", notification.relatedId)
        assertEquals("C1", notification.campsiteId)
    }

    @Test
    fun loserNotificationIsLotteryLoss() {
        val service = LotteryDrawService(Random(42))
        val notification = service.resultNotification(dueLottery(), "b", false)

        assertEquals(NotificationType.LOTTERY_LOSS, notification.type)
        assertEquals("b", notification.recipientId)
    }

    @Test
    fun runDueDrawsSkipsLotteryStillInRegistration() {
        val lotteries = FakeDrawLotteryRepository(threeEntries)
        val notifications = FakeDrawNotificationRepository()
        val notDue = dueLottery().copy(registrationEnd = 5000L)

        LotteryDrawService(Random(42)).runDueDraws(listOf(notDue), lotteries, notifications, now = 2000L)

        assertEquals(0, lotteries.claimCalls)
        assertEquals(0, notifications.sent.size)
    }

    @Test
    fun runDueDrawsSkipsLotteryAlreadyDrawn() {
        val lotteries = FakeDrawLotteryRepository(threeEntries)
        val notifications = FakeDrawNotificationRepository()

        LotteryDrawService(Random(42)).runDueDraws(listOf(dueLottery(LotteryStatus.DRAWN)), lotteries, notifications, now = 2000L)

        assertEquals(0, lotteries.claimCalls)
        assertEquals(0, notifications.sent.size)
    }

    @Test
    fun runDueDrawsMarksWinnersAndLosersAndNotifiesEveryone() {
        val lotteries = FakeDrawLotteryRepository(threeEntries)
        val notifications = FakeDrawNotificationRepository()

        LotteryDrawService(Random(42)).runDueDraws(listOf(dueLottery()), lotteries, notifications, now = 2000L)

        // winnerCount is 2, so 2 of the 3 win
        assertEquals(1, lotteries.claimCalls)
        assertEquals(2, lotteries.results.values.count { it == WaitlistResult.WON })
        assertEquals(1, lotteries.results.values.count { it == WaitlistResult.LOST })

        // everyone gets exactly one notification
        assertEquals(3, notifications.sent.size)
        assertEquals(2, notifications.sent.count { it.type == NotificationType.LOTTERY_WIN })
        assertEquals(1, notifications.sent.count { it.type == NotificationType.LOTTERY_LOSS })
    }

    @Test
    fun runDueDrawsDoesNothingWhenAnotherPhoneClaimedIt() {
        val lotteries = FakeDrawLotteryRepository(threeEntries, claimSucceeds = false)
        val notifications = FakeDrawNotificationRepository()

        LotteryDrawService(Random(42)).runDueDraws(listOf(dueLottery()), lotteries, notifications, now = 2000L)

        assertEquals(1, lotteries.claimCalls)
        assertEquals(0, lotteries.results.size)
        assertEquals(0, notifications.sent.size)
    }

    // fake lottery repository for the draw tests: gives back fixed entries and records results
    private class FakeDrawLotteryRepository(
        private val entries: List<WaitlistEntry>,
        private val claimSucceeds: Boolean = true
    ) : LotteryRepository {
        var claimCalls = 0
        val results = HashMap<String, WaitlistResult>()

        override val lotteries: List<Lottery> = emptyList()
        override fun add(lottery: Lottery) {}
        override fun update(lottery: Lottery) {}
        override fun get(lotteryId: String): Lottery? = null
        override fun forOwner(ownerId: String): List<Lottery> = emptyList()
        override fun forCampsite(campsiteId: String): List<Lottery> = emptyList()
        override fun setStatus(lotteryId: String, status: LotteryStatus) {}
        override fun join(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun leave(lotteryId: String, entrantId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun listenToEntries(lotteryId: String, onUpdate: (List<WaitlistEntry>) -> Unit): () -> Unit = {}
        override fun entriesFor(entrantId: String, onResult: (List<WaitlistEntry>) -> Unit) {}

        override fun setResult(lotteryId: String, entrantId: String, result: WaitlistResult) {
            results[entrantId] = result
        }

        override fun getEntries(lotteryId: String, onResult: (List<WaitlistEntry>) -> Unit) {
            onResult(entries)
        }

        override fun claimDraw(lotteryId: String, now: Long, onResult: (Boolean) -> Unit) {
            claimCalls++
            onResult(claimSucceeds)
        }
    }

    // fake notification repository for the draw tests: just keeps what was sent
    private class FakeDrawNotificationRepository : NotificationRepository {
        val sent = ArrayList<Notification>()

        override val notifications: List<Notification> = emptyList()
        override fun send(notification: Notification) { sent.add(notification) }
        override fun markRead(notificationId: String) {}
        override fun forUser(userId: String): List<Notification> = emptyList()
        override fun sentBy(senderId: String): List<Notification> = emptyList()
        override fun listenToUser(
            userId: String,
            onAdded: (Notification) -> Unit,
            onModified: (Notification) -> Unit,
            onRemoved: (Notification) -> Unit
        ): () -> Unit = {}
    }
}