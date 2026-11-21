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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        askNotificationPermission()

        val db = FirebaseFirestore.getInstance()
        SampleData.uploadIfMissing(db)

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: "test_user"

        val campsiteRepository = CampsiteRepository()
        val searchController = CampsiteSearchController(campsiteRepository)

        val lotteryRepository = FirestoreLotteryRepository(db)
        val lotteryController = EntrantLotteryController(lotteryRepository, currentUserId)
        val ownerLotteryController = OwnerLotteryController(lotteryRepository)

        val notificationRepository = FirestoreNotificationRepository(db)
        val notificationController = NotificationController(notificationRepository) {
            FirebaseAuth.getInstance().currentUser?.uid
        }

        val reservationRepository = FirestoreReservationRepository(db)
        val reservationController = OwnerReservationController(
            repository = reservationRepository,
            notificationRepository = notificationRepository
        )

        window.statusBarColor = android.graphics.Color.rgb(225, 241, 250)

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCFE7F5))
            )
            SummitclimbTheme {
                AppNavigation(
                    searchController = searchController,
                    lotteryController = lotteryController,
                    currentUserId = currentUserId,
                    ownerCampsiteController = OwnerCampsiteController(campsiteRepository),
                    notificationController = notificationController,
                    reservationController = reservationController,
                    ownerLotteryController = ownerLotteryController
                )
            }
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

