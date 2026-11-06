package com.example.summit_climb.service

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

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
}