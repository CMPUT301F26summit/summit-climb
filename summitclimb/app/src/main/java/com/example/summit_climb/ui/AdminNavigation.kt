package com.example.summit_climb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.summit_climb.ui.admin.AdminDashboardScreen
import com.example.summit_climb.ui.admin.AdminLogsScreen
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.systemBarsPadding


@Composable
fun AdminNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ADMIN_DASHBOARD,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .systemBarsPadding()
    ) {
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(navController)
        }

        composable(Routes.ADMIN_CAMPSITES) {
            AdminPlaceholder(
                title = "Manage Campsites",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_PROFILES) {
            AdminPlaceholder(
                title = "Manage Profiles",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_LOGS) {
            Column {
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("← Back")
                }

                AdminLogsScreen()
            }
        }
    }
}

/**
 * Temporary destination until the assigned admin
 * management screen is implemented.
 */
@Composable
private fun AdminPlaceholder(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("$title — coming soon")
        }
    }
}

