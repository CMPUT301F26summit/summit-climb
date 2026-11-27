package com.example.summit_climb.ui.owner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun OwnerReservationDetailsScreen(
    entrantName: String,
    dates: String,
    nights: Int,
    guests: Int,
    totalPrice: String,
    status: String,
    rulesAccepted: Boolean?,
    onMessageClick: (String) -> Unit,
    onRejectClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onConversationClick: (() -> Unit)? = null
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val textColor = Color(0xFF233B4D)
    val headerColor = Color(0xFFCBE3F0)
    val buttonColor = Color(0xFF245C86)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(20.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Back", color = textColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reservation Details",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF4F8FB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = entrantName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when (status) {
                            "ACTIVE" -> "Confirmed"
                            "CANCELLED" -> "Cancelled"
                            "REJECTED" -> "Rejected"
                            "COMPLETED" -> "Completed"
                            else -> status
                        },
                        color = if (status == "ACTIVE")
                            Color(0xFF456B43)
                        else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(
                        color = Color(0xFFD5E3EC)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ReservationDetailRow("Dates", dates)
                    ReservationDetailRow("Length of stay", "$nights nights")
                    ReservationDetailRow("Guests", guests.toString())
                    ReservationDetailRow("Total price", totalPrice)

                    HorizontalDivider(
                        color = Color(0xFFD5E3EC)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReservationDetailRow(
                        "Campsite rules",
                        when (rulesAccepted) {
                            true -> "Accepted"
                            false -> "Not accepted"
                            null -> "Not available"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (onConversationClick != null) {
                        onConversationClick()
                    } else {
                        showMessageDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            ) {
                Text("Message Entrant")
            }

            if (status == "ACTIVE") {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFBD3C32)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFBD3C32)
                    )
                ) {
                    Text("Reject Reservation")
                }
            }
        }
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = {
                showMessageDialog = false
            },
            title = {
                Text("Message Entrant")
            },
            text = {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    enabled = message.isNotBlank(),
                    onClick = {
                        onMessageClick(message.trim())
                        message = ""
                        showMessageDialog = false
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMessageDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject reservation?") },
            text = {
                Text("Are you sure you want to reject this reservation?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectClick()
                        showRejectDialog = false
                    }
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReservationDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF647887)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF233B4D)
        )
    }
}