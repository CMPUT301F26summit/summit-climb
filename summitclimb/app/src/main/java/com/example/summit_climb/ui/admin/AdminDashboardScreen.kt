package com.example.summit_climb.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.summit_climb.R
import com.example.summit_climb.ui.Routes

/**
 * Main dashboard for administrators.
 * Provides access to campsite, profile and log management.
 */
@Composable
fun AdminDashboardScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFE7F5))
    ) {
        Image(
            painter = painterResource(R.drawable.mountain_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = "Admin Dashboard",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color(0xFF1C3348)
            )

            Spacer(Modifier.height(32.dp))

            AdminDashboardCard(
                title = "Manage Campsites",
                description = "Review, approve and remove campsites",
                onClick = {
                    navController.navigate(Routes.ADMIN_CAMPSITES)
                }
            )

            AdminDashboardCard(
                title = "Manage Profiles",
                description = "Browse and manage user accounts",
                onClick = {
                    navController.navigate(Routes.ADMIN_PROFILES)
                }
            )

            AdminDashboardCard(
                title = "Images & Logs",
                description = "Review images, notifications and messages",
                onClick = {
                    navController.navigate(Routes.ADMIN_LOGS)
                }
            )
        }
    }
}

/**
 * Reusable navigation card for administrative actions.
 */
@Composable
private fun AdminDashboardCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF245C86)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF597A94)
                )
            }

            Text(
                text = "›",
                fontSize = 28.sp,
                color = Color(0xFF245C86)
            )
        }
    }
}