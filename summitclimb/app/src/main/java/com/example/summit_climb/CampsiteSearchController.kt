package com.example.summit_climb

/**
 * Handles what an entrant does when finding campsites: search, filter, view details and rate.
 * Screens call this class instead of [CampsiteRepository] directly.
 * Example:
 * val controller = CampsiteSearchController(campsiteRepository)
 * val results = controller.searchCampsites("lake", CampsiteFilter(location = "Jasper", guests = 4))
 *
 * @param campsiteRepository: where campsites come from.
 * @constructor Creates a controller that searches the given repository.
 */
class CampsiteSearchController(private val campsiteRepository: CampsiteRepository) {
    //private val qrService = QRCodeService()

    /**
     * Searches approved campsites by keyword and filters them by location, date and guests number.
     * Each campsite in result has its summary info: name, location,
     * maximum occupancy and average rating.
     *
     * @param keyword: word to search for in the name, description and tags. Empty matches every campsite.
     * @param filter: the location, date range and number of guests. Empty parts match every campsite.
     * @return the matching campsites. Empty if nothing matches.
     */
    fun search(keyword: String, filter: CampsiteFilter): List<Campsite>{
        val approvedCamps = campsiteRepository.approvedCampsites()
        return filterCamps(approvedCamps, keyword, filter)
    }

    /**
     * Gets one campsite with all its details.
     * @param campsiteId: ID of the campsite.
     * @return the campsite, or `null` if no campsite has this ID.
     */
    fun campsiteDetails(campsiteId: String): Campsite? {
        return campsiteRepository.getCampsite(campsiteId)
    }

    /*
    /**
     * Gets a campsite's details from the text of a scanned QR code.
     * @param qrText: raw text the scanner read from the QR code.
     * @return the campsite, or `null` if the QR code is not a campsite code.
     */
    fun getCampFromQR(qrText: String): Campsite?{
        val campsiteId = qrService.campsiteIDFromQRText(qrText) ?: return null
        return campsiteDetails(campsiteId)
    }
    */

    /**
     * Saves an entrant's star rating for a campsite.
     * If the entrant already rated this campsite, the new rating replaces the old one.
     *
     * @param campsiteId: campsite being rated.
     * @param entrantId: [User.userId] of the entrant rating it.
     * @param stars: star value, a whole number from 1 to 5.
     * @return `true` if the rating was saved, `false` if [stars] is not 1 to 5.
     */
    fun rateCampsite(campsiteId: String, entrantId: String, stars: Int): Boolean{
        val rating = Rating(entrantId = entrantId, campsiteId = campsiteId, stars = stars)
        if (!rating.validStars()){
            return false
        }
        campsiteRepository.saveRating(rating)
        return true
    }

    /**
     * Keeps only the campsites that match keyword and filter.
     *
     * @param campsites: campsites to filter.
     * @param keyword: word to search for. Empty matches every campsite.
     * @param filter: location, date range and guests number.
     * @return the campsites that match both, in their original order.
     */
    fun filterCamps(campsites: List<Campsite>, keyword: String, filter: CampsiteFilter): List<Campsite>{
        return campsites.filter { campsite ->
            campsite.keywordMatch(keyword) && campsite.filterMatch(filter)
        }
    }
}