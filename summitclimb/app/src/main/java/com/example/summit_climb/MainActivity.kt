package com.example.summit_climb

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.summit_climb.data.FirestoreLotteryRepository
import com.example.summit_climb.data.SampleData
import com.example.summit_climb.ui.theme.SummitclimbTheme
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Identifies the user by their device,
 * loads or creates their profile in Firestore
 * Pending: deactivated users are not blocked yet, and the loaded profile is only
 * logged (not yet used to show the My Campsites tab for owners).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = FirebaseFirestore.getInstance()

        // identify user by their device
        val currentUserId = readUserDeviceId()

        // oad their profile or create it on first launch
        val userRepository = UserRepository()
        userRepository.getUser(currentUserId) { user ->
            Log.d(TAG, "Profile ready for ${user.userId}")
        }

        // make sure sample campsites and lotteries exist
        SampleData.uploadIfMissing(db)

        // create each repository once
        val campsiteRepository = CampsiteRepository()
        val lotteryRepository = FirestoreLotteryRepository(db)

        // create controllers which screens use
        val searchController = CampsiteSearchController(campsiteRepository)
        val lotteryController = EntrantLotteryController(lotteryRepository, currentUserId)

        setContent {
            SummitclimbTheme {
                AppNavigation(
                    searchController = searchController,
                    lotteryController = lotteryController,
                    currentUserId = currentUserId
                )
            }
        }
    }

    /**
     * Gets an ID that is unique to this device and this app, used as the user's ID.
     * @return the device ID, like `"9774d56d682e549c"`.
     */
    @SuppressLint("HardwareIds")
    private fun readUserDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}