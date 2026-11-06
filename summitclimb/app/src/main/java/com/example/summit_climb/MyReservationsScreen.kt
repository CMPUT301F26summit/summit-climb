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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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

/**
 * Purpose: the entrant's "My Reservations" tab (US 01.06.01, 01.06.02, 01.07.01).
 * Active, Past and Cancelled tabs (same as the owner's reservations screen).
 * Active -> Confirmed badge, total, Cancel Reservation (asks "are you sure?" first)
 * Past -> Completed badge, total, no button
 * Cancelled -> Cancelled or Rejected badge, no button
 * After cancelling, a short message shows at the top and disappears by itself.
 * Opened from the bottom bar's Reservations button.
 */

// team colours (from the info doc), kept in an object so the names can't clash with other files
private object ReservationListColors {
    val charcoal = Color(0xFF36454F)
    val lightGreen = Color(0xFF96B38E)
    val lightRed = Color(0xFFFF7F7F)
    val lightBlue = Color(0xFFA6C8EB)
    val greyBlue = Color(0xFFC9DEF3)
    val lightGrey = Color(0xFFDADAD8)
}

/**
 * What one reservation card shows.
 *
 * @property reservationId: which reservation this card is for.
 * @property campsiteName: name at the top of the card.
 * @property location: city/area under the name.
 * @property dates: e.g. "November 14 - 16, 2027".
 * @property nights: how many nights the stay is.
 * @property total: e.g. "$120".
 * @property status: "ACTIVE", "COMPLETED", "CANCELLED" or "REJECTED".
 */
data class MyReservationUiItem(
    val reservationId: String,
    val campsiteName: String,
    val location: String,
    val dates: String,
    val nights: Int,
    val total: String,
    val status: String
)

/**
 * The layout of the My Reservations screen.
 *
 * @param reservations: the entrant's reservations to show.
 * @param message: short message shown at the top (e.g. after cancelling). Empty shows nothing.
 * @param onMessageShown: called after the message has shown for 3 seconds, so it can be cleared.
 * @param onCancel: called with a reservationId after the entrant confirms cancelling.
 */
@Composable
fun MyReservationsContent(
    reservations: List<MyReservationUiItem>,
    message: String,
    onMessageShown: () -> Unit,
    onCancel: (String) -> Unit
) {
    val tabs = listOf(MyReservationsText.ACTIVE_TAB, MyReservationsText.PAST_TAB, MyReservationsText.CANCELLED_TAB)
    var selectedTab by remember { mutableStateOf(MyReservationsText.ACTIVE_TAB) }

    // the reservation waiting for the user to confirm cancelling (null = no popup)
    var toCancel by remember { mutableStateOf<MyReservationUiItem?>(null) }

    // hide the message after 3 seconds
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(3000)
            onMessageShown()
        }
    }

    // only the reservations for the chosen tab
    val shown = reservations.filter { reservation -> isInTab(reservation.status, selectedTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        //title
        Text(
            text = MyReservationsText.TITLE,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )

        //short message (e.g. after cancelling)
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = ReservationListColors.charcoal,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ReservationListColors.greyBlue, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag(MyReservationsTags.MESSAGE)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Active, Past, Cancelled tabs
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = Color.White
        ) {
            for (tab in tabs) {
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab) },
                    modifier = Modifier.testTag(MyReservationsTags.tab(tab))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (shown.isEmpty()) {
            Text(
                text = "No ${selectedTab.lowercase()} reservations.",
                color = ReservationListColors.charcoal,
                fontSize = 14.sp
            )
        }

        //scrollable list of reservation cards
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(shown) { reservation ->
                ReservationCard(
                    reservation = reservation,
                    onCancel = { toCancel = reservation } // opens the popup first
                )
            }
        }
    }

    //"Cancel Reservation?" popup
    val cancelling = toCancel
    if (cancelling != null) {
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text(MyReservationsText.CANCEL_TITLE) },
            text = {
                Column {
                    Text(MyReservationsText.ARE_YOU_SURE)
                    Spacer(modifier = Modifier.height(8.dp))
                    // grey box with the reservation being cancelled
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ReservationListColors.lightGrey, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(cancelling.campsiteName, fontWeight = FontWeight.Bold)
                        Text("${cancelling.dates} | ${cancelling.nights} nights", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancel(cancelling.reservationId)
                        toCancel = null
                    },
                    modifier = Modifier.testTag(MyReservationsTags.CONFIRM_CANCEL)
                ) {
                    Text(MyReservationsText.CONFIRM_CANCEL, color = ReservationListColors.lightRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { toCancel = null }) {
                    Text(MyReservationsText.KEEP, color = ReservationListColors.charcoal)
                }
            }
        )
    }
}

