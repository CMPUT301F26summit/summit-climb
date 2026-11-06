package com.example.summit_climb

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.WaitlistEntry


/**
 * The entrant's campsite details screen.
 * Shows the campsite's full details, its map location, its active lottery (or
 * "No active lottery"), a Join or Leave Lottery button, and 1–5 star rating buttons.
 *
 * @param campsiteId: ID of the campsite to show.
 * @param currentUserId: [User.userId] of the entrant using the app.
 * @param searchController: loads campsite and saves ratings.
 * @param lotteryController: loads lottery and joins or leaves it.
 */
@Composable
fun CampsiteDetailsScreen(
    campsiteId: String,
    currentUserId: String,
    searchController: CampsiteSearchController,
    lotteryController: EntrantLotteryController
) {
    var myEntries by remember { mutableStateOf(listOf<WaitlistEntry>()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        lotteryController.getMyLotteries{ entries -> myEntries = entries}
    }

    val campsite = searchController.campsiteDetails(campsiteId)
    if (campsite == null){
        Text("Loading...", modifier = Modifier.safeDrawingPadding().padding(16.dp))
        return
    }

    val lottery = campsite.activeLotteryId?.let { lotteryId -> lotteryController.getLottery(lotteryId) }
    val myEntry = myEntries.find { entry -> entry.lotteryId == lottery?.id }

    CampsiteDetailsContent(
        campsite = campsite,
        lottery = lottery,
        hasJoined = myEntry != null,
        message = message,
        onJoinClick = {
            if (lottery != null) {
                val problem = lotteryController.joinLottery(lottery, myEntries) { problem ->
                    message = problem ?: CampsiteDetailsText.JOINED
                }
                lotteryController.getMyLotteries { entries -> myEntries = entries }
            }
        },
        onLeaveClick = {
            if (lottery != null && myEntry != null) {
                val problem = lotteryController.leaveLottery(lottery, myEntry) {
                    problem -> message = problem ?: CampsiteDetailsText.LEFT
                }
                lotteryController.getMyLotteries { entries -> myEntries = entries }
            }
        },
        onRateClick = { stars ->
            searchController.rateCampsite(campsite.id, currentUserId, stars)
            message = CampsiteDetailsText.RATED
        }
    )
}

/**
 * The layout of [CampsiteDetailsScreen].
 * UI tests can show it with a fake campsite and lottery.
 *
 * @param campsite: campsite to show.
 * @param lottery: campsite's active lottery, or `null` to show "No active lottery".
 * @param hasJoined: `true` to show "Leave Lottery", `false` to show "Join Lottery".
 * @param message: message to show, for example why a join was rejected. Empty shows nothing.
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
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onRateClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (campsite.promoImageUrl.isNotBlank()) {
            AsyncImage(
                model = campsite.promoImageUrl,
                contentDescription = "Photo of ${campsite.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Text(campsite.name, style = MaterialTheme.typography.headlineSmall)
        Text(campsite.location)
        Text("Up to ${campsite.maxOccupancy} guests")
        Text(formatCampsiteRating(campsite))
        Text("Price: $" + String.format(Locale.US, "%.2f", campsite.price))

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(campsite.description)
        Text("Amenities", style = MaterialTheme.typography.titleMedium)
        Text(campsite.amenities.joinToString(", "))
        Text("Rules", style = MaterialTheme.typography.titleMedium)
        Text(campsite.rules)
        Text("Cancellation policy", style = MaterialTheme.typography.titleMedium)
        Text(campsite.cancellationPolicy)

        // map component (LocationService) when it is ready.
        Text("Map location", style = MaterialTheme.typography.titleMedium)
        Text("${campsite.latitude}, ${campsite.longitude}")

        Text("Lottery", style = MaterialTheme.typography.titleMedium)
        if (lottery == null) {
            Text(CampsiteDetailsText.NO_ACTIVE_LOTTERY)
        } else {
            Text("Registration: ${formatLotteryDate(lottery.registrationStart)} – ${formatLotteryDate(lottery.registrationEnd)}")
            Text("Booking period: ${formatLotteryDate(lottery.bookingStart)} – ${formatLotteryDate(lottery.bookingEnd)}")
            if (hasJoined) {
                Button(onClick = onLeaveClick) { Text(CampsiteDetailsText.LEAVE_LOTTERY) }
            } else {
                Button(onClick = onJoinClick) { Text(CampsiteDetailsText.JOIN_LOTTERY) }
            }
        }

        Text("Rate this campsite", style = MaterialTheme.typography.titleMedium)
        Row {
            for (stars in 1..5) {
                TextButton(onClick = { onRateClick(stars) }) { Text("$stars ★") }
            }
        }
    }
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
    const val NO_ACTIVE_LOTTERY = "No active lottery"
    const val JOIN_LOTTERY = "Join Lottery"
    const val LEAVE_LOTTERY = "Leave Lottery"
    const val JOINED = "You joined the lottery waitlist."
    const val LEFT = "You left the lottery waitlist."
    const val RATED = "Thanks for rating!"
}