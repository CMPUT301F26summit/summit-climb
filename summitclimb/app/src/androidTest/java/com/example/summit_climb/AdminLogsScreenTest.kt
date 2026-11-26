package com.example.summit_climb

import androidx.compose.material3.MaterialTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.User
import com.example.summit_climb.ui.admin.AdminImageType
import com.example.summit_climb.ui.admin.AdminImageItem
import com.example.summit_climb.ui.admin.AdminLogsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for AdminLogsScreen image removal.
 * Uses sample campsite and user data instead of Firestore.
 */
@RunWith(AndroidJUnit4::class)
class AdminLogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCampsite = Campsite(
        id = "campsite-1",
        name = "Cedar Valley",
        ownerId = "owner-1",
        promoImageUrl = "https://example.com/campsite.jpg"
    )

    private val testUser = User(
        userId = "user-1",
        name = "Alex",
        profileImageUrl = "https://example.com/profile.jpg"
    )

    @Test
    fun removing_campsite_image_calls_removal_callback() {
        var removedImage: AdminImageItem? = null

        composeTestRule.setContent {
            MaterialTheme {
                AdminLogsScreen(
                    campsites = listOf(testCampsite),
                    users = emptyList(),
                    notifications = emptyList(),
                    onRemoveImage = { item, onDone ->
                        removedImage = item
                        onDone(true)
                    }
                )
            }
        }

        // Open the removal confirmation
        composeTestRule
            .onNodeWithContentDescription("Remove image")
            .performClick()

        composeTestRule
            .onNodeWithText("Remove Image?")
            .assertExists()

        // Confirm removal
        composeTestRule
            .onNodeWithText("Remove")
            .performClick()

        // Verify the correct campsite image was selected
        assertEquals("campsite-1", removedImage?.id)
        assertEquals(AdminImageType.CAMPSITE, removedImage?.type)
    }

    @Test
    fun removing_profile_image_calls_removal_callback() {
        var removedImage: AdminImageItem? = null

        composeTestRule.setContent {
            MaterialTheme {
                AdminLogsScreen(
                    campsites = emptyList(),
                    users = listOf(testUser),
                    notifications = emptyList(),
                    onRemoveImage = { item, onDone ->
                        removedImage = item
                        onDone(true)
                    }
                )
            }
        }

        // Open the removal confirmation
        composeTestRule
            .onNodeWithContentDescription("Remove image")
            .performClick()

        // Confirm removal
        composeTestRule
            .onNodeWithText("Remove")
            .performClick()

        // Verify the correct profile image was selected
        assertEquals("user-1", removedImage?.id)
        assertEquals(AdminImageType.PROFILE, removedImage?.type)
    }

    @Test
    fun cancelling_image_removal_does_not_call_callback() {
        var removedImage: AdminImageItem? = null

        composeTestRule.setContent {
            MaterialTheme {
                AdminLogsScreen(
                    campsites = listOf(testCampsite),
                    users = emptyList(),
                    notifications = emptyList(),
                    onRemoveImage = { item, onDone ->
                        removedImage = item
                        onDone(true)
                    }
                )
            }
        }

        // Open the removal confirmation
        composeTestRule
            .onNodeWithContentDescription("Remove image")
            .performClick()

        // Cancel removal
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Verify removal was never requested
        assertNull(removedImage)

        composeTestRule
            .onNodeWithText("Remove Image?")
            .assertDoesNotExist()
    }

    @Test
    fun failed_image_removal_shows_error_message() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminLogsScreen(
                    campsites = listOf(testCampsite),
                    users = emptyList(),
                    notifications = emptyList(),
                    onRemoveImage = { _, onDone ->
                        onDone(false)
                    }
                )
            }
        }

        // Open the removal confirmation
        composeTestRule
            .onNodeWithContentDescription("Remove image")
            .performClick()

        // Confirm removal
        composeTestRule
            .onNodeWithText("Remove")
            .performClick()

        // Verify the error is displayed
        composeTestRule
            .onNodeWithText("Unable to Remove Image")
            .assertExists()
    }
}
