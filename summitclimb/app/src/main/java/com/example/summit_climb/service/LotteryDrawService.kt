package com.example.summit_climb.service

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import kotlin.random.Random
import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.model.WaitlistResult

/** Purpose: runs a lottery's draw once its registration period ends.
 * pickWinners and isDueForDraw hold the draw logic. runDueDraws uses them on every due lottery:
 * it claims the lottery so the draw only runs once, sets each entry WON or LOST,
 * and sends every entrant a LOTTERY_WIN or LOTTERY_LOSS notification.
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

    /**
     * runs the draw for every lottery that is due
     * each one is claimed first, so if many phones call this at once only one draws it
     */
    fun runDueDraws(
        lotteries: List<Lottery>,
        lotteryRepository: LotteryRepository,
        notificationRepository: NotificationRepository,
        now: Long = System.currentTimeMillis()
    ) {
        for (lottery in lotteries) {
            if (!isDueForDraw(lottery, now)) {
                continue
            }
            lotteryRepository.claimDraw(lottery.id, now) { claimed ->
                if (claimed) {
                    drawOne(lottery, lotteryRepository, notificationRepository)
                }
            }
        }
    }

    // picks winners for one claimed lottery, saves WON/LOST and tells every entrant
    private fun drawOne(
        lottery: Lottery,
        lotteryRepository: LotteryRepository,
        notificationRepository: NotificationRepository
    ) {
        lotteryRepository.getEntries(lottery.id) { entries ->
            val entrantIds = ArrayList<String>()
            for (entry in entries) {
                entrantIds.add(entry.entrantId)
            }

            val result = pickWinners(entrantIds, lottery.winnerCount)

            for (id in result.winners) {
                lotteryRepository.setResult(lottery.id, id, WaitlistResult.WON)
                notificationRepository.send(resultNotification(lottery, id, true))
            }
            for (id in result.losers) {
                lotteryRepository.setResult(lottery.id, id, WaitlistResult.LOST)
                notificationRepository.send(resultNotification(lottery, id, false))
            }
        }
    }

    // the win or lose notification for one entrant
    fun resultNotification(lottery: Lottery, entrantId: String, won: Boolean): Notification {
        var campsiteId = ""
        if (lottery.campsiteIds.isNotEmpty()) {
            campsiteId = lottery.campsiteIds[0]
        }

        if (won) {
            return Notification(
                senderId = "system",
                recipientId = entrantId,
                type = NotificationType.LOTTERY_WIN,
                title = "You won the lottery!",
                text = "Congratulations, you were selected! Go to My Lotteries to reserve your dates.",
                campsiteId = campsiteId,
                relatedId = lottery.id
            )
        }
        return Notification(
            senderId = "system",
            recipientId = entrantId,
            type = NotificationType.LOTTERY_LOSS,
            title = "Lottery results",
            text = "You weren't selected this time. Thanks for entering!",
            campsiteId = campsiteId,
            relatedId = lottery.id
        )
    }
}