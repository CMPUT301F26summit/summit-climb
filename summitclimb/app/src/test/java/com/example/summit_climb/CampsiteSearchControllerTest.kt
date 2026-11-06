package com.example.summit_climb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

/**
 * Purpose: unit tests for [CampsiteSearchController]
 * [CampsiteRepository] connects to Firestore when it is created, so these tests use a
 * Mockito mock of it instead. The mock is never constructed for real, so no Firebase code runs.
 */
class CampsiteSearchControllerTest {

    private lateinit var repository: CampsiteRepository
    private lateinit var controller: CampsiteSearchController

    /** Banff, up to 6 guests, free Dec 1–5 2026. */
    private val cedarValley = Campsite(
        id = "cedar-valley",
        name = "Cedar Valley",
        location = "Banff, AB",
        maxOccupancy = 6,
        description = "A quiet, tree-lined site near Banff townsite.",
        tags = listOf("climbing", "river"),
        availableDates = listOf("2026-12-01", "2026-12-02", "2026-12-03", "2026-12-04", "2026-12-05"),
        listingStatus = ListingStatus.APPROVED
    )

    /** Jasper, up to 8 guests, open Dec 1–3 2026 but Dec 2 is booked. */
    private val lakesideCamp = Campsite(
        id = "lakeside-camp",
        name = "Lakeside Camp",
        location = "Jasper, AB",
        maxOccupancy = 8,
        description = "Lakefront site with a private canoe launch.",
        tags = listOf("canoe", "family"),
        availableDates = listOf("2026-12-01", "2026-12-02", "2026-12-03"),
        bookedDates = listOf("2026-12-02"),
        listingStatus = ListingStatus.APPROVED
    )

    /** Hinton, up to 8 guests, open Dec 1–2 2026. */
    private val summitCamp = Campsite(
        id = "summit-camp",
        name = "Summit Camp",
        location = "Hinton, AB",
        maxOccupancy = 8,
        description = "Group site with an outdoor cookhouse and a tennis court.",
        tags = listOf("group", "tennis"),
        availableDates = listOf("2026-12-01", "2026-12-02"),
        listingStatus = ListingStatus.APPROVED
    )

    private val approvedCampsites = listOf(cedarValley, lakesideCamp, summitCamp)

    @Before
    fun setUp() {
        repository = mock(CampsiteRepository::class.java)
        `when`(repository.approvedCampsites()).thenReturn(approvedCampsites)
        controller = CampsiteSearchController(repository)
    }

    // search

    @Test
    fun search_withEmptyKeywordAndFilter_returnsEveryApprovedCampsite() {
        val results = controller.search("", CampsiteFilter())

        assertEquals(approvedCampsites, results)
    }

    @Test
    fun search_onlyLooksAtApprovedCampsites() {
        controller.search("", CampsiteFilter())

        verify(repository).approvedCampsites()
    }

    @Test
    fun search_byKeyword_matchesName() {
        val results = controller.search("lakeside", CampsiteFilter())

        assertEquals(listOf(lakesideCamp), results)
    }

    @Test
    fun search_byKeyword_ignoresCase() {
        val results = controller.search("CEDAR", CampsiteFilter())

        assertEquals(listOf(cedarValley), results)
    }

    @Test
    fun search_byKeyword_matchesTag() {
        val results = controller.search("tennis", CampsiteFilter())

        assertEquals(listOf(summitCamp), results)
    }

    @Test
    fun search_byKeyword_matchesDescription() {
        val results = controller.search("canoe launch", CampsiteFilter())

        assertEquals(listOf(lakesideCamp), results)
    }

    @Test
    fun search_byKeyword_ignoresSurroundingSpaces() {
        val results = controller.search("  river  ", CampsiteFilter())

        assertEquals(listOf(cedarValley), results)
    }

    @Test
    fun search_withNoMatch_returnsEmptyList() {
        val results = controller.search("desert", CampsiteFilter())

        assertTrue(results.isEmpty())
    }

