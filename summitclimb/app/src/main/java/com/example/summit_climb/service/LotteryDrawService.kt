package com.example.summit_climb.service

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import kotlin.random.Random

/** Purpose: runs a lottery's draw once its registration period ends.
 * This first version holds the draw logic only (picking winners, checking if a draw is due).
 * Reading the waitlist, saving results and sending WIN/LOSS notifications through
 * NotificationRepository get added once dev compiles and LotteryRepository's entry methods exist.
 */

//who won and who lost one draw
data class DrawResult(
    val winners: List<String>,
    val losers: List<String>
)

// Picks lottery winners at random.
class LotteryDrawService(
    private val random: Random = Random.Default   // tests pass a fixed seed
) {

    /**
     * picks winnerCount entrants at random and everyone else loses
     * if there are fewer entrants than winnerCount, everyone wins
     */
    fun pickWinners(entrantIds: List<String>, winnerCount: Int): DrawResult {
        //Remove duplicates
        val unique = ArrayList<String>()
        for (id in entrantIds) {
            if (id !in unique) {
                unique.add(id)
            }
        }

        // put everyone in a random order
        val shuffled = unique.shuffled(random)

        // work out how many can actually win
        var count = winnerCount
        if (count < 0) {
            count = 0
        }
        if (count > shuffled.size) {
            count = shuffled.size
        }

        // the first `count` people win, everyone else loses
        val winners = ArrayList<String>()
        val losers = ArrayList<String>()
        for (i in shuffled.indices) {
            if (i < count) {
                winners.add(shuffled[i])
            } else {
                losers.add(shuffled[i])
            }
        }

        return DrawResult(winners, losers)
    }

    // true if registration has ended and the lottery hasn't been drawn yet
    fun isDueForDraw(lottery: Lottery, now: Long = System.currentTimeMillis()): Boolean =
        lottery.status == LotteryStatus.OPEN && now >= lottery.registrationEnd
}