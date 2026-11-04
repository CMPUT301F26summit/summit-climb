package com.example.summit_climb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PolicyDetailsScreen(
    policyCode: String,
    navController: NavController
) {
    val currentPolicy = currentPolicies.firstOrNull { it.code == policyCode }

    Column(
        modifier = Modifier
            .background(Color(0xFFFAF3EB))
            .fillMaxSize()
            .padding(12.dp)
    ) {
        TextButton(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            onClick = { navController.popBackStack() }
        ) {
            Text(
                text = "←  Policies",
                fontSize = 18.sp,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3EB))
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFEBDCCB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = currentPolicy?.title
                            ?: "Sorry, there was an error. Please try again later",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B1D16)
                    )

                    Spacer(Modifier.height(20.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFEBDCCB)
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = currentPolicy?.details ?: "",
                        lineHeight = 26.sp,
                        color = Color(0xFF4A3B30)
                    )
                }
            }
        }
    }
}
