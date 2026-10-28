package com.example.summit_climb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.summit_climb.data.FirestoreNotificationRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.ui.theme.SummitclimbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AppContainer()

        setContent {
            SummitclimbTheme {
                    AppNavigation(container)
            }
        }
    }
}