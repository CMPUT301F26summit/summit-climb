package com.example.summit_climb.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightRed
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Purpose: the owner's "List in Lottery" screen (US 02.04.01 to 02.04.07).
 * Pick one or more of your campsites, the registration and booking periods,
 * how many winners, and an optional waitlist cap.
 * Opened from a campsite's details screen, with that campsite already ticked.
 * If that campsite already has an open lottery, the form opens filled in to edit it (02.04.06).
 * Saving goes through OwnerLotteryController, so its validate() rules decide what's allowed.
 */

private val fieldDateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

// the first moment of a picked day on this phone (used for start dates)
fun startOfDay(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// the first moment of the NEXT day, so the whole picked day is included  (used for end dates)
fun endOfDay(date: LocalDate): Long =
    date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// the day a start time falls on (edit mode)
fun dayOfStart(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

// the last day an end time covers (edit mode, undoes endOfDay)
fun dayOfEnd(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis - 1).atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * One campsite the owner can tick.
 *
 * @property id: the campsite's id.
 * @property name: the name shown next to the checkbox.
 */
data class LotteryCampsiteOption(
    val id: String,
    val name: String
)

/**
 * The layout of the List in Lottery screen.
 *
 * @param campsites: the owner's campsites to choose from.
 * @param existing: the open lottery being edited, or null to make a new one.
 * @param preselectedCampsiteId: the campsite the owner came from, ticked by default.
 * @param message: why saving failed (from validate). Empty shows nothing.
 * @param saving: true while saving, so Save can't be pressed twice.
 * @param onBack: called when the owner taps Back or the X.
 * @param onSave: called with the lottery to save (ownerId is filled in by the caller).
 */
@Composable
fun CreateLotteryContent(
    campsites: List<LotteryCampsiteOption>,
    existing: Lottery?,
    preselectedCampsiteId: String,
    message: String,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (Lottery) -> Unit
) {
    // keyed on the lottery's id so the form fills in once an existing lottery loads
    var picked by remember(existing?.id) {
        mutableStateOf(existing?.campsiteIds?.toSet() ?: setOf(preselectedCampsiteId))
    }
    var regStart by remember(existing?.id) { mutableStateOf(existing?.let { dayOfStart(it.registrationStart) }) }
    var regEnd by remember(existing?.id) { mutableStateOf(existing?.let { dayOfEnd(it.registrationEnd) }) }
    var bookStart by remember(existing?.id) { mutableStateOf(existing?.let { dayOfStart(it.bookingStart) }) }
    var bookEnd by remember(existing?.id) { mutableStateOf(existing?.let { dayOfEnd(it.bookingEnd) }) }
    var winners by remember(existing?.id) { mutableStateOf(existing?.winnerCount?.toString() ?: "") }
    var capOn by remember(existing?.id) { mutableStateOf(existing?.waitlistCap != null) }
    var cap by remember(existing?.id) { mutableStateOf(existing?.waitlistCap?.toString() ?: "") }

    // which date's picker is open (null = none)
    var pickingDate by remember { mutableStateOf<String?>(null) }

    val winnersNumber = winners.toIntOrNull()
    val capNumber = cap.toIntOrNull()
    val complete = picked.isNotEmpty() &&
            regStart != null && regEnd != null && bookStart != null && bookEnd != null &&
            winnersNumber != null &&
            (!capOn || capNumber != null)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // back
            Row(
                Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp)
                    .testTag(CreateLotteryTags.BACK),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Button",
                    modifier = Modifier.size(20.dp),
                    tint = charcoal
                )
                Text(
                    text = CreateLotteryText.BACK,
                    color = charcoal,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // title and cancel X
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existing == null) CreateLotteryText.TITLE else CreateLotteryText.TITLE_EDIT,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.testTag(CreateLotteryTags.TITLE)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel button",
                    tint = lightRed,
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, lightRed, CircleShape)
                        .padding(4.dp)
                        .clickable(onClick = onBack)
                )
            }

            // campsites in this lottery
            FieldLabel("${CreateLotteryText.CAMPSITES} · ${picked.size} selected")
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(greyBlue, RoundedCornerShape(22.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (campsites.isEmpty()) {
                    Text(
                        text = CreateLotteryText.NO_CAMPSITES,
                        fontSize = 13.sp,
                        color = charcoal,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                for (campsite in campsites) {
                    val ticked = campsite.id in picked
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { picked = if (ticked) picked - campsite.id else picked + campsite.id }
                            .testTag(CreateLotteryTags.campsite(campsite.id)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ticked,
                            onCheckedChange = { picked = if (ticked) picked - campsite.id else picked + campsite.id },
                            colors = CheckboxDefaults.colors(checkedColor = charcoal)
                        )
                        Text(text = campsite.name, fontSize = 14.sp, color = charcoal)
                    }
                }
            }

            // registration period
            FieldLabel(CreateLotteryText.REGISTRATION)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField(CreateLotteryText.OPENS, regStart, CreateLotteryTags.REG_START, Modifier.weight(1f)) { pickingDate = CreateLotteryTags.REG_START }
                DateField(CreateLotteryText.CLOSES, regEnd, CreateLotteryTags.REG_END, Modifier.weight(1f)) { pickingDate = CreateLotteryTags.REG_END }
            }

            // booking period
            FieldLabel(CreateLotteryText.BOOKING)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField(CreateLotteryText.FROM, bookStart, CreateLotteryTags.BOOK_START, Modifier.weight(1f)) { pickingDate = CreateLotteryTags.BOOK_START }
                DateField(CreateLotteryText.TO, bookEnd, CreateLotteryTags.BOOK_END, Modifier.weight(1f)) { pickingDate = CreateLotteryTags.BOOK_END }
            }
            Text(
                text = CreateLotteryText.BOOKING_HINT,
                fontSize = 12.sp,
                color = charcoal,
                modifier = Modifier.padding(top = 4.dp)
            )

            // number of winners
            FieldLabel(CreateLotteryText.WINNERS)
            OutlinedTextField(
                value = winners,
                onValueChange = { input -> if (input.all { it.isDigit() }) winners = input },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag(CreateLotteryTags.WINNERS),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            // optional waitlist cap
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = CreateLotteryText.CAP, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = capOn,
                    onCheckedChange = { capOn = it },
                    modifier = Modifier.testTag(CreateLotteryTags.CAP_SWITCH),
                    colors = SwitchDefaults.colors(checkedTrackColor = charcoal)
                )
            }
            if (capOn) {
                OutlinedTextField(
                    value = cap,
                    onValueChange = { input -> if (input.all { it.isDigit() }) cap = input },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag(CreateLotteryTags.CAP),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            } else {
                Text(text = CreateLotteryText.NO_CAP, fontSize = 12.sp, color = charcoal)
            }

            // why saving failed (from validate)
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = charcoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(lightRed.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag(CreateLotteryTags.MESSAGE)
                )
            }

            Spacer(Modifier.height(16.dp))

            // save
            Button(
                onClick = {
                    val rs = regStart
                    val re = regEnd
                    val bs = bookStart
                    val be = bookEnd
                    if (rs != null && re != null && bs != null && be != null && winnersNumber != null) {
                        onSave(
                            Lottery(
                                id = existing?.id ?: "",
                                ownerId = existing?.ownerId ?: "",
                                campsiteIds = picked.toList(),
                                registrationStart = startOfDay(rs),
                                registrationEnd = endOfDay(re),
                                bookingStart = startOfDay(bs),
                                bookingEnd = endOfDay(be),
                                winnerCount = winnersNumber,
                                waitlistCap = if (capOn) capNumber else null,
                                entrantCount = existing?.entrantCount ?: 0,
                                status = existing?.status ?: LotteryStatus.OPEN
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(CreateLotteryTags.SAVE),
                shape = CircleShape,
                enabled = complete && !saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = charcoal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (existing == null) CreateLotteryText.SAVE else CreateLotteryText.SAVE_EDIT,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // date picker popup for whichever date was tapped
    val field = pickingDate
    if (field != null) {
        val current = when (field) {
            CreateLotteryTags.REG_START -> regStart
            CreateLotteryTags.REG_END -> regEnd
            CreateLotteryTags.BOOK_START -> bookStart
            else -> bookEnd
        }
        DatePickerPopup(
            initial = current,
            onPicked = { day ->
                when (field) {
                    CreateLotteryTags.REG_START -> regStart = day
                    CreateLotteryTags.REG_END -> regEnd = day
                    CreateLotteryTags.BOOK_START -> bookStart = day
                    else -> bookEnd = day
                }
            },
            onDismiss = { pickingDate = null }
        )
    }
}

// small bold label above a field (same as the register campsite screen)
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

// a box showing the picked date, tap to open the picker
@Composable
private fun DateField(label: String, date: LocalDate?, tag: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier) {
        Text(text = label, fontSize = 12.sp, color = charcoal)
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, greyBlue, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp)
                .testTag(tag),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = date?.format(fieldDateFormat) ?: CreateLotteryText.PICK_DATE,
                fontSize = 14.sp,
                color = charcoal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerPopup(initial: LocalDate?, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // the picker gives midnight UTC of the picked day
                val millis = state.selectedDateMillis
                if (millis != null) {
                    onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) {
                Text(CreateLotteryText.OK, color = charcoal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(CreateLotteryText.CANCEL, color = charcoal)
            }
        }
    ) {
        DatePicker(state = state)
    }
}

// text field colours used across the owner forms
@Composable
private fun fieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = charcoal,
    unfocusedTextColor = charcoal,
    cursorColor = charcoal,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = charcoal,
    unfocusedBorderColor = greyBlue
)

// Text shown on the List in Lottery screen. Tests compare against these.
object CreateLotteryText {
    const val BACK = "Back"
    const val TITLE = "List in Lottery"
    const val TITLE_EDIT = "Edit Lottery"
    const val CAMPSITES = "Campsites"
    const val NO_CAMPSITES = "You don't have any campsites yet."
    const val REGISTRATION = "Registration period"
    const val OPENS = "Opens"
    const val CLOSES = "Closes"
    const val BOOKING = "Booking period"
    const val FROM = "From"
    const val TO = "To"
    const val BOOKING_HINT = "Booking must start after registration closes."
    const val WINNERS = "Number of winners"
    const val CAP = "Limit the waitlist"
    const val NO_CAP = "No limit on how many entrants can join."
    const val PICK_DATE = "Pick a date"
    const val SAVE = "List in lottery"
    const val SAVE_EDIT = "Save changes"
    const val OK = "OK"
    const val CANCEL = "Cancel"
}

// Test tags on the List in Lottery screen, for UI tests.
object CreateLotteryTags {
    const val BACK = "createLotteryBack"
    const val TITLE = "createLotteryTitle"
    const val REG_START = "registrationStart"
    const val REG_END = "registrationEnd"
    const val BOOK_START = "bookingStart"
    const val BOOK_END = "bookingEnd"
    const val WINNERS = "winnerCount"
    const val CAP_SWITCH = "waitlistCapSwitch"
    const val CAP = "waitlistCap"
    const val MESSAGE = "createLotteryMessage"
    const val SAVE = "saveLotteryButton"

    fun campsite(campsiteId: String): String {
        return "lotteryCampsite_$campsiteId"
    }
}

// Preview with sample data.
@Preview(showBackground = true)
@Composable
fun CreateLotteryPreview() {
    CreateLotteryContent(
        campsites = listOf(
            LotteryCampsiteOption("1", "Cedar Valley"),
            LotteryCampsiteOption("2", "Lakeside Camp")
        ),
        existing = null,
        preselectedCampsiteId = "1",
        message = "",
        saving = false,
        onBack = {},
        onSave = {}
    )
}