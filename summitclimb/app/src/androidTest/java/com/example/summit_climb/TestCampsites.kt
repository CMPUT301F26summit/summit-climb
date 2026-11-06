package com.example.summit_climb

import com.example.summit_climb.model.Lottery
import com.example.summit_climb.model.LotteryStatus

/**
 * fake campsites and lotteries shared by the UI tests, so tests don't need Firestore.
 * The data is realistic so every test run sees exactly the same values.
 */
object TestCampsites {
    private const val ONE_DAY = 24L * 60 * 60 * 1000

    /** Banff, up to 6 guests, free Dec 1–5 2026, rated 4.5. */
    val cedarValley = Campsite(
        id = "test-cedar-valley",
        name = "Cedar Valley",
        location = "Banff, AB",
        maxOccupancy = 6,
        avgRating = 4.5,
        ratings = mapOf("device-1" to 4, "device-2" to 5),
        description = "A quiet, tree-lined site ten minutes from Banff townsite.",
        amenities = listOf("Parking", "Washrooms", "Fire", "Water Tap"),
        tags = listOf("climbing", "river"),
        price = 60.0,
        rules = "Quiet hours 10 PM – 7 AM.",
        cancellationPolicy = "Full refund up to 7 days before check-in.",
        availableDates = listOf("2026-12-01", "2026-12-02", "2026-12-03", "2026-12-04", "2026-12-05"),
        listingStatus = ListingStatus.APPROVED
    )

    /** Jasper, up to 8 guests, Dec 2 already booked, no ratings. */
    val lakesideCamp = Campsite(
        id = "test-lakeside-camp",
        name = "Lakeside Camp",
        location = "Jasper, AB",
        maxOccupancy = 8,
        description = "Lakefront site with a private canoe launch.",
        amenities = listOf("Parking", "Trails", "Picnic tables"),
        tags = listOf("lake", "canoe", "family"),
        price = 80.0,
        rules = "Keep 30 m away from wildlife.",
        cancellationPolicy = "Full refund up to 14 days before check-in.",
        availableDates = listOf("2026-12-01", "2026-12-02", "2026-12-03"),
        bookedDates = listOf("2026-12-02"),
        listingStatus = ListingStatus.APPROVED
    )

    /** Hinton, up to 8 guests, no amenities listed. */
    val summitCamp = Campsite(
        id = "test-summit-camp",
        name = "Summit Camp",
        location = "Hinton, AB",
        maxOccupancy = 8,
        description = "Group site with an outdoor cookhouse and a tennis court.",
        tags = listOf("group", "tennis"),
        price = 65.0,
        rules = "Clean the cookhouse after use.",
        cancellationPolicy = "Full refund up to 3 days before check-in.",
        availableDates = listOf("2026-12-01", "2026-12-02"),
        listingStatus = ListingStatus.APPROVED
    )

    /** All three, in the order the list shows them. */
    val all = listOf(cedarValley, lakesideCamp, summitCamp)

    /** Registration open now, no waitlist cap: joining works. */
    fun openLottery(): Lottery {
        val now = System.currentTimeMillis()
        return Lottery(
            id = "test-lottery-open",
            campsiteIds = listOf(cedarValley.id),
            registrationStart = now - 2 * ONE_DAY,
            registrationEnd = now + 14 * ONE_DAY,
            bookingStart = now + 15 * ONE_DAY,
            bookingEnd = now + 45 * ONE_DAY,
            winnerCount = 5,
            entrantCount = 12,
            status = LotteryStatus.OPEN
        )
    }

    /** Registration open, but cap of 2 is already reached: joining is rejected as full. */
    fun fullLottery(): Lottery {
        return openLottery().copy(id = "test-lottery-full", waitlistCap = 2, entrantCount = 2)
    }

    /** Winners already drawn and registration over: joining is rejected as closed. */
    fun drawnLottery(): Lottery {
        val now = System.currentTimeMillis()
        return openLottery().copy(
            id = "test-lottery-drawn",
            registrationStart = now - 20 * ONE_DAY,
            registrationEnd = now - 6 * ONE_DAY,
            status = LotteryStatus.DRAWN
        )
    }
}