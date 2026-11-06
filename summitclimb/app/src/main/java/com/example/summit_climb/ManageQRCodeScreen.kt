package com.example.summit_climb

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun ManageQRCodeScreen (
    navController: NavController,
    campsiteName: String = "Campsite",
    campsiteID: String = "sample-id"
) {
    val qrService = remember { QRCodeService() }
    val qrCode = remember(campsiteID) {
        qrService.generateQRCode(campsiteID)
    }

    Column(
        modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFAF3EB))
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row () {
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(
                    text = "←  Back",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "QR Code",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Guests can scan this to open your campsite",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (qrCode != null) {
                    Image(
                        bitmap = qrCode.asImageBitmap(),
                        contentDescription = "QR code for $campsiteName",
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    Text(
                        text = "Sorry, the QR Code for this campsite is not available yet",
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = campsiteName,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Campsite ID: $campsiteID",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* WIP: save image function */ },
                modifier = Modifier.weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF96c6c6)
                )
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Button(
                onClick = { /* WIP: share image function */ },
                modifier = Modifier.weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2f7d6d)
                )
            ) {
                Text(
                    text = "Share",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }

}
