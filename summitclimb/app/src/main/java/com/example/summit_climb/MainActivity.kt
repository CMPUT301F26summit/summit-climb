package com.example.summit_climb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.example.summit_climb.service.LotteryDrawService


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        SampleData.uploadIfMissing(db)

        val auth = FirebaseAuth.getInstance()

        fun startScreen(currentUserId: String) {
            Log.d("Auth-login", "current user id: $currentUserId")
            val userRepository = UserRepository(db)
            userRepository.getUser(currentUserId) { }

            val campsiteRepository = CampsiteRepository()
            val searchController = CampsiteSearchController(campsiteRepository)

            val lotteryRepository = FirestoreLotteryRepository(db)
            val drawService = LotteryDrawService()
            val lotteryController = EntrantLotteryController(lotteryRepository, currentUserId)
            val ownerLotteryController = OwnerLotteryController(lotteryRepository)

            val notificationRepository = FirestoreNotificationRepository(db)
            val notificationController = NotificationController(notificationRepository) {
                FirebaseAuth.getInstance().currentUser?.uid
            }

            val ownerNotificationController = OwnerNotificationController(
                notificationRepository = notificationRepository
            )

            val reservationRepository = FirestoreReservationRepository(db)
            val reservationController = OwnerReservationController(
                repository = reservationRepository,
                notificationRepository = notificationRepository
            )

            val entrantReservationController = ReservationController(
                repository = reservationRepository,
                notificationRepository = notificationRepository
            )

            setContent {
                // runs any lottery draws that are due whenever the lottery list loads or changes (F3)
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
                    AppNavigation(
                        searchController = searchController,
                        lotteryController = lotteryController,
                        currentUserId = currentUserId,
                        ownerCampsiteController = OwnerCampsiteController(campsiteRepository),
                        notificationController = notificationController,
                        reservationController = reservationController,
                        ownerLotteryController = ownerLotteryController,
                        entrantReservationController = entrantReservationController,
                        ownerNotificationController = ownerNotificationController
                    )
                }
            }
        }

        window.statusBarColor = android.graphics.Color.rgb(225, 241, 250)

        val current = auth.currentUser

        if (current != null) {
            startScreen(current.uid)
        }
        else {
            auth.signInAnonymously()
                .addOnSuccessListener { startScreen(it.user!!.uid) }
                .addOnFailureListener { Log.e("Auth", "Anonymous sign-in failed", it) }
        }
    }
}
