package com.example.summit_climb

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.navigation.compose.composable
import org.junit.Test
import junit.framework.TestCase.assertNotNull
import androidx.navigation.compose.NavHost
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertEquals
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.ComposeNavigator
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.ui.owner.OwnerEditCampsite

class OwnerEditCampIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private var saved: Campsite? = null
    private var deleted: Campsite? = null

    //sample campsite
    private val sample = Campsite(
        id = "sample-lakeview",
        name = "Lakeview",
        location = "Banff, AB",
        maxOccupancy = 6,
        price = 60.0,
        description = "A quiet site.",
        amenities = listOf("Parking", "Fire"),
        tags = listOf("climbing", "river"),
        rules = "Quiet hours 10 PM - 7 AM.",
        promoImageUrl = ""
    )

    private fun launch() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController, startDestination = "ownerCampsites") {
                composable("ownerCampsites") { Text("campsite list") }
                composable("edit") {
                    OwnerEditCampsite(
                        navController = navController,
                        campsite = sample,
                        saveCampsite = { saved = it },
                        deleteCampsite = { deleted = it }
                    )
                }
            }
        }
        composeRule.runOnUiThread { navController.navigate("edit") }
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

    @Test
    fun fillsFieldsFromCampsite() {
        launch()
        composeRule.onNodeWithTag("campsiteName").assertTextContains("Lakeview")
        composeRule.onNodeWithTag("campLocation").assertTextContains("Banff, AB")
        composeRule.onNodeWithTag("pricePerNight").assertTextContains("60")
    }

    @Test
    fun save_returnsEdited() {
        launch()
        typeInto("campsiteName", "  Renamed Camp  ")
        typeInto("pricePerNight", "75")
        clickTag("AmenityTrails")
        clickTag("saveButton")
        composeRule.waitForIdle()

        val result = saved
        assertNotNull(result)
        assertEquals(sample.id, result!!.id)
        assertEquals("Renamed Camp", result.name)          // trimmed
        assertEquals(75.0, result.price, 0.001)
        assertEquals(true, "Trails" in result.amenities)
        assertEquals("ownerCampsites", currentRoute())
    }

    @Test
    fun cancel_buttonNoSaving() {
        launch()
        typeInto("campsiteName", "Not Saved")
        composeRule.onNodeWithText("Back").performClick()
        composeRule.waitForIdle()

        assertNull(saved)
        assertEquals("ownerCampsites", currentRoute())
    }

    @Test
    fun delete_cancelKeepsCampsite() {
        launch()
        clickTag("deleteCampsite")
        composeRule.onNodeWithText("Delete this campsite?").assertExists()

        clickTag("undoDeleteButton")
        composeRule.onNodeWithText("Delete this campsite?").assertDoesNotExist()
        assertNull(deleted)
        assertEquals("edit", currentRoute())
    }

    @Test
    fun delete_returnsToList() {
        launch()
        clickTag("deleteCampsite")
        clickTag("doDeleteButton")
        composeRule.waitForIdle()

        assertEquals(sample, deleted)
        assertEquals("ownerCampsites", currentRoute())
    }

    @Test
    fun save_withEmptyPrice() {
        launch()
        typeInto("pricePerNight", "")
        clickTag("saveButton")
        composeRule.waitForIdle()

        assertNotNull(saved)
    }
}
