package com.example.summit_climb.ui.entrant

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.summit_climb.controller.ProfileController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.AsyncImage
import androidx.compose.foundation.border
import com.example.summit_climb.ui.ScreensBackground
import androidx.compose.material3.SwitchDefaults
import com.example.summit_climb.ui.Routes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect

import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.sonicSilver

/**
 * Purpose: the entrant's Profile Screen (US 01.07.03, US 01.08.01, US 01.08.02)
 * Allows Users to Choose and Update Profile Photos
 * Displays User's name, email, and phone number and allows users to update information
 * Toggles Notifications for User
 * Directs towards app policies and guidelines
 * Opened from the bottom bar's Profile button
 * @param navController: app's navigation controller, used to open other screens
 * @param controller: obtains and updates information about the user
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    controller: ProfileController = viewModel()
) {

    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var showDeletePopup by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * The following code for profile photo selection contains code
     * from Chase at https://medium.com/@jpmtech/jetpack-compose-display-a-photo-picker-6bcb9b357a3a
     */
    val photoSelector =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                error = null
                controller.updateProfilePhoto(uri, onFailure = { error = it.message })
            }
        }

    // display the error message in the snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            error = null
        }
    }

    // display the non-error message in the snackbar
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F8FB))
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
        ) {

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Profile",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.3f)
                    .padding(2.dp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF204F62),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.5.dp,
                            color = greyBlue,
                            shape = RoundedCornerShape(90.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { // displays Modifiable Profile Image
                    if (controller.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = controller.photoUrl,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = controller.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Change photo",
                    fontSize = 13.sp,
                    color = charcoal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(enabled = !controller.uploadPhoto) {
                            photoSelector.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // displays User's Personal Information (name, email, phone number)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = charcoal
                    )

                    // allows users to modify personal information
                    OutlinedTextField(
                        value = controller.name,
                        onValueChange = { controller.name = it },
                        label = { Text(text = "Name", color = charcoal) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = controller.email,
                        onValueChange = { controller.email = it },
                        label = { Text(text = "Email", color = charcoal) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = controller.phone,
                        onValueChange = { controller.phone = it },
                        label = { Text(text = "Phone Number", color = charcoal) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // saves changes to personal information
                    Button(
                        onClick = {
                            error = null
                            message = null
                            controller.saveProfile(
                                onSuccess = { message = "Profile Saved!" },
                                onFailure = { error = it.message }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greyBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save Changes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = charcoal
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // displays notifications switch and policies/guidelines buttons
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Notifications",
                                fontSize = 15.sp,
                                color = charcoal
                            )
                        }
                        // allows users to keep or remove notifications
                        Switch(
                            checked = controller.notificationsStatus,
                            onCheckedChange = { on ->
                                error = null
                                controller.setNotifications(on, onFailure = { error = it.message })
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = lightBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = sonicSilver,
                                uncheckedBorderColor = sonicSilver
                            )
                        )
                    }

                    // allows users to view app policies and guidelines
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Routes.POLICIES) }
                            .height(56.dp)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Policies & guidelines",
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // deletes the user's account
            Button(
                onClick = {
                    showDeletePopup = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightRed,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            // displays a pop-up to confirm that the user wants to delete account
            if (showDeletePopup) {
                AlertDialog(
                    onDismissRequest = { showDeletePopup = false },
                    title = { Text("Delete Account?", fontSize = 20.sp) },
                    text = { Text("Your account will be permanently deleted. This action cannot be undone.", fontSize = 14.sp) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeletePopup = false
                            error = null
                            // controller.deleteProfile(onFailure = { error = it.message })
                        }) {
                            Text("Delete", color = lightRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeletePopup = false }) {
                            Text("Cancel", color = charcoal)
                        }
                    },
                    containerColor = Color.White
                )
            }
        }

        // The implementation of the snackbar used code from Android Developers at
        // https://developer.android.com/develop/ui/compose/components/snackbar 2026
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}



