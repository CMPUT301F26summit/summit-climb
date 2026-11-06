/*
package com.example.summit_climb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Purpose: the "Confirm Reservation" screen, opened from a Won or Reserved card on My Lotteries
 * Won: pick check-in and check-out on the calendar, then Confirm Reservation (with a
 * terms checkbox) or Decline (with an "are you sure?" popup).
 * Reserved: shows that the reservation is confirmed (view only).
 * Rules: dates must be inside the lottery's booking period and can't include booked nights
 * (uses AvailabilityCalendar.kt).
 */

// team colours (from the info doc), kept in an object so the names can't clash with other files
private object ReservationColors {
    val charcoal = Color(0xFF36454F)
    val lightGreen = Color(0xFF96B38E)
    val lightRed = Color(0xFFFF7F7F)
    val lightBlue = Color(0xFFA6C8EB)
    val greyBlue = Color(0xFFC9DEF3)
    val linkBlue = Color(0xFF007AFF)
}

private val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

/**
 * What the reservation screen shows.
 *
 * @property lotteryId: the lottery the entrant won.
 * @property campsiteName: name of the campsite.
 * @property location: city/area of the campsite.
 * @property bookingStart: first day the entrant may check in.
 * @property bookingEnd: last day the entrant may check out.
 * @property bookedDates: nights already taken by other reservations.
 * @property reserved: true if the entrant already reserved (view only).
 */
data class ReservationUiInfo(
    val lotteryId: String,
    val campsiteName: String,
    val location: String,
    val bookingStart: LocalDate,
    val bookingEnd: LocalDate,
    val bookedDates: Set<LocalDate>,
    val reserved: Boolean
)


// how many nights between check in and check out
fun nightsBetween(checkIn: LocalDate, checkOut: LocalDate): Long {
    return ChronoUnit.DAYS.between(checkIn, checkOut)
}

// true if the whole stay is inside the booking period
fun isInBookingPeriod(checkIn: LocalDate, checkOut: LocalDate, bookingStart: LocalDate, bookingEnd: LocalDate): Boolean {
    return !checkIn.isBefore(bookingStart) && !checkOut.isAfter(bookingEnd)
}

// true if any night of the stay is already booked (check out day itself is free)
fun overlapsBooked(checkIn: LocalDate, checkOut: LocalDate, bookedDates: Set<LocalDate>): Boolean {
    var night = checkIn
    while (night.isBefore(checkOut)) {
        if (night in bookedDates) return true
        night = night.plusDays(1)
    }
    return false
}

// What's wrong with the picked dates, or null if they're fine.
fun datesProblem(checkIn: LocalDate?, checkOut: LocalDate?, info: ReservationUiInfo): String? {
    if (checkIn == null) return ReservationText.PICK_CHECK_IN
    if (checkOut == null) return ReservationText.PICK_CHECK_OUT
    if (!isInBookingPeriod(checkIn!!, checkOut!!, info.bookingStart, info.bookingEnd)) {
        return "Please pick dates between ${info.bookingStart.format(dateFormat)} and ${info.bookingEnd.format(dateFormat)}."
    }
    if (overlapsBooked(checkIn, checkOut, info.bookedDates)) return ReservationText.ALREADY_BOOKED
    return null
}

/**
 * Works out the new check in / check out after the user taps a date.
 * First tap = check-in, second tap (later date) = check-out, any other tap starts over.
 * @return the new (check-in, check-out).
 */
fun nextSelection(checkIn: LocalDate?, checkOut: LocalDate?, tapped: LocalDate): Pair<LocalDate?, LocalDate?> {
    if (checkIn == null || checkOut != null) return Pair(tapped, null) // start over
    if (!tapped.isAfter(checkIn)) return Pair(tapped, null) // earlier date = new check-in
    return Pair(checkIn, tapped) // later date = check-out
}

// layout

/**
 * The layout of the reservation screen.
 *
 * @param info the lottery and campsite to show.
 * @param message message to show (e.g. why saving failed). Empty shows nothing.
 * @param onBackClick called when the user taps Back.
 * @param onConfirm called with (check-in, check-out) after the user confirms.
 * @param onDecline called after the user confirms declining.
 * @param onGuidelinesClick called when the user taps "Guidelines and Policies" (does nothing yet).
 */
