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

        setContent {
            SummitclimbTheme {
                AppNavigation(container)
            }
        }
    }

}
