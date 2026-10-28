package com.example.summit_climb

import com.example.summit_climb.WaitlistResult
import kotlin.io.encoding.isInMimeAlphabet

class WaitlistEntry(
    var lotteryId: String = "",
    var entrantId: String = "",
    var joinedAt: Long = 0L,
    var result: WaitlistResult = WaitlistResult.WAITING
) {
    fun removable(lottery: Lottery, now: Long = System.currentTimeMillis()): Boolean{
        return lottery.isRegisterationOpen(now)
    }

    fun action(lottery: Lottery, now: Long = System.currentTimeMillis()): Boolean{
        val won = (result == WaitlistResult.WON)
        val booking_open = lottery.isInBookingPeriod(now)
        return won && booking_open
    }
}

enum class WaitlistResult{
    WAITING, WON, LOST, DECLINED, RESERVED
}