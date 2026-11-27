package com.example.summit_climb

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.model.WaitlistResult
import com.example.summit_climb.ui.entrant.MyLotteriesText
import com.example.summit_climb.ui.entrant.lotteryCardFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// tests for turning an entrant's entry into a My Lotteries card (F16)
class MyLotteriesCardTest {

    private val lottery = Lottery(
        id = "L1",
        campsiteIds = listOf("C1"),
        registrationEnd = 1000L,
        bookingStart = 2000L,
        bookingEnd = 3000L
    )

    private fun entry(result: WaitlistResult) = WaitlistEntry("L1", "me", 0L, result)

    @Test
    fun waitingEntryShowsPendingWithClosingDate() {
        val card = lotteryCardFor(entry(WaitlistResult.WAITING), lottery, "Cedar Valley", "Banff, AB")!!

        assertEquals(MyLotteriesText.PENDING, card.status)
        assertTrue(card.info.startsWith("Lottery closes"))
    }

    @Test
    fun wonEntryShowsWonWithBookingPeriod() {
        val card = lotteryCardFor(entry(WaitlistResult.WON), lottery, "Cedar Valley", "Banff, AB")!!

        assertEquals(MyLotteriesText.WON, card.status)
        assertTrue(card.info.contains("Reserve between"))
    }

    @Test
    fun lostEntryShowsLost() {
        val card = lotteryCardFor(entry(WaitlistResult.LOST), lottery, "Cedar Valley", "Banff, AB")!!

        assertEquals(MyLotteriesText.LOST, card.status)
        assertEquals(MyLotteriesText.NOT_SELECTED, card.info)
    }

    @Test
    fun declinedAndReservedAreHidden() {
        assertNull(lotteryCardFor(entry(WaitlistResult.DECLINED), lottery, "Cedar Valley", "Banff, AB"))
        assertNull(lotteryCardFor(entry(WaitlistResult.RESERVED), lottery, "Cedar Valley", "Banff, AB"))
    }

    @Test
    fun cardUsesLotteryIdAndCampsiteDetails() {
        val card = lotteryCardFor(entry(WaitlistResult.WAITING), lottery, "Cedar Valley", "Banff, AB")!!

        assertEquals("L1", card.lotteryId)
        assertEquals("Cedar Valley", card.campsiteName)
        assertEquals("Banff, AB", card.location)
    }
}