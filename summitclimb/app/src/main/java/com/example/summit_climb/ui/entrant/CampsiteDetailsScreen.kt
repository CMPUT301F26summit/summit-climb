package com.example.summit_climb.ui.entrant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import com.example.summit_climb.model.WaitlistEntry
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.EntrantLotteryController
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightGreen
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.lightOrange
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.noLotteryGrey
/**
 * The entrant's campsite details screen (US 01.01.05, 01.01.06, 01.02.01, 01.02.02, 01.13.01).
 * Styled like the owner's details screen ([com.example.summit_climb.ui.owner.OwnerCampsiteDetails]) but with entrant actions:
 * full details, amenities, availability, rules and policy, the active lottery with a
 * Join or Leave button, and 1–5 star rating.
 * Icons are vector drawables in res/drawable (ic_arrow_back, ic_location, ic_star_*,
 * ic_amenity_*), so this screen doesn't need the Material icon library.
 *
 * Outstanding: the map shows coordinates until the shared map component is ready.
 *
 * @param navController: app's navigation controller, used for the Back button.
 * @param campsiteId: ID of the campsite to show.
 * @param currentUserId: [com.example.summit_climb.model.User.userId] of the entrant using the app.
 * @param searchController: loads campsite and saves ratings.
 * @param lotteryController: loads lottery and joins or leaves it.
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsiteId: String,
    currentUserId: String,
    searchController: CampsiteSearchController,
    lotteryController: EntrantLotteryController
) {
    var myEntries by remember { mutableStateOf(listOf<WaitlistEntry>()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        lotteryController.getMyLotteries { entries -> myEntries = entries }
    }

    val campsite = searchController.campsiteDetails(campsiteId)
    if (campsite == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val lottery = campsite.activeLotteryId?.let { lotteryId -> lotteryController.getLottery(lotteryId) }
    val myEntry = myEntries.find { entry -> entry.lotteryId == lottery?.id }

    CampsiteDetailsContent(
        campsite = campsite,
        lottery = lottery,
        hasJoined = myEntry != null,
        message = message,
        onBackClick = { navController.popBackStack() },
        onJoinClick = {
            if (lottery != null) {
                lotteryController.joinLottery(lottery, myEntries) { problem ->
                    message = problem ?: CampsiteDetailsText.JOINED
                    lotteryController.getMyLotteries { entries -> myEntries = entries }
                }
            }
        },
        onLeaveClick = {
            if (lottery != null && myEntry != null) {
                lotteryController.leaveLottery(lottery, myEntry) { problem ->
                    message = problem ?: CampsiteDetailsText.LEFT
                    lotteryController.getMyLotteries { entries -> myEntries = entries }
                }
            }
        },
        onRateClick = { stars ->
            searchController.rateCampsite(campsite.id, currentUserId, stars)
            message = CampsiteDetailsText.RATED
        }
    )
}


/**
 * One amenity and its icon.
 * @property name: must match the text stored in [Campsite.amenities], for example "Parking".
 * @property iconRes: the amenity's icon in res/drawable.
 */
data class AmenityIcon(val name: String, val iconRes: ImageVector)

/** Every amenity an owner can pick (same names as OwnerCampsiteDetails), with its icon. */
val amenityDrawables = listOf(
    AmenityIcon("Parking",Icons.Default.LocalParking),
    AmenityIcon("Washrooms", Icons.Default.Wc),
    AmenityIcon("Fire", Icons.Default.LocalFireDepartment),
    AmenityIcon("Trails", Icons.Default.Hiking),
    AmenityIcon("Picnic tables", Icons.Default.TableRestaurant),
    AmenityIcon("Water Tap", Icons.Default.WaterDrop)
)

/**
 * The layout of [CampsiteDetailsScreen].
 * UI tests can show it with a fake campsite and lottery.
 *
 * @param campsite: campsite to show.
 * @param lottery: campsite's active lottery, or `null` to show "No active lottery".
 * @param hasJoined: `true` to show "Leave Lottery", `false` to show "Join Lottery".
 * @param message: message to show, for example why a join was rejected. Empty shows nothing.
 * @param onBackClick: called when the user taps Back.
 * @param onJoinClick: called when the user taps "Join Lottery".
 * @param onLeaveClick: called when the user taps "Leave Lottery".
 * @param onRateClick: called with the star value (1 to 5) when the user taps a star.
 */
