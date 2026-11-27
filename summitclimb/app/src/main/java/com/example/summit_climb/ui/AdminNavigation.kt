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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.summit_climb.data.CampsiteRepository
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.data.UserRepository
import com.example.summit_climb.model.Notification
import com.example.summit_climb.service.ImageService
import com.example.summit_climb.ui.admin.AdminImageType
import com.example.summit_climb.ui.admin.AdminNotificationDetailsScreen

private const val ADMIN_NOTIFICATION_DETAILS = "adminNotificationDetails"


/**
 * Main navigation for the admin section, starting at the admin dashboard.
 * An admin can open the campsite management, profile management
 * and logs screens.
 *
 * @param onExitAdmin Called when the admin leaves the dashboard.
 * @param campsiteRepository Repository used to access campsite data.
 * @param userRepository Repository used to access user data.
 * @param notificationRepository Repository used to access notifications.
 */
@Composable
fun AdminNavigation(
    onExitAdmin: () -> Unit = {},
    campsiteRepository: CampsiteRepository,
    userRepository: UserRepository,
    notificationRepository: NotificationRepository
) {
    val navController = rememberNavController()
    val imageService = remember { ImageService() }

    // Holds the notification selected from Admin Logs.
    var selectedNotification by remember {
        mutableStateOf<Notification?>(null)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.ADMIN_DASHBOARD,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
            .systemBarsPadding()
    ) {
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(navController, onExit = onExitAdmin)
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
            AdminLogsScreen(
                campsites = campsiteRepository.campsite,
                users = userRepository.users,
                notifications = notificationRepository.notifications,
                onRemoveImage = { item, onDone ->
                    when (item.type) {
                        AdminImageType.CAMPSITE -> {
                            campsiteRepository.removeCampsiteImage(item.id) { firestoreSuccess ->
                                if (!firestoreSuccess) {
                                    onDone(false)
                                } else {
                                    imageService.delete(item.imageUrl) { storageSuccess ->
                                        onDone(storageSuccess)
                                    }
                                }
                            }
                        }

                        AdminImageType.PROFILE -> {
                            userRepository.removeProfileImage(item.id) { firestoreSuccess ->
                                if (!firestoreSuccess) {
                                    onDone(false)
                                } else {
                                    imageService.delete(item.imageUrl) { storageSuccess ->
                                        onDone(storageSuccess)
                                    }
                                }
                            }
                        }
                    }
                },
                onNotificationClick = { notification ->
                    selectedNotification = notification
                    navController.navigate(ADMIN_NOTIFICATION_DETAILS)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ADMIN_NOTIFICATION_DETAILS) {
            val notification = selectedNotification

            if (notification != null) {
                val recipient = userRepository.users.find {
                    it.userId == notification.recipientId
                }

                val campsite = campsiteRepository.campsite.find {
                    it.id == notification.campsiteId
                }

                AdminNotificationDetailsScreen(
                    notification = notification,
                    recipientName = recipient?.name ?: "Unknown",
                    campsiteName = campsite?.name ?: "Unknown",
                    onBack = {
                        navController.popBackStack()
                    },
                    onViewUser = { userId ->
                    }, // TO BE DONE: add route once screen is built

                    onViewCampsite = { campsiteId ->
                    } // TO BE DONE: add route once screen is built
                )
            } else {
                AdminPlaceholder(
                    title = "Notification unavailable",
                    onBack = { navController.popBackStack() }
                )
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

