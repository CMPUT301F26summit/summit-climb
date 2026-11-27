package com.example.summit_climb.ui.owner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Column
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
import com.example.summit_climb.service.QRCodeService
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import com.example.summit_climb.ui.ScreensBackground
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.summit_climb.R
import com.example.summit_climb.service.saveQRCodeToGallery
import com.example.summit_climb.service.shareQRCode

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.entrant.BackRow
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.theme.screenBackground


/**
 * Purpose: displays the unique QR code for each campsite
 * Allows users to save QR code to device gallery
 * @param navController: app's navigation controller, used to open other screens
 * @param campsiteID: the selected campsite's ID
 */

@Composable
fun ManageQRCodeScreen(
    navController: NavController,
    campsiteID: String = ""
) {
    val context = LocalContext.current
    val qrService = remember { QRCodeService() }
    val qrCode = remember(campsiteID) {
        qrService.generateQRCode(campsiteID)
    }
    var message by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
            message = ""
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(screenBackground)) {
        Image(
            painter = painterResource(R.drawable.mountain_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp)
        ) {
            BackRow(onBackClick = { navController.popBackStack() })
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 58.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "QR Code",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // displays the QR code
                    if (qrCode != null) {
                        Image(
                            bitmap = qrCode.asImageBitmap(),
                            contentDescription = "QR code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text(
                            text = "Sorry, the QR Code for this campsite is not available yet",
                            color = Color.DarkGray
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Campsite ID: $campsiteID",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "Your guests can scan this QR Code to see campsite details.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = charcoal,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(25.dp))

            // save QR code button
            Button(
                onClick = {
                    if (qrCode == null) {
                        message = "QR Code is not available"
                    } else {
                        val saved = runCatching {
                            saveQRCodeToGallery(context, qrCode, "QR_$campsiteID")
                        }.getOrDefault(false)

                        message = if (saved) {
                            "QR Code saved to gallery"
                        } else {
                            "Failed to save QR Code"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greyBlue
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = charcoal
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (qrCode == null) {
                        message = "QR Code is not available"
                    } else {
                        if (!shareQRCode(context, qrCode, "QR_$campsiteID")) {
                            message = "Failed to share QR Code"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greyBlue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    text = "Share",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = charcoal
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
