package com.example.summit_climb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold

/**
 * Purpose: the entrant's "My Lotteries" tab.
 * A scrollable list of every lottery the entrant is in, with a status badge on each.
 * Pending -> Leave Lottery (asks "are you sure?" first)
 * Won -> Confirm Reservation (opens the reservation screen)
 * Lost -> no button
 * Leaving, declining or reserving removes the lottery from the list (reserved ones show in My Reservations).
 * After leaving, a short message shows at the top and disappears by itself.
 * Opened from the bottom bar's Lotteries button and from lottery notifications.
 */

// team colours (from the info doc)
// NOTE: delete these once Kshitij's CampsiteDetailsScreen.kt is on this branch (it has the same ones)
private val charcoal = Color(0xFF36454F)
private val lightGreen = Color(0xFF96B38E)
private val lightRed = Color(0xFFFF7F7F)
private val lightOrange = Color(0xFFFFA759)
private val lightBlue = Color(0xFFA6C8EB)
private val greyBlue = Color(0xFFC9DEF3)

/**
 * What one lottery card shows.
 *
 * @property lotteryId: which lottery this card is for.
 * @property campsiteName: name at the top of the card.
 * @property location: city/area under the name.
 * @property status: "Pending", "Won" or "Lost".
 * @property info: line under the location, e.g. "Lottery closes October 30, 2026".
 */
data class LotteryUiItem(
    val lotteryId: String,
    val campsiteName: String,
    val location: String,
    val status: String,
    val info: String
)

/**
 * The layout of the My Lotteries screen.
 *
 * @param lotteries: the entrant's lotteries to show.
 * @param message: short message shown at the top (e.g. after leaving). Empty shows nothing.
 * @param onMessageShown: called after the message has shown for 3 seconds, so it can be cleared.
 * @param onLeave: called with a lotteryId after the entrant confirms leaving.
 * @param onConfirmReservation: called with a lotteryId when the entrant taps Confirm Reservation.
 */
@Composable
fun MyLotteriesContent(
    lotteries: List<LotteryUiItem>,
    message: String,
    onMessageShown: () -> Unit,
    onLeave: (String) -> Unit,
    onConfirmReservation: (String) -> Unit
) {
    // the lottery waiting for the user to confirm leaving (null = no popup)
    var toLeave by remember { mutableStateOf<LotteryUiItem?>(null) }

    // hide the message after 3 seconds
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(3000)
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        //title
        Text(
            text = MyLotteriesText.TITLE,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )

        //short message (e.g. after leaving)
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = charcoal,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(greyBlue, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag(MyLotteriesTags.MESSAGE)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (lotteries.isEmpty()) {
            Text(text = MyLotteriesText.NONE, color = charcoal, fontSize = 14.sp)
        }

        //scrollable list of lottery cards
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lotteries) { lottery ->
                LotteryCard(
                    lottery = lottery,
                    onLeave = { toLeave = lottery },      // opens the popup first
                    onConfirmReservation = onConfirmReservation
                )
            }
        }
    }

    //"Leave Lottery?" popup
    val leaving = toLeave
    if (leaving != null) {
        AlertDialog(
            onDismissRequest = { toLeave = null },
            title = { Text(MyLotteriesText.LEAVE_TITLE) },
            text = { Text("Are you sure you would like to leave the lottery for ${leaving.campsiteName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLeave(leaving.lotteryId)
                        toLeave = null
                    },
                    modifier = Modifier.testTag(MyLotteriesTags.CONFIRM_LEAVE)
                ) {
                    Text(MyLotteriesText.CONFIRM_LEAVE, color = lightRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toLeave = null }) {
                    Text(MyLotteriesText.CANCEL, color = charcoal)
                }
            }
        )
    }
}

// one lottery card
@Composable
fun LotteryCard(
    lottery: LotteryUiItem,
    onLeave: () -> Unit,
    onConfirmReservation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(greyBlue, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        //name on the left, status badge on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = lottery.campsiteName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.Black
            )
            Text(
                text = lottery.status,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(statusColor(lottery.status), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Text(text = lottery.location, fontSize = 14.sp, color = charcoal)
        Text(text = lottery.info, fontSize = 13.sp, color = charcoal)

        //Pending: Leave Lottery
        if (lottery.status == MyLotteriesText.PENDING) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = lightBlue)
            Button(
                onClick = onLeave,
                modifier = Modifier.testTag(MyLotteriesTags.leave(lottery.lotteryId)),
                colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
            ) {
                Text(text = MyLotteriesText.LEAVE, color = Color.Black)
            }
        }

        //Won: Confirm Reservation
        if (lottery.status == MyLotteriesText.WON) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = lightBlue)
            Button(
                onClick = { onConfirmReservation(lottery.lotteryId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(MyLotteriesTags.confirm(lottery.lotteryId)),
                colors = ButtonDefaults.buttonColors(containerColor = lightBlue)
            ) {
                Text(text = MyLotteriesText.CONFIRM_RESERVATION, color = Color.Black)
            }
        }
    }
}

// the colour behind the status word
fun statusColor(status: String): Color {
    if (status == MyLotteriesText.PENDING) return lightOrange
    if (status == MyLotteriesText.WON) return lightGreen
    return lightRed   // Lost
}

// Text shown on the My Lotteries screen. Tests compare against these.
object MyLotteriesText {
    const val TITLE = "My Lotteries"
    const val PENDING = "Pending"
    const val WON = "Won"
    const val LOST = "Lost"
    const val LEAVE = "Leave Lottery"
    const val CONFIRM_RESERVATION = "Confirm Reservation"
    const val NONE = "You haven't joined any lotteries yet."
    const val LEAVE_TITLE = "Leave Lottery?"
    const val CONFIRM_LEAVE = "Yes, leave lottery"
    const val CANCEL = "Cancel"

    // message shown after leaving, e.g. "You left the lottery for Summit Camp."
    fun left(campsiteName: String): String {
        return "You left the lottery for $campsiteName."
    }
}

// Test tags on the My Lotteries screen, for UI tests.
object MyLotteriesTags {
    const val MESSAGE = "lotteriesMessage"
    const val CONFIRM_LEAVE = "confirmLeaveButton"

    // e.g. "leave_123"
    fun leave(lotteryId: String): String {
        return "leave_$lotteryId"
    }

    // e.g. "confirm_123"
    fun confirm(lotteryId: String): String {
        return "confirm_$lotteryId"
    }
}

// Preview with sample data.
@Preview(showBackground = true)
@Composable
fun MyLotteriesPreview() {
    var lotteries by remember {
        mutableStateOf(
            listOf(
                LotteryUiItem("1", "Summit Camp", "Banff, AB", "Pending", "Lottery closes October 30, 2026"),
                LotteryUiItem("2", "Cedar Valley", "Banff, AB", "Won", "You've been selected!"),
                LotteryUiItem("4", "Pine Ridge", "Canmore, AB", "Lost", "You weren't selected this time.")
            )
        )
    }
    var message by remember { mutableStateOf("") }

    MyLotteriesContent(
        lotteries = lotteries,
        message = message,
        onMessageShown = { message = "" },
        onLeave = { id ->
            val name = lotteries.find { it.lotteryId == id }?.campsiteName ?: ""
            lotteries = lotteries.filter { it.lotteryId != id }
            message = MyLotteriesText.left(name)
        },
        onConfirmReservation = {}
    )
}