package com.example.summit_climb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.summit_climb.ui.theme.SummitclimbTheme

class MainActivity : ComponentActivity() {

    // the screen a tapped notification wants to open (null = none)
    private var pendingScreen by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AppContainer()

        pendingScreen = intent?.getStringExtra(SystemNotifier.EXTRA_SCREEN)

        setContent {
            SummitclimbTheme {
                RequestNotificationPermission()  // temporary: need to test
                AppNavigation(container)
            }
        }
    }

    // called when a notification is tapped while the app is already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingScreen = intent.getStringExtra(SystemNotifier.EXTRA_SCREEN)
    }

}
