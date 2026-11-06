package com.example.summit_climb

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import org.junit.Rule
import org.junit.Test

/**
 * User Interface tests for the profile screen
 * Checks that the text, input fields, buttons, and notifications
 * are properly represented with the correct values
 * Checks for error messages and error handling
 */
class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController
    lateinit var controller: ProfileController


    private fun showProfile(
        name: String = "Mila",
        email: String = "mila@example.com",
        phone: String = "7805551234",
        notifications: Boolean = true
    ) {
        controller = ProfileController().apply {
            this.name = name
            this.email = email
            this.phone = phone
            this.notificationsStatus = notifications
        }
    }

    private fun navigation() = navController.currentBackStackEntry?.destination?.route

    // show information
    @Test
    fun showTitle() {
        showProfile()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun showChangePhoto() {
        showProfile()
        composeTestRule.onNodeWithText("Change photo").assertIsDisplayed()
    }

    @Test
    fun showPersonalInformation() {
        showProfile()
        composeTestRule.onNodeWithText("Personal Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Changes").assertIsDisplayed()
    }

    @Test
    fun showsAllInputFields() {
        showProfile()
        composeTestRule.onNodeWithText("Mila").assertIsDisplayed()
        composeTestRule.onNodeWithText("mila@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("7805551234").assertIsDisplayed()
    }

    @Test
    fun displaysDeleteAccountButton() {
        showProfile()

        composeTestRule.onNodeWithText("Delete Account").assertIsDisplayed()
    }

    @Test
    fun showsNotificationsAndPolicies() {
        showProfile()

        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Policies & guidelines").assertIsDisplayed()
    }

    // tests notification switch
    @Test
    fun notificationsSwitchCanBeTurnedOn() {
        showProfile(notifications = true)

        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun notificationsSwitchCanBeTurnedOff() {
        showProfile(notifications = false)

        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    // error handling
    @Test
    fun noErrorMessageAtStart() {
        showProfile()

        composeTestRule.onNodeWithText("Unable to save profile").assertDoesNotExist()
        composeTestRule.onNodeWithText("Unable to delete account").assertDoesNotExist()
        composeTestRule.onNodeWithText("Unable to update notifications").assertDoesNotExist()
    }
}