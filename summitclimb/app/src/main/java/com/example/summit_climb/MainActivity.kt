package com.example.summit_climb

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.summit_climb.controller.CampsiteSearchController
import com.example.summit_climb.controller.EntrantLotteryController
import com.example.summit_climb.controller.NotificationController
import com.example.summit_climb.controller.OwnerCampsiteController
import com.example.summit_climb.controller.OwnerLotteryController
import com.example.summit_climb.controller.OwnerNotificationController
import com.example.summit_climb.data.CampsiteRepository
import com.example.summit_climb.data.FirestoreLotteryRepository
import com.example.summit_climb.data.FirestoreNotificationRepository
import com.example.summit_climb.data.SampleData
import com.example.summit_climb.ui.AppNavigation
import com.example.summit_climb.ui.theme.SummitclimbTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.controller.OwnerReservationController
import com.example.summit_climb.data.FirestoreReservationRepository
import com.example.summit_climb.controller.ReservationController
import com.example.summit_climb.data.UserRepository
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.example.summit_climb.model.User
import com.example.summit_climb.model.UserRole
import com.example.summit_climb.model.AccountStatus
import com.example.summit_climb.ui.AdminNavigation
import com.example.summit_climb.service.LotteryDrawService
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        createNotificationChannel()
        askNotificationPermission()

        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("MainActivity", "FirebaseFirestore not initialized", e)
            null
        }
        if (db != null) {
            runCatching { SampleData.uploadIfMissing(db) }
        }

        val auth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("MainActivity", "FirebaseAuth not initialized", e)
            null
        }

        fun startScreen(currentUserId: String) {
            Log.d("Auth-login", "current user id: $currentUserId")

            // Retrieve and save the current FCM token after authentication
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    FcmTokenManager().saveToken(currentUserId, token)
                }
                .addOnFailureListener { exception ->
                    Log.e("FCM", "Failed to retrieve FCM token", exception)
                }
            val userRepository = UserRepository(db ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull() ?: return)

            val campsiteRepository = CampsiteRepository()
            val searchController = CampsiteSearchController(campsiteRepository)

            val lotteryRepository = FirestoreLotteryRepository(db ?: FirebaseFirestore.getInstance())
            val drawService = LotteryDrawService()
            val lotteryController = EntrantLotteryController(lotteryRepository, currentUserId)
            val ownerLotteryController = OwnerLotteryController(lotteryRepository)

            val notificationRepository = FirestoreNotificationRepository(db ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull() ?: return)
            val notificationController = NotificationController(notificationRepository) {
                FirebaseAuth.getInstance().currentUser?.uid
            }

            val ownerNotificationController = OwnerNotificationController(
                notificationRepository = notificationRepository
            )

            val reservationRepository = FirestoreReservationRepository(db ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull() ?: return)
            val reservationController = OwnerReservationController(
                repository = reservationRepository,
                notificationRepository = notificationRepository
            )

            val entrantReservationController = ReservationController(
                repository = reservationRepository,
                notificationRepository = notificationRepository
            )

            setContent {
                var currentUser by remember(currentUserId) {
                    mutableStateOf<User?>(null)
                }
                var adminMode by remember { mutableStateOf(false) }

                LaunchedEffect(currentUserId) {
                    userRepository.getUser(currentUserId) { user ->
                        currentUser = user
                    }
                }

                // runs any lottery draws that are due whenever the lottery list loads or changes
                LaunchedEffect(Unit) {
                    snapshotFlow { lotteryRepository.lotteries.toList() }
                        .collect { lotteries ->
                            drawService.runDueDraws(lotteries, lotteryRepository, notificationRepository)
                        }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFCFE7F5))
                )
                SummitclimbTheme {
                    val user = currentUser

                    when {
                        user == null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFCFE7F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        user.userId != currentUserId ||
                                user.accountStatus != AccountStatus.ACTIVE -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Account unavailable")
                            }
                        }

                        adminMode -> {
                            AdminNavigation(
                                onExitAdmin = { adminMode = false },
                                campsiteRepository = campsiteRepository,
                                userRepository = userRepository,
                                notificationRepository = notificationRepository
                            )
                        }

                        else -> {
                            AppNavigation(
                                searchController = searchController,
                                lotteryController = lotteryController,
                                currentUserId = currentUserId,
                                ownerCampsiteController =
                                    OwnerCampsiteController(campsiteRepository),
                                notificationController = notificationController,
                                reservationController = reservationController,
                                ownerLotteryController = ownerLotteryController,
                                entrantReservationController =
                                    entrantReservationController,
                                ownerNotificationController =
                                    ownerNotificationController,
                                onOpenAdmin = { adminMode = true }
                            )
                        }
                    }
                }
            }
        }

        window.statusBarColor = android.graphics.Color.rgb(225, 241, 250)

        val current = auth?.currentUser

        if (current != null) {
            startScreen(current.uid)
        }
        else if (auth != null) {
            auth.signInAnonymously()
                .addOnSuccessListener { startScreen(it.user!!.uid) }
                .addOnFailureListener { Log.e("Auth", "Anonymous sign-in failed", it) }
        } else {
            startScreen("test-user")
        }
    }

    /**
     * Creates the notification channel
     *
     * Android 8.0+ require notifications to belong to a notification
     * channel. The channel groups notifications related to lotteries,
     * reservations, and messages.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "summit_climb_notifications",
                "Summit Climb Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about lotteries, reservations, and messages"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Handles the result of the Android notification permission request.
     *
     * The system reports whether the user granted or denied permission
     * to display notifications.
     */
    private val requestNotificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // notification permission granted
            } else {
                // notification permission denied
            }
        }

    /**
     * Request permission to display notifications when necessary.
     *
     * Android 13+ require runtime permission before an application
     * can display notifications.
     *
     * If permission has already been granted, no additional
     * request is made. Older Android versions do not require this.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // permission already granted
                }

                else -> {
                    requestNotificationPermission.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }
}