@Composable
fun CampsiteDetailsContent(
    campsite: Campsite,
    lottery: Lottery?,
    hasJoined: Boolean,
    message: String,
    onBackClick: () -> Unit,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onRateClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        BackRow(onBackClick = onBackClick)

        AsyncImage(
            model = campsite.promoImageUrl,
            contentDescription = "Photo of ${campsite.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(greyBlue)
        )

        Spacer(Modifier.height(14.dp))
        CampsiteHeader(campsite = campsite, lottery = lottery)

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(message, color = charcoal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        DetailsCard(title = "About this Campsite") {
            Text(campsite.description, fontSize = 13.sp, lineHeight = 19.sp, color = charcoal)
        }

        DetailsCard(title = "Amenities") {
            AmenitiesRow(campsite = campsite)
        }

        DetailsCard(title = null) {
            AvailabilitySection(campsite = campsite)
        }

        DetailsCard(title = "Lottery") {
            LotterySection(
                lottery = lottery,
                hasJoined = hasJoined,
                onJoinClick = onJoinClick,
                onLeaveClick = onLeaveClick
            )
        }

        DetailsCard(title = "Rules") {
            Text(campsite.rules, fontSize = 13.sp, lineHeight = 19.sp, color = charcoal)
        }

        DetailsCard(title = "Cancellation policy") {
            Text(campsite.cancellationPolicy, fontSize = 13.sp, lineHeight = 19.sp, color = charcoal)
        }

        DetailsCard(title = "Map location") {
            Text("${campsite.latitude}, ${campsite.longitude}", fontSize = 13.sp, color = charcoal)
        }

        DetailsCard(title = "Rate this campsite") {
            StarRatingRow(onRateClick = onRateClick)
        }
    }
}

/**
 * "← Back" row at the top of the screen.
 * @param onBackClick: called when the user taps it.
 */
@Composable
fun BackRow(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .testTag(CampsiteDetailsTags.BACK_BUTTON)
            .clickable(onClick = onBackClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = CampsiteDetailsText.BACK,
            modifier = Modifier.size(20.dp),
            tint = charcoal
        )
        Text(
            text = CampsiteDetailsText.BACK,
            color = charcoal,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Name, location and price on the left; lottery badge and rating on the right.
 * @param campsite: campsite to show.
 * @param lottery: campsite's active lottery, or `null`.
 */
@Composable
fun CampsiteHeader(campsite: Campsite, lottery: Lottery?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = campsite.name,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = CampsiteDetailsText.BACK,
                    modifier = Modifier.size(20.dp),
                    tint = charcoal
                )
                Text(campsite.location, color = charcoal, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$${campsite.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = " / night (up to ${campsite.maxOccupancy} guests)",
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LotteryBadge(status = lottery?.status)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.StarBorder,
                    null,
                    Modifier.size(14.dp)
                )
                Text(
                    text = " " + formatCampsiteRating(campsite),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Coloured pill showing the lottery's stage.
 * OPEN → green "Lottery Open", DRAWN → orange "Drawn", CLOSED → red "Closed", none → grey "No Lottery".
 * @param status: the lottery's stage, or `null` if the campsite has no active lottery.
 */
@Composable
fun LotteryBadge(status: LotteryStatus?) {
    val badgeText = when (status) {
        LotteryStatus.OPEN -> "Lottery Open"
        LotteryStatus.DRAWN -> "Drawn"
        LotteryStatus.CLOSED -> "Closed"
        null -> "No Lottery"
    }
    val badgeColor = when (status) {
        LotteryStatus.OPEN -> lightGreen
        LotteryStatus.DRAWN -> lightOrange
        LotteryStatus.CLOSED -> lightRed
        null -> noLotteryGrey
    }
    val textColor = if (status == null) Color(0xFF757575) else Color.White

    Box(modifier = Modifier.background(badgeColor, RoundedCornerShape(10.dp))) {
        Text(
            text = badgeText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            fontSize = 11.sp,
            color = textColor
        )
    }
}

/**
 * Rounded grey-blue card used for each section, like OwnerCampsiteDetails.
 * @param title: bold heading at the top of the card, or `null` for no heading.
 * @param content: what goes inside the card.
 */
@Composable
fun DetailsCard(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(greyBlue)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (title != null) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
        }
        content()
    }
}

/**
 * A scrollable row of icon tiles, one for each amenity the campsite offers.
 * Amenities without a known icon are skipped.
 * @param campsite: campsite whose amenities to show.
 */
@Composable
fun AmenitiesRow(campsite: Campsite) {
    val offeredAmenities = amenityDrawables.filter { amenity -> amenity.name in campsite.amenities }

    if (offeredAmenities.isEmpty()) {
        Text("No amenities listed", fontSize = 13.sp, color = charcoal)
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (amenity in offeredAmenities) {
            AmenityBorder(amenity)
        }
    }
}

/**
 * One rounded light-blue tile with an amenity's icon (same look as OwnerCampsiteDetails).
 * @param amenity: the amenity to show; its name is read aloud by screen readers.
 */
@Composable
fun AmenityBorder(amenity: AmenityIcon) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(lightBlue),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = amenity.iconRes,
            contentDescription = amenity.name,
            modifier = Modifier.size(26.dp),
            tint = charcoal
        )
    }
}

