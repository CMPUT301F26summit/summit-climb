package com.example.summit_climb.ui.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.summit_climb.controller.OwnerReservationController
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog

// Contains reservation information displayed on the owner's.
data class ReservationUiItem(
    val id: String,
    val entrantName: String,
    val dates: String,
    val guests: Int,
    val status: String,
    val entrantId: String = "",
    val campsiteId: String = ""
)

@Composable
fun OwnerReservationsFromFirestore(
    campsiteId: String,
    controller: OwnerReservationController,
    onReservationClick: (String) -> Unit
) {
    var reservations by remember { mutableStateOf(emptyList<ReservationUiItem>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    fun loadReservations() {
        loading = true
        error = null

        controller.getReservationsByCampsite(
            campsiteId = campsiteId,
            onSuccess = {result ->
                reservations = result.map { reservation ->
                    ReservationUiItem(
                        id = reservation.id,
                        entrantName = reservation.entrantName,
                        dates = "${reservation.startDate} - ${reservation.endDate}",
                        guests = reservation.guests,
                        entrantId = reservation.entrantId,
                        campsiteId = reservation.campsiteId,

                        status = if (
                            reservation.status == "ACTIVE" &&
                            reservation.endDate < java.text.SimpleDateFormat(
                                "yyyy-MM-dd",
                                java.util.Locale.US
                            ).format(java.util.Date())
                        ){
                            "COMPLETED"
                        } else {
                            reservation.status
                        }
                    )
                }
                loading = false
            },
            onFailure = { exception ->
                error = exception.message
                loading = false
            }
        )
    }
    LaunchedEffect(campsiteId) { loadReservations() }

    Column {
        if (loading) {
            Text("Loading reservations...")
        }
        else if (error != null) {
            Text("Error: $error")
        }

        if (!loading && error == null) {
            OwnerReservationsScreen(
                reservations = reservations,
                onReservationClick = onReservationClick,
                onRejectClick = { reservation ->
                    controller.rejectReservation(
                        reservationId = reservation.id,
                        entrantId = reservation.entrantId,
                        campsiteId = reservation.campsiteId,

                        onSuccess = { loadReservations() },
                        onFailure = { exception ->
                            error = exception.message ?: "Could not reject reservation"
                        }
                    )
                }
            )
        }
    }
}
@Composable
fun OwnerReservationsScreen(
    reservations: List<ReservationUiItem>,
    onReservationClick: (String) -> Unit,
    onRejectClick: (ReservationUiItem) -> Unit
) {
    val tabs = listOf("Active", "Past", "Cancelled")

    var selectedTab by remember {mutableStateOf("Active")}

    var reservationToReject by remember {
        mutableStateOf<ReservationUiItem?>(null)
    }

    val activeCount = reservations.count {it.status == "ACTIVE"}

    // Filter reservations based on the selected tab
    val filteredReservations = reservations.filter { reservation ->
        when (selectedTab) {
            "Active" -> reservation.status == "ACTIVE"
            "Past" -> reservation.status == "COMPLETED"
            "Cancelled" -> reservation.status == "CANCELLED" ||
                    reservation.status == "REJECTED"
            else -> false
        }
    }

    val backgroundColor = Color(0xFFE6E6E6)
    val tabBackground = Color(0xFFD6D5D2)
    val textColor = Color(0xFF1E1E1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // Screen title
        Text(
            text = "Reservations",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active, Past, Cancelled tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = tabBackground,
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab

                val tabText = if (tab == "Active") {
                    "Active ($activeCount)"
                } else {tab}

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) Color.White
                            else Color.Transparent,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabText,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold
                        else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reservation list
        if (filteredReservations.isEmpty()) {
            Text(
                text = "No ${selectedTab.lowercase()} reservations.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredReservations,
                    key = { it.id }
                ) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = {
                            onReservationClick(reservation.id)
                        },
                        onReject = {
                            reservationToReject = reservation
                        }
                    )
                }
            }
        }
    }

    // Confirmation dialog before rejecting a reservation
    reservationToReject?.let { reservation ->
        Dialog(
            onDismissRequest = {
                reservationToReject = null
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Red rejection icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFF8E5E3),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⊗",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8C0505)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Reject reservation?",
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reservation information
                    Text(
                        text ="${reservation.entrantName} · ${reservation.dates}",
                        fontSize = 17.sp,
                        color = Color(0xFF555555),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The guest will be notified and the dates " +
                                "will open up again.",
                        fontSize = 15.sp,
                        color = Color(0xFF777777),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                reservationToReject = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(
                                2.dp,
                                Color(0xFFD8D8D8)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFFFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onRejectClick(reservation)
                                reservationToReject = null
                                selectedTab = "Cancelled"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFBD3C32),
                                contentColor = Color(0xFFFCFCFC)
                            )
                        ) {
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays a reservation card with entrant details and status.
 * Active reservations include a Reject button.
 */
@Composable
private fun ReservationCard(
    reservation: ReservationUiItem,
    onClick: () -> Unit,
    onReject: () -> Unit
) {
    val isActive = reservation.status == "ACTIVE"

    val initials = reservation.entrantName
        .trim()
        .split(" ")
        .filter{it.isNotBlank()}
        .take(2)
        .joinToString("") { it.first().uppercase()}

    val avatarColor = if (isActive) {
        Color(0xFFE3EDDF)
    } else {
        Color(0xFFEEEEEE)
    }

    val avatarTextColor = if (isActive) {
        Color(0xFF456B43)
    } else {
        Color(0xFF666666)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = avatarColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reservation.entrantName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${reservation.dates} · ${reservation.guests} guests",
                        fontSize = 18.sp,
                        color = Color(0xFF777777),
                        maxLines = 2
                    )

                    if (!isActive) {
                        Spacer(modifier = Modifier.height(4.dp))

                        val statusText = when (reservation.status) {
                            "COMPLETED" -> "Completed"
                            "REJECTED" -> "Rejected by you"
                            "CANCELLED" -> "Cancelled by guest"
                            else -> reservation.status
                        }

                        val badgeBackground = when (reservation.status) {
                            "COMPLETED" -> Color(0xFFE3EDDF)
                            "REJECTED" -> Color(0xFFF8E5E3)
                            else -> Color(0xFFE7E7E7)
                        }

                        val badgeTextColor = when (reservation.status) {
                            "COMPLETED" -> Color(0xFF456B43)
                            "REJECTED" -> Color(0xFFB53C32)
                            else -> Color(0xFF666666)
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = badgeBackground,
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                )
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = badgeTextColor
                            )
                        }
                    }
                }
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFBD3C32)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFBD3C32)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Reject",
                            fontSize = 17.sp,
                        )
                    }
                }
            }
        }
    }
}

