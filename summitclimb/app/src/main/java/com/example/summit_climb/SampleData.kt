package com.example.summit_climb.data

import android.util.Log
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.ListingStatus
import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

object SampleData {

    private const val TAG = "SampleData"
    private const val CAMPSITES = "campsites"
    private const val LOTTERIES = "lotteries"
    private const val SAMPLE_OWNER_ID = "sample-owner"
    private const val ONE_DAY = 24L * 60 * 60 * 1000

    fun uploadIfMissing(db: FirebaseFirestore) {
        val now = System.currentTimeMillis()

        for (campsite in sampleCampsites()) {
            val document = db.collection(CAMPSITES).document(campsite.id)
            uploadDocumentIfMissing(document, campsite)
        }

        for (lottery in sampleLotteries(now)) {
            val document = db.collection(LOTTERIES).document(lottery.id)
            uploadDocumentIfMissing(document, lottery)
        }
    }


    private fun uploadDocumentIfMissing(document: DocumentReference, data: Any) {
        document.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    return@addOnSuccessListener
                }
                document.set(data)
                    .addOnSuccessListener { Log.d(TAG, "Uploaded ${document.path}") }
                    .addOnFailureListener { error -> Log.e(TAG, "Couldn't upload ${document.path}", error) }
            }
            .addOnFailureListener { error -> Log.e(TAG, "Couldn't check ${document.path}", error) }
    }


    private fun dateFromToday(daysFromToday: Long): String {
        return LocalDate.now().plusDays(daysFromToday).toString()
    }


    private fun nextDays(days: Long): List<String> {
        val dates = mutableListOf<String>()
        for (day in 1..days) {
            dates.add(dateFromToday(day))
        }
        return dates
    }

    private fun sampleCampsites(): List<Campsite> {
        val cedarValley = Campsite(
            id = "sample-cedar-valley",
            name = "Cedar Valley",
            location = "Banff, AB",
            latitude = 51.1784,
            longitude = -115.5708,
            maxOccupancy = 6,
            price = 60.0,
            description = "A quiet, tree-lined site ten minutes from Banff townsite, with views of " +
                    "Cascade Mountain and easy access to the Bow River trails. Great base for rock climbing.",
            amenities = listOf("Parking", "Washrooms", "Fire", "Water Tap"),
            tags = listOf("climbing", "river", "mountains"),
            rules = "Quiet hours 10 PM – 7 AM. Fires only in the provided fire pit. " +
                    "Store all food in the bear locker. Maximum 2 vehicles.",
            cancellationPolicy = "Full refund up to 7 days before check-in. 50% refund up to 48 hours before. " +
                    "No refund after that.",
            promoImageUrl = "https://picsum.photos/seed/cedar-valley/800/500",
            ownerId = SAMPLE_OWNER_ID,
            availableDates = nextDays(60),
            bookedDates = listOf(dateFromToday(20), dateFromToday(21), dateFromToday(27)),
            activeLotteryId = "sample-lottery-cedar-valley",
            listingStatus = ListingStatus.APPROVED
        )

        val lakesideCamp = Campsite(
            id = "sample-lakeside-camp",
            name = "Lakeside Camp",
            location = "Jasper, AB",
            latitude = 52.8737,
            longitude = -118.0814,
            maxOccupancy = 8,
            price = 80.0,
            description = "Lakefront site on the shore of Lac Beauvert with a private canoe launch. " +
                    "Wildlife is common at dawn; elk often graze nearby.",
            amenities = listOf("Parking", "Washrooms", "Trails", "Picnic tables"),
            tags = listOf("lake", "canoe", "family"),
            rules = "Keep 30 m away from wildlife. No glass bottles on the beach. Pets must be leashed.",
            cancellationPolicy = "Full refund up to 14 days before check-in. No refund after that.",
            promoImageUrl = "https://picsum.photos/seed/lakeside-camp/800/500",
            ownerId = SAMPLE_OWNER_ID,
            availableDates = nextDays(60),
            bookedDates = listOf(dateFromToday(24), dateFromToday(25)),
            activeLotteryId = "sample-lottery-lakeside-camp",
            listingStatus = ListingStatus.APPROVED
        )

        val summitCamp = Campsite(
            id = "sample-summit-camp",
            name = "Summit Camp",
            location = "Hinton, AB",
            latitude = 53.4065,
            longitude = -117.5839,
            maxOccupancy = 8,
            price = 65.0,
            description = "Large group site in the Hinton–Grande Cache area with an outdoor cookhouse " +
                    "and an on-site tennis court. Fits up to 8 people.",
            amenities = listOf("Fire", "Trails", "Picnic tables", "Washrooms"),
            tags = listOf("group", "tennis", "cookhouse"),
            rules = "Check-in after 2 PM, check-out by 11 AM. Tennis court closes at dusk. " +
                    "Clean the cookhouse after use.",
            cancellationPolicy = "Full refund up to 3 days before check-in. No refund after that.",
            promoImageUrl = "https://picsum.photos/seed/summit-camp/800/500",
            ownerId = SAMPLE_OWNER_ID,
            availableDates = nextDays(60),
            bookedDates = listOf(dateFromToday(3), dateFromToday(4), dateFromToday(10)),
            activeLotteryId = "sample-lottery-summit-camp",
            listingStatus = ListingStatus.APPROVED
        )

        return listOf(cedarValley, lakesideCamp, summitCamp)
    }


    private fun sampleLotteries(now: Long): List<Lottery> {
        val cedarValleyLottery = Lottery(
            id = "sample-lottery-cedar-valley",
            ownerId = SAMPLE_OWNER_ID,
            campsiteIds = listOf("sample-cedar-valley"),
            registrationStart = now - 2 * ONE_DAY,
            registrationEnd = now + 14 * ONE_DAY,
            bookingStart = now + 15 * ONE_DAY,
            bookingEnd = now + 45 * ONE_DAY,
            winnerCount = 5,
            waitlistCap = null,
            entrantCount = 0,
            status = LotteryStatus.OPEN
        )

        val lakesideLottery = Lottery(
            id = "sample-lottery-lakeside-camp",
            ownerId = SAMPLE_OWNER_ID,
            campsiteIds = listOf("sample-lakeside-camp"),
            registrationStart = now - 1 * ONE_DAY,
            registrationEnd = now + 10 * ONE_DAY,
            bookingStart = now + 12 * ONE_DAY,
            bookingEnd = now + 40 * ONE_DAY,
            winnerCount = 1,
            waitlistCap = 2,
            entrantCount = 0,
            status = LotteryStatus.OPEN
        )

        val summitLottery = Lottery(
            id = "sample-lottery-summit-camp",
            ownerId = SAMPLE_OWNER_ID,
            campsiteIds = listOf("sample-summit-camp"),
            registrationStart = now - 20 * ONE_DAY,
            registrationEnd = now - 6 * ONE_DAY,
            bookingStart = now - 5 * ONE_DAY,
            bookingEnd = now + 20 * ONE_DAY,
            winnerCount = 5,
            waitlistCap = null,
            entrantCount = 0,
            status = LotteryStatus.DRAWN
        )

        return listOf(cedarValleyLottery, lakesideLottery, summitLottery)
    }
}