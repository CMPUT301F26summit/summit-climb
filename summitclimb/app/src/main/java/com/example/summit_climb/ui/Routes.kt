package com.example.summit_climb.ui

/**
 * Every navigation route in the app.
 * I think this will reduce crash risk caused by typo in filename
 */
object Routes {
    const val HOME_SCREEN = "homeScreen"
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

    // Entrant screens

    /** Argument name for the campsite ID in every route that has one.*/
    const val CAMPSITE_ID_ARG = "campsiteId"

    /** Route pattern for campsite details, registered in NavHost.*/
    const val CAMPSITE_DETAILS = "entrantCampDetails/{$CAMPSITE_ID_ARG}"

    /** The user's notifications (opened from the bell on the campsite list).*/
    const val NOTIFICATIONS = "entrantNotifs"

    /** QR code scanner (opened from the scan button on the campsite list).*/
    const val QR_SCANNER = "QRScanner"

    /** Policies & guidelines (opened from the profile screen).*/
    const val POLICIES = "policies"

    // Owner screens

    /** Pattern: owner's view of one campsite.*/
    const val OWNER_CAMPSITE_DETAILS = "ownerCampsiteDetails/{$CAMPSITE_ID_ARG}"

    /** Pattern: edit one existing campsite.*/
    const val OWNER_EDIT_CAMPSITE = "ownerEditCampsite/{$CAMPSITE_ID_ARG}"

    /**
     * Register a NEW campsite. Plain route with no ID, because a campsite that
     * doesn't exist yet has no ID. The (+) button navigates straight to this.
     */
    const val OWNER_REGISTER_CAMPSITE = "ownerRegisterCamp"

    /** Pattern: owner's management menu for one campsite.*/
    const val OWNER_MANAGE_CAMPSITE = "ownerManageCamp/{$CAMPSITE_ID_ARG}"

    /** Pattern: list one campsite in a lottery.*/
    const val OWNER_LIST_LOTTERY = "ownerListLottery/{$CAMPSITE_ID_ARG}"

    /** Pattern: waitlist of one campsite (opened from the manage menu).*/
    const val OWNER_WAITLIST = "ownerWaitlist/{$CAMPSITE_ID_ARG}"

    /** Owner's active reservations (opened from the manage menu).*/
    const val OWNER_ACTIVE_RESERVATIONS = "ownerActiveReserve"

    /** Notifications the owner has sent (opened from the manage menu).*/
    const val OWNER_SENT_NOTIFICATIONS = "ownerSentNotifications"

    /** Owner's QR code screen (opened from the manage menu).*/
    const val MANAGE_QR_CODE = "manageQRCode"


    /**
     * Builds the route that opens one campsite's details.
     * @param campsiteId: ID of the campsite to show.
     * @return for example `"entrantCampDetails/abc123"`.
     */
    fun campsiteDetails(campsiteId: String): String {
        return "entrantCampDetails/$campsiteId"
    }
/*
    /** @return for example `"ownerCampsiteDetails/abc123"`.*/
    fun ownerCampsiteDetails(campsiteId: String): String {
        return "ownerCampsiteDetails/$campsiteId"
    }*/

    //testing
    fun ownerCampsiteDetails(campsiteId: String): String {
        return OWNER_CAMPSITE_DETAILS.replace("{$CAMPSITE_ID_ARG}", campsiteId)
    }

    /** @return for example `"ownerEditCampsite/abc123"`.*/
   /* fun ownerEditCampsite(campsiteId: String): String {
        return "ownerEditCampsite/$campsiteId"
    }*/

    // testing
    fun ownerEditCampsite(campsiteId: String): String {
        return OWNER_EDIT_CAMPSITE.replace("{$CAMPSITE_ID_ARG}", campsiteId)
    }

    /** @return for example `"ownerManageCamp/abc123"`.*/
   /* fun ownerManageCampsite(campsiteId: String): String {
        return "ownerManageCamp/$campsiteId"
    }*/

    //testing
    fun ownerManageCampsite(campsiteId: String): String {
        return OWNER_MANAGE_CAMPSITE.replace("{$CAMPSITE_ID_ARG}", campsiteId)
    }

    /** @return for example `"ownerListLottery/abc123"`.*/
    /*fun ownerListLottery(campsiteId: String): String {
        return "ownerListLottery/$campsiteId"
    }*/

    //testing
    fun ownerListLottery(campsiteId: String): String {
        return OWNER_LIST_LOTTERY.replace("{$CAMPSITE_ID_ARG}", campsiteId)
    }

    /** @return for example `"ownerWaitlist/abc123"`.*/
    fun ownerWaitlist(campsiteId: String): String {
        return "ownerWaitlist/$campsiteId"
    }
}