// Sample data for previewing the screen
@Preview(showBackground = true)
@Composable
private fun OwnerReservationsPreview() {

    val sampleReservations = remember {
        mutableStateListOf(
            ReservationUiItem(
                id = "1",
                entrantName = "Ava Thompson",
                dates = "November 9–11, 2026",
                guests = 4,
                status = "ACTIVE"
            ),
            ReservationUiItem(
                id = "2",
                entrantName = "Liam Chen",
                dates = "November 17–19, 2026",
                guests = 2,
                status = "ACTIVE"
            ),
            ReservationUiItem(
                id = "3",
                entrantName = "Sophia Martin",
                dates = "September 12–14, 2026",
                guests = 3,
                status = "COMPLETED"
            ),
            ReservationUiItem(
                id = "4",
                entrantName = "Emma Wilson",
                dates = "October 3–5, 2026",
                guests = 2,
                status = "CANCELLED"
            ),
            ReservationUiItem(
                id = "5",
                entrantName = "Noah Patel",
                dates = "October 20–22, 2026",
                guests = 4,
                status = "REJECTED"
            )
        )
    }
    MaterialTheme {
        OwnerReservationsScreen(
            reservations = sampleReservations,
            onReservationClick = {},
            onRejectClick = {reservation ->
                val index = sampleReservations.indexOfFirst {
                    it.id == reservation.id
                }

                if (index != -1) {
                    sampleReservations[index] = reservation.copy(
                        status = "REJECTED"
                    )
                }
            }
        )
    }
}