package com.example.summit_climb

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import com.example.summit_climb.ui.owner.ManageQRCodeScreen
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ManageQRCodeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController

    private val lakeCampID = "0Fg6o1BEFVOumnrKQ8m9ZsqAbHP2"
    private val valleyCampID = "DwH0Fa1Av4MoDtTFhmg0xymaZZ83"

    private fun showQRScreen(
        name: String = "Lakeside Campsite",
        id: String = lakeCampID
    ) {
        composeTestRule.setContent {
            ManageQRCodeScreen(
                navController = rememberNavController(),
                campsiteName = name,
                campsiteID = id
            )
        }
    }

    private fun navigation() = navController.currentBackStackEntry?.destination?.route

    // Display the title of the screen
    @Test
    fun displaysTitle() {
        showQRScreen()

        composeTestRule.onNodeWithText("QR Code").assertIsDisplayed()
    }

    // Display instructions for users
    @Test
    fun displaysScanDescription() {
        showQRScreen()

        composeTestRule
            .onNodeWithText("Guests can scan this to open your campsite")
            .assertIsDisplayed()
    }

    // Display corresponding campsite name
    @Test
    fun showCampsiteName() {
        showQRScreen(name = "Lakeside Campsite")

        composeTestRule.onNodeWithText("Lakeside Campsite").assertIsDisplayed()
    }

    // Display campsite ID
    @Test
    fun showCampsiteId() {
        showQRScreen(id = lakeCampID)

        composeTestRule.onNodeWithText("Campsite ID: $lakeCampID").assertIsDisplayed()
    }

    // Each campsite only shows their own details
    @Test
    fun showsDifferentCampsiteDetails() {
        showQRScreen(name = "Valley Camp", id = valleyCampID)

        composeTestRule.onNodeWithText("Valley Camp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campsite ID: $valleyCampID").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lakeside Campsite").assertDoesNotExist()
        composeTestRule.onNodeWithText("Campsite ID: $lakeCampID").assertDoesNotExist()
    }

    // Displays the generated QR code with the campsite name
    @Test
    fun showQRCodeImage() {
        showQRScreen(name = "Lakeside Campsite")

        composeTestRule
            .onNodeWithContentDescription("QR code for Lakeside Campsite")
            .assertIsDisplayed()
    }

    // Do not show unavailability message if the QR code is visible
    @Test
    fun qrCodeAvailableStopsUnavailableMessage() {
        showQRScreen()

        composeTestRule
            .onNodeWithText("Sorry, the QR Code for this campsite is not available yet")
            .assertDoesNotExist()
    }

    // Back button returns to the previous screen
    @Test
    fun backButtonReturnsToPrevScreen() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply { navigatorProvider.addNavigator(
                ComposeNavigator()
            ) }

            NavHost(navController, startDestination = "ownerCampsiteDetails") {
                composable("ownerCampsiteDetails") { }
                composable("manageQRCode") {
                    ManageQRCodeScreen(
                        navController = navController,
                        campsiteName = "Lakeside Campsite",
                        campsiteID = lakeCampID
                    )
                }
            }
        }

        composeTestRule.runOnUiThread { navController.navigate("manageQRCode") }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Back").performClick()

        Assert.assertEquals("ownerCampsiteDetails", navigation())
    }
}