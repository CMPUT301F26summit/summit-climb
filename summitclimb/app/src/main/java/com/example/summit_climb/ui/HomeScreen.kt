package com.example.summit_climb.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import com.example.summit_climb.R
import com.example.summit_climb.data.UserRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.summit_climb.model.UserRole
import android.util.Log

/**
 * Background image shown behind the app's screens.
 */
@Composable
fun ScreensBackground() {
    Image(
        painter = painterResource(R.drawable.screens_background),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

/**
 * Summit's landing screen.
 * "Login" signs the user in anonymously and opens the campsite list.
 * "Admin Access" opens the admin dashboard only if the user has the ADMIN role
 *
 * @param navController: navigation used to open the campsite list.
 * @param onAdminClick: called when a user with the ADMIN role clicks Admin Access.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    onAdminClick: () -> Unit = {}
) {
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    fun openAdminIfAccess(uid: String) {
        Log.d("AdminAccess", "My user id: $uid")
        UserRepository().getUser(uid) { user ->
            if (UserRole.ADMIN in user.roles) {
                errorMessage = ""
                onAdminClick()
            } else {
                errorMessage = "Unable to verify access. Please try again."
            }
        }
    }

    if (errorMessage != "") {
        AlertDialog(
            onDismissRequest = { errorMessage = "" },
            title = { Text("Access denied") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { errorMessage = "" }) {
                    Text("Back")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.summit_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = {
                    errorMessage = ""
                    auth.signInAnonymously()
                        .addOnSuccessListener { navController.navigate(Routes.CAMPSITE_LIST) }
                        .addOnFailureListener { errorMessage = "Login failed" }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3963E))
            ){
                Text(
                    text = "Login",
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = {
                    errorMessage = ""
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        openAdminIfAccess(currentUser.uid)
                    } else {
                        auth.signInAnonymously()
                            .addOnSuccessListener { openAdminIfAccess(it.user!!.uid) }
                            .addOnFailureListener {
                                errorMessage = "Unable to verify access. Please try again."
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF245C86))
            ){
                Text(
                    text = "Admin Access",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "© 2026 Summit. All rights reserved.",
                fontSize = 12.sp,
                color = Color.Black
            )

        }
    }
}