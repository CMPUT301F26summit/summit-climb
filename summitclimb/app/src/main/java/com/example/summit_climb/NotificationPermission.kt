package com.example.summit_climb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Purpose: asks the user once for permission to show notifications
// Calls it from the notification screen, so the question appears after login,
// when the user taps the notifications icon.

/** Shows the Android "Allow notifications?" pop-up the first time it is composed. */
@Composable
fun RequestNotificationPermission() {
    // nothing to ask on Android 12 and older
    if (Build.VERSION.SDK_INT < 33) return

    val context = LocalContext.current

    // the pop-up asking "Allow Summit Climb to send notifications?"
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied: SystemNotifier checks again before showing anything */ }

    // runs once when the screen opens
    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}