package com.example.summit_climb

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import com.example.summit_climb.model.Notification
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.model.NotificationType
import com.example.summit_climb.ui.theme.SummitclimbTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AppContainer(applicationContext)

        setContent {
            SummitclimbTheme {
                    AppNavigation(container)
            }
        }
    }
}