@Composable
fun ReservationContent(
    info: ReservationUiInfo,
    message: String,
    onBackClick: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDecline: () -> Unit,
    onGuidelinesClick: () -> Unit
) {
    var checkIn by remember { mutableStateOf<LocalDate?>(null) }
    var checkOut by remember { mutableStateOf<LocalDate?>(null) }
    var showConfirmPopup by remember { mutableStateOf(false) }
    var showDeclinePopup by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    val problem = datesProblem(checkIn, checkOut, info)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // back
        Text(
            text = ReservationText.BACK,
            color = ReservationColors.charcoal,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable(onClick = onBackClick)
                .padding(vertical = 8.dp)
                .testTag(ReservationTags.BACK)
        )

        // title
        Text(
            text = if (info.reserved) ReservationText.TITLE_VIEW else ReservationText.TITLE,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black,
            modifier = Modifier.testTag(ReservationTags.TITLE)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // green banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReservationColors.lightGreen, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (info.reserved) ReservationText.CONFIRMED else ReservationText.CONGRATS,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "You've been selected for ${info.campsiteName}.",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // campsite card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReservationColors.greyBlue, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = info.campsiteName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.Black
            )
            Text(text = info.location, fontSize = 14.sp, color = ReservationColors.charcoal)

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ReservationColors.lightBlue)

            if (info.reserved) {
                // view only
                Text(text = ReservationText.VIEW_ONLY, fontSize = 14.sp, color = ReservationColors.charcoal)
            } else {
                Text(text = ReservationText.SELECT_DATES, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = ReservationText.HOW_TO_PICK, fontSize = 12.sp, color = ReservationColors.charcoal)

                // calendar (Using availablitityCalendar.kt): highlights the date being picked
                AvailabilityCalendar(
                    bookedDates = info.bookedDates,
                    // Add a fallback so it passes a non-null LocalDate when nothing is selected yet:
                    selectedDate = checkOut ?: checkIn ?: info.bookingStart ?: LocalDate.now(),
                    onDateSelected = { tapped ->
                        val picked = nextSelection(checkIn, checkOut, tapped)
                        checkIn = picked.first
                        checkOut = picked.second
                    }
                )
                // picked dates as text
                val pickedIn = checkIn
                val pickedOut = checkOut
                if (pickedIn != null && pickedOut != null) {
                    Text(
                        text = "${pickedIn.format(dateFormat)} – ${pickedOut.format(dateFormat)} " +
                                "(${nightsBetween(pickedIn, pickedOut)} nights)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag(ReservationTags.RANGE)
                    )
                } else if (pickedIn != null) {
                    Text(text = "Check-in: ${pickedIn.format(dateFormat)}", fontSize = 14.sp)
                }

                // what's still wrong (if anything)
                if (problem != null) {
                    Text(
                        text = problem,
                        fontSize = 13.sp,
                        color = ReservationColors.charcoal,
                        modifier = Modifier.testTag(ReservationTags.PROBLEM)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        acceptedTerms = false
                        showConfirmPopup = true
                    },
                    enabled = problem == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(ReservationTags.CONFIRM),
                    colors = ButtonDefaults.buttonColors(containerColor = ReservationColors.lightBlue)
                ) {
                    Text(text = ReservationText.CONFIRM_BUTTON, color = Color.Black)
                }

                TextButton(
                    onClick = { showDeclinePopup = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag(ReservationTags.DECLINE)
                ) {
                    Text(text = ReservationText.DECLINE, color = ReservationColors.lightRed)
                }
            }
        }

        // message (e.g. if saving failed)
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = ReservationColors.charcoal, fontSize = 14.sp)
        }
    }

    // "Confirm Reservation?" popup with the terms checkbox
    val confirmIn = checkIn
    val confirmOut = checkOut
    if (showConfirmPopup && confirmIn != null && confirmOut != null) {
        AlertDialog(
            onDismissRequest = { showConfirmPopup = false },
            title = { Text(ReservationText.CONFIRM_TITLE) },
            text = {
                Column {
                    Text(ReservationText.TERMS_QUESTION)
                    Text(ReservationText.TERMS_NOTE, fontSize = 12.sp, color = ReservationColors.charcoal)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            modifier = Modifier.testTag(ReservationTags.TERMS_CHECKBOX)
                        )
                        Column {
                            Text(ReservationText.TERMS_ACCEPT, fontSize = 12.sp)
                            // link, does nothing until the guidelines screen is connected
                            Text(
                                text = ReservationText.GUIDELINES_LINK,
                                fontSize = 12.sp,
                                color = ReservationColors.linkBlue,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable(onClick = onGuidelinesClick)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(checkIn!!, checkOut!!)
                        showConfirmPopup = false
                    },
                    enabled = acceptedTerms,
                    modifier = Modifier.testTag(ReservationTags.CONFIRM_POPUP)
                ) {
                    Text(ReservationText.CONFIRM_POPUP)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPopup = false }) {
                    Text(ReservationText.CANCEL, color = ReservationColors.charcoal)
                }
            }
        )
    }

    // "Decline Reservation?" popup
    if (showDeclinePopup) {
        AlertDialog(
            onDismissRequest = { showDeclinePopup = false },
            title = { Text(ReservationText.DECLINE_TITLE, color = ReservationColors.lightRed) },
            text = { Text(ReservationText.ARE_YOU_SURE) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDecline()
                        showDeclinePopup = false
                    },
                    modifier = Modifier.testTag(ReservationTags.DECLINE_POPUP)
                ) {
                    Text(ReservationText.CONFIRM_POPUP)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclinePopup = false }) {
                    Text(ReservationText.CANCEL, color = ReservationColors.charcoal)
                }
            }
        )
    }
}

