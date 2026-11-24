
package com.example.summit_climb.ui.owner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.model.CampsiteFilter
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.ui.ScreensBackground

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightGreen
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.lightOrange
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.black
import com.example.summit_climb.ui.theme.screenBackground
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Entrant(
    val id: String,
    val name: String,
    val joinedDate: String
)

@Composable
fun WaitlistScreen(
    campsiteId: String,
    currentUserId: String,
    searchController: CampsiteSearchController,
    ownerLotteryController: OwnerLotteryController,
    onBack: () -> Unit,
    navController: NavController
) {
    // Find the lotteries belonging to this campsite.
    val campsiteLotteries = ownerLotteryController
        .lotteriesFor(currentUserId)
        .filter { campsiteId in it.campsiteIds }

    // Prefer the latest open lottery.
    // If none is open, show the latest drawn or closed lottery.
    val selectedLottery = campsiteLotteries
        .filter { it.status == LotteryStatus.OPEN }
        .maxByOrNull { it.registrationEnd }
        ?: campsiteLotteries
            .filter {
                it.status == LotteryStatus.DRAWN ||
                        it.status == LotteryStatus.CLOSED
            }
            .maxByOrNull { it.registrationEnd }

    // Listen for changes to the selected lottery's waitlist.
    var waitlistEntries by remember(selectedLottery?.id) {
        mutableStateOf<List<WaitlistEntry>>(emptyList())
    }

    DisposableEffect(selectedLottery?.id) {
        val lotteryId = selectedLottery?.id

        if (lotteryId != null) {
            val stopListening =
                ownerLotteryController.listenToWaitlist(lotteryId) { entries ->
                    waitlistEntries = entries
                }

            onDispose {
                stopListening()
            }
        } else {
            onDispose { }
        }
    }

    // Load entrant names from Firestore.
    var entrants by remember(selectedLottery?.id) {
        mutableStateOf<List<Entrant>>(emptyList())
    }

    LaunchedEffect(waitlistEntries) {
        // Initially display each entrant as loading.
        entrants = waitlistEntries.map { entry ->
            Entrant(
                id = entry.entrantId,
                name = "Loading...",
                joinedDate = if (entry.joinedAt > 0L) {
                    SimpleDateFormat("MMM d", Locale.getDefault())
                        .format(Date(entry.joinedAt))
                } else {
                    "Unknown"
                }
            )
        }

        // Retrieve each entrant's name from their user document.
        waitlistEntries.forEach { entry ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(entry.entrantId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                        ?: "Unknown entrant"

                    // Only update entrants who are still on the waitlist.
                    if (waitlistEntries.any {
                            it.entrantId == entry.entrantId
                        }) {
                        entrants = entrants.map { entrant ->
                            if (entrant.id == entry.entrantId) {
                                entrant.copy(name = name)
                            } else {
                                entrant
                            }
                        }
                    }
                }
        }
    }

    // Find the campsite details.
    val campsite = searchController.campsiteDetails(campsiteId)
        ?: searchController.search("", CampsiteFilter())
            .find { it.id == campsiteId }

    // Display the screen.
    Box(modifier = Modifier
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

        WaitlistContent(
            campsiteName = campsite?.name ?: "Campsite",
            entrants = entrants,
            lotteryStatus = when (selectedLottery?.status) {
                LotteryStatus.OPEN -> "Lottery Open"
                LotteryStatus.DRAWN -> "Winners Drawn"
                LotteryStatus.CLOSED -> "Lottery Closed"
                else -> "No Lottery"
            },
            entrantCount = waitlistEntries.size,
            winnersDrawnDate = "—",
            maxEntrants = selectedLottery?.waitlistCap,
            navController = navController,
            onBack = onBack,
            onNotifyEntrants = {
                // TODO: Connect notification functionality.
            }
        )
    }
}

@Composable
fun WaitlistContent(
    campsiteName: String,
    entrants: List<Entrant>,
    lotteryStatus: String,
    entrantCount: Int,
    winnersDrawnDate: String,
    navController: NavController,
    maxEntrants: Int?,
    onBack: () -> Unit,
    onNotifyEntrants: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Back button.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x=(-12).dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Back",
                fontSize = 15.sp,
                color = black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }

        // Waitlist title in a white box.
        Text(
            text = "Waitlist",
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
            color = black,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )


        // Campsite information box.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = campsiteName,
                    color = black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val statusColor = when (lotteryStatus) {
                    "Lottery Open" -> lightGreen
                    "Lottery Closed" -> lightRed
                    "Winners Drawn" -> lightOrange
                    else -> greyBlue
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = lotteryStatus,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        color = black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (lotteryStatus == "Winners Drawn") {
                    Text(
                        text = "Winners drawn: $winnersDrawnDate",
                        color = charcoal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics box.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = entrantCount.toString(),
                        color = charcoal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Entrants",
                        color = charcoal,
                        fontSize = 13.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(45.dp),
                    color = greyBlue,
                    thickness = 1.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = maxEntrants?.toString() ?: "—",
                        color = charcoal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Maximum Entrants",
                        color = charcoal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Entrants",
            color = black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // One white box containing every entrant.
        // If the list is empty, show an empty-state message.
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (entrants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entrants yet",
                        color = charcoal,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    itemsIndexed(entrants) { index, entrant ->
                        EntrantRow(entrant = entrant)

                        if (index < entrants.lastIndex) {
                            HorizontalDivider(
                                color = greyBlue,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notify Entrants section at the bottom.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = greyBlue
            )
        ) {
            Button(
                onClick = onNotifyEntrants,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightBlue
                )
            ) {
                Text(
                    text = "Notify Entrants",
                    color = black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EntrantRow(
    entrant: Entrant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Entrant initials.
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = lightBlue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val initials = entrant.name
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") {
                    it.first().uppercase()
                }

            Text(
                text = initials,
                color = black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = entrant.name,
                color = charcoal,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Joined ${entrant.joinedDate}",
                color = charcoal,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitlistScreenPreview() {
    WaitlistContent(
        campsiteName = "Mountain View Campsite",
        entrants = emptyList(),
        lotteryStatus = "Lottery Open",
        entrantCount = 0,
        winnersDrawnDate = "—",
        maxEntrants = 10,
        onBack = {},
        navController = rememberNavController(),
        onNotifyEntrants = {}
    )
}
