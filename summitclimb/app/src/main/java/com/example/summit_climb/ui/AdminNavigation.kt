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
import androidx.compose.runtime.DisposableEffect
import com.example.summit_climb.controller.ConversationController
import com.example.summit_climb.ui.admin.AdminConversationDetailsScreen

private const val ADMIN_NOTIFICATION_DETAILS = "adminNotificationDetails"
private const val ADMIN_CONVERSATION_DETAILS = "adminConversationDetails"



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
    val conversationController = remember {ConversationController()}
    var messages by remember { mutableStateOf<List<Notification>>(emptyList())}
    var selectedConversation by remember {mutableStateOf<List<Notification>>(emptyList())}
    var messageError by remember {mutableStateOf("")}

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
            DisposableEffect(Unit) {
                val stopListening = conversationController.getAllMessages(
                    onMessages = {
                        messages = it
                        messageError = ""
                    },
                    onFailure = {messageError = it.message ?: "Unable to load messages"}
                )
                onDispose {stopListening()}
            }
            val conversations = conversationController.groupAdminConversations(messages)

            AdminLogsScreen(
                campsites = campsiteRepository.campsite,
                users = userRepository.users,
                notifications = notificationRepository.notifications,
                conversations = conversations,
                onConversationClick = { conversation ->
                    selectedConversation = conversation
                    navController.navigate(ADMIN_CONVERSATION_DETAILS)
                },
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
            if (messageError.isNotBlank()) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { messageError = "" },
                    title = {Text("Unable to load messages")},
                    text = {Text(messageError)},
                    confirmButton = {
                        TextButton(onClick = {messageError = "" }) {
                            Text("OK")
                        }
                    }
                )
            }
        }

        composable(ADMIN_CONVERSATION_DETAILS) {
            val conversation = selectedConversation
            val message = conversation.firstOrNull()

            if (message != null) {
                val campsite = campsiteRepository.campsite.find {
                    it.id == message.campsiteId
                }

                val ownerId = campsite?.ownerId ?: ""

                val camperId = if (message.senderId == ownerId) {
                    message.recipientId
                } else if (message.recipientId == ownerId) {
                    message.senderId
                } else {
                    ""
                }

                val owner = userRepository.users.find {
                    it.userId == ownerId
                }

                val camper = userRepository.users.find {
                    it.userId == camperId
                }

                AdminConversationDetailsScreen(
                    messages = conversation,
                    ownerName = owner?.name?.ifBlank { "Unknown owner" } ?: "Unknown owner",
                    camperName = camper?.name?.ifBlank { "Unknown camper" } ?: "Unknown camper",
                    campsiteName = campsite?.name ?: "Unknown campsite",
                    getUserName = { userId ->
                        val user = userRepository.users.find {
                            it.userId == userId
                        }
                        user?.name?.ifBlank { "Unknown user" } ?: "Unknown user"
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onViewOwner = {
                        // TODO: Connect admin owner profile
                    },
                    onViewCamper = {
                        // TODO: Connect admin camper profile
                    }
                )
            } else {
                AdminPlaceholder(
                    title = "Conversation unavailable",
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
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