// one reservation card
@Composable
fun ReservationCard(
    reservation: MyReservationUiItem,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReservationListColors.greyBlue, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        //name on the left, status badge on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = reservation.campsiteName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.Black
            )
            Text(
                text = badgeText(reservation.status),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(badgeColor(reservation.status), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Text(text = reservation.location, fontSize = 14.sp, color = ReservationListColors.charcoal)
        Text(
            text = "${reservation.dates} | ${reservation.nights} nights",
            fontSize = 13.sp,
            color = ReservationListColors.charcoal
        )

        //Active and Past: total (and Cancel Reservation for Active)
        if (reservation.status == MyReservationsText.ACTIVE || reservation.status == MyReservationsText.COMPLETED) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ReservationListColors.lightBlue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${reservation.total} total",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                //Active: Cancel Reservation
                if (reservation.status == MyReservationsText.ACTIVE) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag(MyReservationsTags.cancel(reservation.reservationId))
                    ) {
                        Text(text = MyReservationsText.CANCEL_BUTTON, color = ReservationListColors.lightRed)
                    }
                }
            }
        }
    }
}

// which tab each status goes under (same as the owner's reservations screen)
fun isInTab(status: String, tab: String): Boolean {
    if (tab == MyReservationsText.ACTIVE_TAB) return status == MyReservationsText.ACTIVE
    if (tab == MyReservationsText.PAST_TAB) return status == MyReservationsText.COMPLETED
    if (tab == MyReservationsText.CANCELLED_TAB) {
        return status == MyReservationsText.CANCELLED || status == MyReservationsText.REJECTED
    }
    return false
}

// the word on the badge
fun badgeText(status: String): String {
    if (status == MyReservationsText.ACTIVE) return "Confirmed"
    if (status == MyReservationsText.COMPLETED) return "Completed"
    if (status == MyReservationsText.REJECTED) return "Rejected"
    return "Cancelled"
}

// the colour behind the badge word
private fun badgeColor(status: String): Color {
    if (status == MyReservationsText.ACTIVE) return ReservationListColors.lightGreen
    if (status == MyReservationsText.COMPLETED) return ReservationListColors.charcoal
    return ReservationListColors.lightRed   // Cancelled or Rejected
}

// Text shown on the My Reservations screen. Tests compare against these.
object MyReservationsText {
    const val TITLE = "My Reservations"
    const val ACTIVE_TAB = "Active"
    const val PAST_TAB = "Past"
    const val CANCELLED_TAB = "Cancelled"

    // reservation statuses (same words Saba's Reservation uses)
    const val ACTIVE = "ACTIVE"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
    const val REJECTED = "REJECTED"

    const val CANCEL_BUTTON = "Cancel Reservation"
    const val CANCEL_TITLE = "Cancel Reservation?"
    const val ARE_YOU_SURE = "Are you sure you want to cancel?"
    const val CONFIRM_CANCEL = "Yes, cancel reservation"
    const val KEEP = "Keep reservation"

    // message shown after cancelling
    fun cancelled(campsiteName: String): String {
        return "Your reservation at $campsiteName has been cancelled. " +
                "A confirmation has been sent to your notifications."
    }
}

// Test tags on the My Reservations screen, for UI tests.
object MyReservationsTags {
    const val MESSAGE = "reservationsMessage"
    const val CONFIRM_CANCEL = "confirmCancelButton"

    // e.g. "tab_Past"
    fun tab(name: String): String {
        return "tab_$name"
    }

    // e.g. "cancel_123"
    fun cancel(reservationId: String): String {
        return "cancel_$reservationId"
    }
}

// Preview with sample data. Cancelling really moves the card to Cancelled and shows the message.
@Preview(showBackground = true)
@Composable
fun MyReservationsPreview() {
    var reservations by remember {
        mutableStateOf(
            listOf(
                MyReservationUiItem("1", "Cedar Valley", "Banff, AB", "November 14 - 16, 2027", 2, "$120", "ACTIVE"),
                MyReservationUiItem("2", "New Valley", "Banff, AB", "September 3 - 5, 2026", 2, "$120", "COMPLETED"),
                MyReservationUiItem("3", "Dark Forest", "Jasper, AB", "October 1 - 3, 2026", 2, "$90", "CANCELLED"),
                MyReservationUiItem("4", "Pine Ridge", "Canmore, AB", "October 20 - 22, 2026", 2, "$100", "REJECTED")
            )
        )
    }
    var message by remember { mutableStateOf("") }

    MyReservationsContent(
        reservations = reservations,
        message = message,
        onMessageShown = { message = "" },
        onCancel = { id ->
            val name = reservations.find { it.reservationId == id }?.campsiteName ?: ""
            reservations = reservations.map {
                if (it.reservationId == id) it.copy(status = MyReservationsText.CANCELLED) else it
            }
            message = MyReservationsText.cancelled(name)
        }
    )
}