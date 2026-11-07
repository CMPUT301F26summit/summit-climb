package com.example.summit_climb.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Where a [Campsite] listing is in the admin review process.
 * Entrants only ever see [APPROVED] campsites.
 */
enum class ListingStatus{
    PENDING, APPROVED, HIDDEN, REJECTED
}

/**
 * The filter an entrant sets on the campsite list screen.
 * Leave a field empty (or [guests] at 0) to not filter by it.
 * Dates use the format `yyyy-MM-dd`, for example `2026-11-03`.
 * Example: campsites in Jasper for 4 people from Nov 3 to Nov 5, 2026:
 * val filter = CampsiteFilter(
 *     location = "Jasper",
 *     startDate = "2026-11-03",
 *     endDate = "2026-11-05",
 *     guests = 4
 * )
 *
 * @property location: text the campsite's location must contain.
 * @property startDate: first date of the stay, as `yyyy-MM-dd`.
 * @property endDate: last date of the stay, as `yyyy-MM-dd`. If empty, only [startDate] is checked.
 * @property guests: number of people staying. 0 means any number.
 * @constructor Creates a filter. With no arguments, creates a filter that matches every campsite.
 */
data class CampsiteFilter(
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val guests: Int = 0
)

/**
 * A campsite that entrants can find, view and join lotteries for.
 * Campsites are stored in the Firestore `campsites` collection.
 * The document ID is not stored inside the document
 * [com.example.summit_climb.data.CampsiteRepository] will copy it into [id] after loading.
 * Every property has a default value
 * Dates in [availableDates] and [bookedDates] use the format `yyyy-MM-dd`,
 *
 * @property id: Firestore document ID.
 * @property name: campsite's name.
 * @property location: campsite's location as text, for example `Jasper, AB`. Part of the summary info.
 * @property maxOccupancy: most people who can stay at once. Part of the summary info.
 * @property avgRating: average of all star ratings, from 0.0 (no ratings) to 5.0. Part of the summary info.
 * @property ratings: each entrant's star rating, keyed by their [User.userId], for example
 *   `{"device123": 4, "device456": 5}`. One rating per entrant.
 * @property description: a longer description of the campsite.
 * @property amenities: the amenities offered.
 * @property tags: short labels used in keyword search, like `["lake", "family"]`.
 * @property price: price of the campsite.
 * @property promoImageUrl: URL of the promotional image.
 * @property rules: campsite rules the entrant must follow.
 * @property cancellationPolicy: campsite's cancellation policy.
 * @property ownerId: [User.userId] of the campsite owner.
 * @property latitude: latitude of the campsite's map location.
 * @property longitude: longitude of the campsite's map location.
 * @property availableDates: dates the owner has opened for booking.
 * @property bookedDates: dates that already have a reservation.
 * @property activeLotteryId: ID of the campsite's active [Lottery] or `null` if there is none.
 * @property listingStatus: where the listing is in admin review.
 * @property adminNotes: notes an admin left about the listing like why it was rejected.
 * @constructor Creates a campsite. With no arguments, creates an empty pending campsite.
 */
