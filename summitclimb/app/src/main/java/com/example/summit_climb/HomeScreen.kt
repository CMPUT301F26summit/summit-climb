package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
@Composable
fun HomeScreen(navController: NavController, container: AppContainer){
    var userId by remember { mutableStateOf(container.currUserId) }
    var loginMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    auth.signInAnonymously()
                        .addOnSuccessListener {
                            userId = container.currUserId
                            loginMessage = "Login successful"
                            navController.navigate("entrantCampsiteList")
                        }

                        .addOnFailureListener {
                            loginMessage = "Login failed, perhaps new user"
                        }
                },

                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3963E)
                )
            ) {
                Text(
                    text = "Login",
                    fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}