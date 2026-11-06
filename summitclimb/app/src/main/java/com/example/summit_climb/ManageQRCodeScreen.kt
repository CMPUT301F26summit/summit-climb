package com.example.summit_climb

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon

// Summit Climb colors
private val black = Color.Black
private val charcoal = Color(0xFF36454F)
private val lightBlue = Color(0xFFA6C8EB)
private val lightGrey = Color(0xFFDADAD8)
private val greyBlue = Color(0xFFC9DEF3)
private val white = Color.White

// Displays the unique campsite QR code for a campsite owner
@Composable
fun ManageQRCodeScreen(
    navController: NavController,
    campsiteName: String,
    campsiteID: String
) {

    // Obtains a generated QR code for the current campsite
    val qrService = remember { QRCodeService() }
    val qrCode = remember(campsiteID) {
        qrService.generateQRCode(campsiteID)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Back Button
        val backButton = { navController.popBackStack(); Unit }
        Row(
            Modifier
                .clickable(onClick = backButton)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Button",
                modifier = Modifier.size(20.dp),
                tint = charcoal
            )

            Text(
                text = "Back",
                color = charcoal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)

            )
        }

        Spacer(Modifier.height(8.dp))

        // QR Code Description
        Text(
            text = "QR Code",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = black
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Guests can scan this to open your campsite",
            fontSize = 14.sp,
            color = charcoal
        )

        Spacer(Modifier.height(16.dp))

        // Displays the QR code
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = greyBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        color = charcoal
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = campsiteName,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = black
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Campsite ID: $campsiteID",
                    color = charcoal,
                    fontSize = 13.sp
                )
            }
        }
    }
}


