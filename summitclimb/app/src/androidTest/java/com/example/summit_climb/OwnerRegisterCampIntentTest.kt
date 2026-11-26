package com.example.summit_climb

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.navigation.compose.composable
import org.junit.Test
import junit.framework.TestCase.assertNotNull
import androidx.compose.ui.test.assertIsEnabled
import androidx.navigation.compose.NavHost
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.ComposeNavigator
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsNotEnabled
import junit.framework.TestCase.assertTrue
import androidx.compose.ui.test.onNodeWithContentDescription

import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.ui.owner.OwnerRegisterCampsite

class OwnerRegisterCampIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private var registered: Campsite? = null

    private fun launch() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController, startDestination = "ownerCampsites") {
                composable("ownerCampsites") { Text("campsite list") }
                composable("register") {
                    OwnerRegisterCampsite(
                        navController = navController,
                        campsite = Campsite(),            // ADJUST if Campsite() has no defaults
                        registerCampsite = { registered = it }
                    )
                }
            }
        }
        composeRule.runOnUiThread { navController.navigate("register") }
        composeRule.waitForIdle()
    }

    private fun currentRoute() = navController.currentBackStackEntry?.destination?.route

    private fun typeInto(tag: String, text: String) {
        composeRule.onNodeWithTag(tag).performScrollTo().performTextClearance()
        composeRule.onNodeWithTag(tag).performTextInput(text)
    }

    private fun clickTag(tag: String) {
        composeRule.onNodeWithTag(tag).performScrollTo().performClick()
    }

    private fun fillRequiredFields() {
        typeInto("campsiteName", "Test Camp")
        typeInto("campLocation", "Banff, AB")
        typeInto("pricePerNight", "45")
    }


    @Test
    fun startsEmpty_withRegisterDisabled() {
        launch()
        composeRule.onNodeWithText("Register Campsite").assertExists()
        composeRule.onNodeWithTag("campsiteName").assertTextEquals("")
        composeRule.onNodeWithTag("campLocation").assertTextEquals("")
        composeRule.onNodeWithText("Amenities · 0 selected").assertExists()
        composeRule.onNodeWithTag("registerButton").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun registerButton_needsBothNameAndLocation() {
        launch()
        typeInto("campsiteName", "Test Camp")
        composeRule.onNodeWithTag("registerButton").performScrollTo().assertIsNotEnabled()

        typeInto("campLocation", "Banff, AB")
        composeRule.onNodeWithTag("registerButton").performScrollTo().assertIsEnabled()
    }

    @Test
    fun price_rejectsNonDigitInput() {
        launch()
        typeInto("pricePerNight", "45")
        composeRule.onNodeWithTag("pricePerNight").performTextInput("x")
        composeRule.onNodeWithTag("pricePerNight").assertTextEquals("45")

    }

    @Test
    fun register_returnsEnteredValuesAndGoesBack() {
        launch()
        fillRequiredFields()
        composeRule.onNodeWithContentDescription("More people").performScrollTo().performClick()
        clickTag("amenity_Trails")
        composeRule.onNodeWithText("+ Add tag").performScrollTo().performClick()
        composeRule.onNodeWithTag("newTag").performTextInput("Lake")
        composeRule.onNodeWithText("Add").performScrollTo().performClick()
        clickTag("registerButton")
        composeRule.waitForIdle()

        val result = registered
        assertNotNull(result)
        assertEquals("Test Camp", result!!.name)
        assertEquals("Banff, AB", result.location)
        assertEquals(45.0, result.price, 0.001)
        assertEquals(2, result.maxOccupancy)
        assertTrue("Trails" in result.amenities)
        assertTrue("lake" in result.tags)                 // lowercased
        assertEquals("ownerCampsites", currentRoute())
    }

    @Test
    fun register_withEmptyPrice_doesNotCrash() {
        launch()
        fillRequiredFields()
        typeInto("pricePerNight", "")
        clickTag("registerButton")
        composeRule.waitForIdle()

        assertNotNull(registered)
    }


    @Test
    fun backAndCancel_leaveWithoutRegistering() {
        launch()
        fillRequiredFields()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.waitForIdle()

        assertNull(registered)
        assertEquals("ownerCampsites", currentRoute())
    }
}