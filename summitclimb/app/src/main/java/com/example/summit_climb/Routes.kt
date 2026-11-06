package com.example.summit_climb

/**
 * Every navigation route in the app.
 * I think this will reduce crash risk caused by typo in filename
 */
object Routes {
    // Bottom bar tabs

    /** Entrant home: the campsite list.*/
    const val CAMPSITE_LIST = "entrantCampsiteList"

    /** The entrant's reservations.*/
    const val MY_RESERVATIONS = "entrantReservations"

    /** The lotteries the entrant joined.*/
    const val MY_LOTTERIES = "entrantLotteries"

    /** The owner's campsites.*/
    const val OWNER_CAMPSITES = "ownerCampsites"

    /** The user's profile. */
    const val PROFILE = "entrantProfile"

    // Other screens

    /** Argument name for the campsite ID in [CAMPSITE_DETAILS].*/
    const val CAMPSITE_ID_ARG = "campsiteId"

    /** Route pattern for campsite details, registered in NavHost.*/
    const val CAMPSITE_DETAILS = "entrantCampDetails/{$CAMPSITE_ID_ARG}"

    /** The user's notifications (opened from the bell on the campsite list).*/
    const val NOTIFICATIONS = "entrantNotifs"

    const val OWNER_CAMPSITE_DETAILS = "ownerCampsiteDetails/{$CAMPSITE_ID_ARG}"

    const val OWNER_EDIT_CAMPSITE = "ownerEditCampsite/{$CAMPSITE_ID_ARG}"

    const val OWNER_REGISTER_CAMPSITE = "ownerRegisterCamp/{$CAMPSITE_ID_ARG}"

    const val OWNER_MANAGE_CAMPSITE = "ownerManageCamp/{$CAMPSITE_ID_ARG}"

    const val OWNER_LIST_LOTTERY = "ownerListLottery/{$CAMPSITE_ID_ARG}"

    /**
     * Builds the route that opens one campsite's details.
     * @param campsiteId: ID of the campsite to show.
     * @return for example `"entrantCampDetails/abc123"`.
     */
    fun campsiteDetails(campsiteId: String): String {
        return "entrantCampDetails/$campsiteId"
    }
}