/**
 * The availability calendar. Booked dates are highlighted.
 * The entrant can tap dates to look around; nothing is reserved from here.
 * @param campsite: campsite whose booked dates to show.
 */
@Composable
fun AvailabilitySection(campsite: Campsite) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val bookedDates = remember(campsite.bookedDates) {
        campsite.bookedDates
            .mapNotNull { dateText -> runCatching { LocalDate.parse(dateText) }.getOrNull() }
            .toSet()
    }

    //AvailabilityCalendar(
        //bookedDates = bookedDates,
        //selectedDate = selectedDate,
        //onDateSelected = { date -> selectedDate = date }
    //)
}

/**
 * The lottery card: registration and booking periods, how many have joined,
 * and a Join or Leave button.
 *
 * @param lottery: the active lottery, or `null` to show "No active lottery".
 * @param hasJoined: `true` to show "Leave Lottery", `false` to show "Join Lottery".
 * @param onJoinClick: called when the user taps "Join Lottery".
 * @param onLeaveClick: called when the user taps "Leave Lottery".
 */
@Composable
fun LotterySection(
    lottery: Lottery?,
    hasJoined: Boolean,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    if (lottery == null) {
        Text(CampsiteDetailsText.NO_ACTIVE_LOTTERY, fontSize = 13.sp, color = charcoal)
        return
    }

    val registration = "${formatLotteryDate(lottery.registrationStart)} – ${formatLotteryDate(lottery.registrationEnd)}"
    val booking = "${formatLotteryDate(lottery.bookingStart)} – ${formatLotteryDate(lottery.bookingEnd)}"

    Text("Registration: $registration", fontSize = 13.sp, color = charcoal)
    Text("Booking period: $booking", fontSize = 13.sp, color = charcoal)
    Text("Winners drawn: ${lottery.winnerCount}", fontSize = 13.sp, color = charcoal)
    Text(formatWaitlistSize(lottery), fontSize = 13.sp, color = charcoal)

    Spacer(Modifier.height(12.dp))

    if (hasJoined) {
        Button(
            onClick = onLeaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(CampsiteDetailsTags.LEAVE_BUTTON),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = lightRed, contentColor = Color.White)
        ) {
            Text(CampsiteDetailsText.LEAVE_LOTTERY, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            onClick = onJoinClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(CampsiteDetailsTags.JOIN_BUTTON),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = charcoal)
        ) {
            Text(CampsiteDetailsText.JOIN_LOTTERY, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Five tappable stars. Tapping the 4th star fills stars 1–4 and reports 4.
 * @param onRateClick: called with the star value (1 to 5).
 */
@Composable
fun StarRatingRow(onRateClick: (Int) -> Unit) {
    var chosenStars by remember { mutableIntStateOf(0) }

    Row {
        for (stars in 1..5) {
            val isFilled = stars <= chosenStars
            val starIcon = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarBorder
            IconButton(
                onClick = {
                    chosenStars = stars
                    onRateClick(stars)
                },
                modifier = Modifier.testTag(CampsiteDetailsTags.star(stars))
            ) {
                Icon(
                    imageVector = starIcon,
                    contentDescription = "$stars star",
                    tint = charcoal
                )
            }
        }
    }
}

/**
 * Describes how full the waitlist is.
 * @return for example `"12 joined"`, or `"12 of 50 spots taken"` if there is a cap.
 */
fun formatWaitlistSize(lottery: Lottery): String {
    val cap = lottery.waitlistCap
    if (cap == null) {
        return "${lottery.entrantCount} joined"
    }
    return "${lottery.entrantCount} of $cap spots taken"
}

/**
 * Format time as a short date.
 * @return for example `"Nov 3, 2026"`.
 */
fun formatLotteryDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return formatter.format(Date(timeMillis))
}

/** Text. Tests compare against these. */
object CampsiteDetailsText {
    const val BACK = "Back"
    const val NO_ACTIVE_LOTTERY = "No active lottery"
    const val JOIN_LOTTERY = "Join Lottery"
    const val LEAVE_LOTTERY = "Leave Lottery"
    const val JOINED = "You joined the lottery waitlist."
    const val LEFT = "You left the lottery waitlist."
    const val RATED = "Thanks for rating!"
}

/** Test tags on [CampsiteDetailsScreen]. for UI tests */
object CampsiteDetailsTags {
    const val BACK_BUTTON = "backButton"
    const val JOIN_BUTTON = "joinButton"
    const val LEAVE_BUTTON = "leaveButton"

    /**
     * Tag of one rating star.
     * @param stars: which star, 1 to 5.
     * @return for example `"star_4"`.
     */
    fun star(stars: Int): String {
        return "star_$stars"
    }
}