data class Campsite(
    var id:String = "",
    var name: String = "",
    var location: String = "",
    var maxOccupancy: Int = 0,
    var avgRating: Double = 0.0,
    var ratings: Map<String, Int> = emptyMap(),
    var description: String = "",
    var amenities: List<String> = emptyList(),
    var tags: List<String> = emptyList(),
    var price: Double = 0.0,
    var promoImageUrl: String = "",
    var rules: String = "",
    var cancellationPolicy: String = "",
    var ownerId: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var availableDates: List<String> = emptyList(),
    var bookedDates: List<String> = emptyList(),
    var activeLotteryId: String? = null,
    var listingStatus: ListingStatus = ListingStatus.PENDING,
    var adminNotes: String = ""
) {
    /**
     * Checks whether the campsite's location contains the given text.
     * Not case sensitive, so `"jasper"` matches `"Jasper, AB"`.
     *
     * @param searchLocation:  location text to look for. Empty matches every campsite.
     * @return `true` if [location] contains [searchLocation] or [searchLocation] is empty.
     */
    fun locationMatch(searchLocation: String): Boolean{
        val trimSearch = searchLocation.trim()
        if (trimSearch.isEmpty()){
            return true
        }
        return location.contains(trimSearch, ignoreCase = true)
    }

    /**
     * Checks whether this campsite matches a filter of location, date range and number of guests.
     * Every part of the filter must match. Empty parts of the filter always match.
     * Example:
     * val campsite = Campsite(location = "Jasper, AB", maxOccupancy = 6,
     *     availableDates = listOf("2026-11-03", "2026-11-04"))
     * campsite.matchesFilter(CampsiteFilter(location = "jasper", guests = 4))  // true
     * campsite.matchesFilter(CampsiteFilter(guests = 8))                       // false, too many guests
     *
     * @param filter: location, dates and number of guests to check.
     * @return `true` if the location, dates and guests all match.
     */
    fun filterMatch(filter: CampsiteFilter): Boolean{
        val locationMatched = locationMatch(filter.location)
        val available = availableBetween(filter.startDate, filter.endDate)
        val guestsFit = fitsGuest(filter.guests)
        return locationMatched && available && guestsFit
    }

    /**
     * Checks whether every date from [startDate] to [endDate] is free.
     * Both dates are included. A date is free when it is in [availableDates]
     * and not in [bookedDates].
     * Example: with `startDate = "2026-11-03"` and `endDate = "2026-11-05"`,
     * the dates Nov 3, 4 and 5 must all be free.
     *
     * @param startDate: first date, as `yyyy-MM-dd`. Empty matches every campsite.
     * @param endDate: last date, as `yyyy-MM-dd`. Empty means only [startDate] is checked.
     * @return `true` if every date in the range is free, or [startDate] is empty.
     * `false` if a date is not in `yyyy-MM-dd` format or [endDate] is before [startDate].
     */
    fun availableBetween( startDate: String, endDate: String): Boolean{
        if (startDate.isBlank()){
            return true
        }
        val deadline = if (endDate.isBlank()) startDate else endDate
        val datesBetween = listDatesBetween(startDate, deadline)
        if (datesBetween.isEmpty()){
            return false
        }
        for (date in datesBetween){
            if (!isDateFree(date)){
                return false
            }
        }
        return true
    }

    /**
     * Checks whether one date is open for booking and not already booked.
     * @param date: date to check, as `yyyy-MM-dd`.
     * @return `true` if [date] is in [availableDates] and not in [bookedDates].
     */
    fun isDateFree(date: String): Boolean{
        val isAvailable = availableDates.contains(date)
        val isBooked = bookedDates.contains(date)
        return isAvailable && !isBooked
    }

    /**
     * Checks whether the campsite can hold the given number of guests.
     * @param guests: number of people staying. 0 or less matches every campsite.
     * @return `true` if [guests] is at most [maxOccupancy], or [guests] is 0 or less.
     */
    fun fitsGuest(guests:Int): Boolean{
        if (guests <= 0){
            return true
        }
        return guests <= maxOccupancy
    }

    /**
     * Checks whether the campsite's name, description or tags contain a search keyword.
     * Not case sensitive, so `"LAKE"` matches the tag `"lake"`.
     *
     * @param keyword: word to search for. Empty matches every campsite.
     * @return `true` if [name], [description] or one of the [tags] contains [keyword],
     * or [keyword] is empty.
     */
    fun keywordMatch(keyword: String): Boolean{
        val trimKeyword = keyword.trim()
        if (trimKeyword.isEmpty()){
            return true
        }
        val matchesName = name.contains(trimKeyword, ignoreCase = true)
        val matchesDescription = description.contains(trimKeyword, ignoreCase = true)
        val matchesTag = tags.any{tag -> tag.contains(trimKeyword, ignoreCase = true)}
        return matchesName || matchesDescription || matchesTag
    }

    /**
     * Saves an entrant's rating and recalculates [avgRating].
     * Each entrant has one rating, so second rating from the same entrant replaces the first.
     *
     * @param entrantId: [User.userId] of the entrant rating the campsite.
     * @param stars: rating, from 1 to 5.
     */
    fun setRating(entrantId: String, stars: Int){
        val updatedRatings = ratings.toMutableMap()
        updatedRatings[entrantId] = stars
        ratings = updatedRatings
        avgRating =
            if (ratings.isEmpty())
                0.0
            else
                ratings.values.average()
    }
}

/**
 * Lists every date from [startDate] to [endDate], both included.
 * @return the dates, or an empty list if a date isn't correctly formatted or [endDate] is before [startDate].
 */
private fun listDatesBetween(startDate: String, endDate: String): List<String>{
    val dateformat = "yyyy-MM-dd"
    val formatter = SimpleDateFormat(dateformat, Locale.CANADA)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    formatter.isLenient = false

    val start = parseDateOrNull(formatter, startDate) ?: return emptyList()
    val end = parseDateOrNull(formatter, endDate) ?: return emptyList()
    if (end.before(start)) {
        return emptyList()
    }
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.time = start
    val dates = mutableListOf<String>()
    while (!calendar.time.after(end)){
        dates.add(formatter.format(calendar.time))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return dates
}

/**
 * Reads a `yyyy-MM-dd` date.
 * @return the date, or `null` if [text] is not a valid date.
 */
private fun parseDateOrNull(formatter: SimpleDateFormat, text: String): Date?{
    return try{
        formatter.parse(text.trim())
    }
    catch (error: ParseException){
        null
    }
}