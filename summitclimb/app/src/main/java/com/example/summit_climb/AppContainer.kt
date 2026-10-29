package com.example.summit_climb
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.data.FirestoreNotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.getValue

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currUserId: String?
        get() = auth.currentUser?.uid

    // Uncomment each line once the repo gets written

    // val userRepository: UserRepository = FirestoreUserRepository(firestore)
    // val campsiteRepository: CampsiteRepository = FirestoreCampsiteRepository(firestore)
    // val lotteryRepository: LotteryRepository = FirestoreLotteryRepository(firestore)
    // val reservationRepository: ReservationRepository = FirestoreReservationRepository(firestore)
    val notificationRepository: NotificationRepository by lazy {
        FirestoreNotificationRepository(firestore)
    }
}