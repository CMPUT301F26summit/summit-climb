package com.example.summit_climb.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.summit_climb.ui.charcoal

private val campsiteBlue = Color(0xFF245C86)

/**
 * Shown while the app waits for campsite data to verify a scanned QR code.
 */
@Composable
fun CheckingCampsiteDialog() {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "Checking campsite...",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = campsiteBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Please wait while we verify your QR code.",
                    fontSize = 14.sp,
                    color = charcoal,
                    textAlign = TextAlign.Center
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(42.dp),
                    color = campsiteBlue,
                    strokeWidth = 4.dp
                )
            }
        },
        confirmButton = {}
    )
}

/**
 * Shown when the scanned QR code does not match an existing campsite.
 *
 * @param errorMessage: explanation of why the campsite could not be found.
 * @param onTryAgain: called when the user wants to scan another QR code.
 * @param onDismiss: called when the user returns to the home page.
 */
@Composable
fun CampsiteNotFoundDialog(
    errorMessage: String,
    onTryAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Campsite not found",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = campsiteBlue
            )
        },
        text = {
            Text(
                text = "$errorMessage Try again or return to the home page.",
                fontSize = 14.sp,
                color = charcoal
            )
        },
        confirmButton = {
            Button(
                onClick = onTryAgain,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = campsiteBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Try again",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Back to home",
                    color = campsiteBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * Shown when a scanned QR code matches an existing campsite.
 *
 * The promotional image is only displayed when an image URL is available.
 *
 * @param campsiteName: name of the campsite that was found.
 * @param imageUrl: URL of the campsite's promotional image, if available.
 * @param onShowDetails: called when the user opens the campsite details.
 * @param onSaveQrCode: called when the user saves the campsite QR code.
 * @param onDismiss: called when the user closes the dialog.
 */
@Composable
fun CampsiteFoundDialog(
    campsiteName: String,
    imageUrl: String?,
    onShowDetails: () -> Unit,
    onSaveQrCode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Campsite Found!",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = campsiteBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = campsiteName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = charcoal,
                    textAlign = TextAlign.Center
                )

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "$campsiteName campsite photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onShowDetails,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = campsiteBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Show Details")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onSaveQrCode) {
                        Text(
                            text = "Save QR Code",
                            color = campsiteBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "OK",
                            color = campsiteBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    )
}