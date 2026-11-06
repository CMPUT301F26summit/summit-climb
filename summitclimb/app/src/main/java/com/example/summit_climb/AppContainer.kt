package com.example.summit_climb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.summit_climb.data.FirestoreLotteryRepository
import com.example.summit_climb.data.LotteryRepository
import com.example.summit_climb.data.NotificationRepository
import com.example.summit_climb.data.FirestoreReservationRepository
import com.example.summit_climb.data.FirestoreNotificationRepository

class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currUserId: String?
        get() = auth.currentUser?.uid

    // Uncomment each line once the repo gets written

    //val userRepository: UserRepository = FirestoreUserRepository(firestore)
    //val campsiteRepository: CampsiteRepository = FirestoreCampsiteRepository(firestore)
    //val lotteryRepository: LotteryRepository = FirestoreLotteryRepository(firestore)
    //val reservationRepository: ReservationRepository = FirestoreReservationRepository(firestore)
    //val notificationRepository: NotificationRepository = FirestoreNotificationRepository(firestore)

}