// Text shown on the reservation screen. Tests compare against these.
object ReservationText {
    const val BACK = "← Back"
    const val TITLE = "Confirm Reservation"
    const val TITLE_VIEW = "Your Reservation"
    const val CONGRATS = "Congratulations! You won!"
    const val CONFIRMED = "Reservation confirmed!"
    const val SELECT_DATES = "Select your Dates"
    const val HOW_TO_PICK = "Tap your check-in date, then your check-out date."
    const val PICK_CHECK_IN = "Pick your check-in date."
    const val PICK_CHECK_OUT = "Pick your check-out date."
    const val ALREADY_BOOKED = "Some of those nights are already booked."
    const val VIEW_ONLY = "Your reservation is confirmed. See it under My Reservations."
    const val CONFIRM_BUTTON = "Confirm Reservation"
    const val DECLINE = "Decline"
    const val CONFIRM_TITLE = "Confirm Reservation?"
    const val TERMS_QUESTION = "Have you read our terms and conditions?"
    const val TERMS_NOTE = "Please make sure you've read the terms and conditions before booking."
    const val TERMS_ACCEPT = "Yes, I have read and accept the"
    const val GUIDELINES_LINK = "Guidelines and Policies"
    const val CONFIRM_POPUP = "Confirm"
    const val CANCEL = "Cancel"
    const val DECLINE_TITLE = "Decline Reservation?"
    const val ARE_YOU_SURE = "Are you sure?"
}

// Test tags on the reservation screen, for UI tests.
object ReservationTags {
    const val BACK = "reservationBack"
    const val CONFIRM = "confirmReservationButton"
    const val DECLINE = "declineButton"
    const val CONFIRM_POPUP = "confirmPopupButton"
    const val DECLINE_POPUP = "declinePopupButton"
    const val TERMS_CHECKBOX = "termsCheckbox"
    const val RANGE = "pickedRange"
    const val PROBLEM = "datesProblem"
    const val TITLE = "reservationTitle"
}

// sample data so the screen shows in Android Studio's preview
@Preview(showBackground = true)
@Composable
fun ReservationPreview() {
    val today = LocalDate.now()
    ReservationContent(
        info = ReservationUiInfo(
            lotteryId = "2",
            campsiteName = "Cedar Valley",
            location = "Banff, AB",
            bookingStart = today.plusDays(2),
            bookingEnd = today.plusDays(25),
            bookedDates = setOf(today.plusDays(6), today.plusDays(7)),
            reserved = false
        ),
        message = "",
        onBackClick = {},
        onConfirm = { _, _ -> },
        onDecline = {},
        onGuidelinesClick = {}
    )
}