    // filter

    @Test
    fun search_filterByLocation_ignoresCase() {
        val results = controller.search("", CampsiteFilter(location = "jasper"))

        assertEquals(listOf(lakesideCamp), results)
    }

    @Test
    fun search_filterByGuests_removesCampsitesThatAreTooSmall() {
        val results = controller.search("", CampsiteFilter(guests = 7))

        assertEquals(listOf(lakesideCamp, summitCamp), results)
    }

    @Test
    fun search_filterByGuests_exactlyMaxOccupancyFits() {
        val results = controller.search("", CampsiteFilter(location = "Banff", guests = 6))

        assertEquals(listOf(cedarValley), results)
    }

    @Test
    fun search_filterByDates_removesBookedAndUnopenedDates() {
        // Lakeside is booked on Dec 2; Summit isn't open on Dec 3
        val filter = CampsiteFilter(startDate = "2026-12-02", endDate = "2026-12-03")

        val results = controller.search("", filter)

        assertEquals(listOf(cedarValley), results)
    }

    @Test
    fun search_filterByStartDateOnly_checksThatOneDate() {
        val results = controller.search("", CampsiteFilter(startDate = "2026-12-01"))

        assertEquals(approvedCampsites, results)
    }

    @Test
    fun search_filterWithEndBeforeStart_returnsEmptyList() {
        val filter = CampsiteFilter(startDate = "2026-12-03", endDate = "2026-12-01")

        val results = controller.search("", filter)

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_filterWithBadDateFormat_returnsEmptyList() {
        val results = controller.search("", CampsiteFilter(startDate = "Dec 1 2026"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_keywordAndFilterTogether_mustBothMatch() {
        // "site" is in every description, but only Summit Camp is in Hinton
        val results = controller.search("site", CampsiteFilter(location = "Hinton"))

        assertEquals(listOf(summitCamp), results)
    }

    //  filterCamps 

    @Test
    fun filterCamps_keepsOriginalOrder() {
        val reversed = listOf(summitCamp, lakesideCamp, cedarValley)

        val results = controller.filterCamps(reversed, "", CampsiteFilter(guests = 7))

        assertEquals(listOf(summitCamp, lakesideCamp), results)
    }

    //  campsiteDetails 

    @Test
    fun campsiteDetails_returnsCampsiteWithThatId() {
        `when`(repository.getCampsite("lakeside-camp")).thenReturn(lakesideCamp)

        val campsite = controller.campsiteDetails("lakeside-camp")

        assertEquals(lakesideCamp, campsite)
    }

    @Test
    fun campsiteDetails_unknownId_returnsNull() {
        val campsite = controller.campsiteDetails("no-such-campsite")

        assertNull(campsite)
    }

    // rateCampsite

    @Test
    fun rateCampsite_validStars_savesRatingAndReturnsTrue() {
        val saved = controller.rateCampsite("cedar-valley", "device-123", 4)

        assertTrue(saved)
        verify(repository).saveRating(Rating(entrantId = "device-123", campsiteId = "cedar-valley", stars = 4))
    }

    @Test
    fun rateCampsite_oneAndFiveStars_areAllowed() {
        assertTrue(controller.rateCampsite("cedar-valley", "device-123", 1))
        assertTrue(controller.rateCampsite("cedar-valley", "device-123", 5))
    }

    @Test
    fun rateCampsite_zeroStars_isRejectedAndNotSaved() {
        val saved = controller.rateCampsite("cedar-valley", "device-123", 0)

        assertFalse(saved)
        verifyNoInteractions(repository)
    }

    @Test
    fun rateCampsite_sixStars_isRejectedAndNotSaved() {
        val saved = controller.rateCampsite("cedar-valley", "device-123", 6)

        assertFalse(saved)
        verifyNoInteractions(repository)
    }
}
