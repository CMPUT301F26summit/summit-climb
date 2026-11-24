package com.example.summit_climb.ui.entrant

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.summit_climb.ui.ScreensBackground
import com.example.summit_climb.R
import androidx.compose.ui.Alignment
import com.example.summit_climb.ui.theme.screenBackground

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

    Box(
        Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        Image(
            painter = painterResource(R.drawable.mountain_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(55.dp))
            //title
            Text(
                text = MyReservationsText.TITLE,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF2C4352),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            //short message (e.g. after cancelling)
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = ReservationListColors.charcoal,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ReservationListColors.greyBlue, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag(MyReservationsTags.MESSAGE)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            //Active, Past, Cancelled tabs
            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color(0xFFE4EDF5),
                contentColor = Color(0xFF245C86),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                indicator = {},
                divider = {}
            ) {
                for (tab in tabs) {
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTab == tab)
                                    FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp)
                               },
                        selectedContentColor = Color(0xFF245C86),
                        unselectedContentColor = Color(0xFF597A94),
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == tab) Color.White
                                else Color.Transparent
                            )
                            .testTag(MyReservationsTags.tab(tab)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shown.isEmpty()) {
                Text(
                    text = "No ${selectedTab.lowercase()} reservations.",
                    color = Color(0xFF2C4352),
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
    }

    //"Cancel Reservation?" popup
    val cancelling = toCancel
    if (cancelling != null) {
        AlertDialog(
            onDismissRequest = { toCancel = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            titleContentColor = Color(0xFF233B4D),
            textContentColor = Color(0xFF597A94),
            title = {
                Text(
                    MyReservationsText.CANCEL_TITLE,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,) },
            text = {
                Column {
                    Text(MyReservationsText.ARE_YOU_SURE)
                    Spacer(modifier = Modifier.height(8.dp))
                    // grey box with the reservation being cancelled
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFEDF4F8),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            cancelling.campsiteName,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF245C86)
                        )
                        Text("${cancelling.dates} | ${cancelling.nights} nights",
                            fontSize = 13.sp,
                            color = Color(0xFF597A94))
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = {
                            onCancel(cancelling.reservationId)
                            toCancel = null
                        }
                    ) {
                        Text(
                            "Yes, cancel reservation",
                            color = Color(0xFFC45050),
                            fontSize = 14.sp
                        )
                    }

                    TextButton(
                        onClick = { toCancel = null }
                    ) {
                        Text(
                            "Keep reservation",
                            color = Color(0xFF245C86),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            dismissButton = {}
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
            .background(Color.White, RoundedCornerShape(20.dp))
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
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF2C4352)
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
            fontSize = 14.sp,
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C4352)
                )

                //Active: Cancel Reservation
                if (reservation.status == MyReservationsText.ACTIVE) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag(MyReservationsTags.cancel(reservation.reservationId))
                    ) {
                        Text(
                            text = MyReservationsText.CANCEL_BUTTON,
                            color = ReservationListColors.lightRed,
                            fontSize = 16.sp
                        )
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