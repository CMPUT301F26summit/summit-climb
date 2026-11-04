package com.example.summit_climb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.test.isToggleable
import org.junit.Before

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ProfileScreen(rememberNavController())
        }
    }
    @Test
    fun showsTitleAndUserInformation() {
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personal Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Photo").assertIsDisplayed()

    }

    @Test
    fun showsAllInputFields() {
        composeTestRule.onNode(hasText("Name") and hasSetTextAction()).assertExists()
        composeTestRule.onNode(hasText("Email") and hasSetTextAction()).assertExists()
        composeTestRule.onNode(hasText("Phone Number") and hasSetTextAction()).assertExists()
    }

    @Test
    fun showsButtonsAndSettingsRows() {
        composeTestRule.onNodeWithText("Save Changes").assertExists()
        composeTestRule.onNodeWithText("Notifications").assertExists()
        composeTestRule.onNode(isToggleable()).assertExists()
        composeTestRule.onNodeWithText("Policies & guidelines").assertExists()
        composeTestRule.onNodeWithText("Delete Account").assertExists()
    }



}
