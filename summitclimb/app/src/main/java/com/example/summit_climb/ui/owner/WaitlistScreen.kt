package com.example.summit_climb.ui.owner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.draw.innerShadow
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ModalBottomSheet

import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.model.CampsiteFilter
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.model.WaitlistEntry
import com.example.summit_climb.R
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.ui.StatusBadge

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightGreen
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.lightOrange
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.black
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
    notificationController: NotificationController,
    onBack: () -> Unit
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
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        WaitlistContent(
            campsiteName = campsite?.name ?: "Campsite",
            location = campsite?.location ?: "Location unavailable",
            entrants = entrants,
            lotteryStatus = when (selectedLottery?.status) {
                LotteryStatus.OPEN -> "Lottery Open"
                LotteryStatus.DRAWN -> "Winners Drawn"
                LotteryStatus.CLOSED -> "Lottery Closed"
                else -> "No Lottery"
            },
            entrantCount = waitlistEntries.size,
            winnersDrawnDate = selectedLottery?.registrationEnd?.let {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
            } ?: "—",
            maxEntrants = selectedLottery?.waitlistCap,
            onBack = onBack,
            onSendNotification = { title, message ->
                notificationController.sendToEntrants(
                    campsiteId = campsiteId,
                    recipientIds = waitlistEntries.map { it.entrantId },
                    title = title,
                    message = message
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitlistContent(
    campsiteName: String,
    location: String,
    entrants: List<Entrant>,
    lotteryStatus: String,
    entrantCount: Int,
    winnersDrawnDate: String,
    maxEntrants: Int?,
    onBack: () -> Unit,
    onSendNotification: (String, String) -> Unit
) {
    // Controls whether the notification panel is visible
    var showNotifySheet by remember { mutableStateOf(false) }

    // Stores the title and message entered by the owner
    var notificationTitle by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }

    // Colour depends on the lottery status
    val statusColor = when (lotteryStatus) {
        "Open", "Lottery Open" -> lightGreen
        "Drawn", "Winners Drawn" -> lightOrange
        "Closed", "Lottery Closed" -> lightRed
        else -> charcoal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8E8F3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Mountain header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.mountain_header),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x660F2938))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 14.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Back button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Text(
                            text = "Back",
                            color = black,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onBack() }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Waitlist",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF233B4D)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = campsiteName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B8FA8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = " • ",
                            fontSize = 15.sp,
                            color = Color(0xFF36454F)
                        )

                        Text(
                            text = location,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B8FA8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        StatusBadge(
                            text = lotteryStatus,
                            color = statusColor
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(6.dp))

            // FIRST BOX: Entrants Joined and Winners Drawn
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = entrantCount.toString(),
                                color = Color(0xFF496B83),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = " / ${maxEntrants ?: "—"}",
                                color = Color(0xFF9AABB7),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "Entrants Joined",
                            color = charcoal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(48.dp),
                        color = greyBlue,
                        thickness = 1.dp
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = winnersDrawnDate,
                            color = Color(0xFF496B83),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Winners Drawn",
                            color = charcoal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // SECOND BOX: Entrants and Notify Entrants
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Entrants",
                        color = Color(0xFF233B4D),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (entrants.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        color = greyBlue,
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notify Entrants button
                    Button(
                        onClick = {
                            notificationTitle = ""
                            notificationMessage = ""
                            showNotifySheet = true
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = lightBlue,
                            contentColor = black,
                            disabledContainerColor = greyBlue,
                            disabledContentColor = charcoal
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Notify entrants",
                            tint = black
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Notify Entrants",
                            color = black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notification pull-up panel
        if (showNotifySheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showNotifySheet = false
                },
                containerColor = Color(0xFFD8E8F3),
                dragHandle = {
                    BottomSheetDefaults.DragHandle(
                        color = Color(0xFF6B8FA8)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        )
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // panel heading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notify Entrants",
                            color = Color(0xFF233B4D),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                showNotifySheet = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = charcoal
                            )
                        }
                    }
                    // White form card.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Recipient row: To + icon + entrant count.
                            Text(
                                text = "To:",
                                color = charcoal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFEAF4FC),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Entrants",
                                    tint = Color(0xFF245C86)
                                )

                                Text(
                                    text = "All $entrantCount entrants",
                                    color = Color(0xFF233B4D),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Notification title.
                            Text(
                                text = "Title",
                                color = charcoal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = notificationTitle,
                                onValueChange = {
                                    notificationTitle = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Enter notification title")
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8FA8),
                                    unfocusedBorderColor = greyBlue,
                                    focusedTextColor = charcoal,
                                    unfocusedTextColor = charcoal
                                )
                            )

                            // Notification message.
                            Text(
                                text = "Message",
                                color = charcoal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = notificationMessage,
                                onValueChange = {
                                    notificationMessage = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                placeholder = {
                                    Text("Write your message to the entrants...")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8FA8),
                                    unfocusedBorderColor = greyBlue,
                                    focusedTextColor = charcoal,
                                    unfocusedTextColor = charcoal
                                )
                            )

                            // Send notification button.
                            Button(
                                onClick = {
                                    onSendNotification(
                                        notificationTitle.trim(),
                                        notificationMessage.trim()
                                    )
                                    showNotifySheet = false
                                },
                                enabled = notificationTitle.isNotBlank() &&
                                        notificationMessage.isNotBlank() &&
                                        entrantCount > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = lightBlue,
                                    contentColor = black,
                                    disabledContainerColor = greyBlue,
                                    disabledContentColor = charcoal
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send notification",
                                    tint = black
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Send to $entrantCount entrants",
                                    color = black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
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
        campsiteName = "Summit Camp",
        location = "Banff, AB",
        entrants = listOf(
            Entrant("1", "Alex Morgan", " Nov 18, 2026"),
            Entrant("2", "Jamie Wilson", " Nov 17, 2026"),
            Entrant("3", "Taylor Smith", " Nov 16, 2026"),
            Entrant("4", "Jordan Lee", " Nov 15, 2026"),
            Entrant("5", "Casey Brown", " Nov 14, 2026")
        ),
        lotteryStatus = "Lottery Open",
        entrantCount = 12,
        winnersDrawnDate = "Nov 24",
        maxEntrants = 50,
        onBack = {},
        onSendNotification = { _, _ -> }
    )
}
