package com.example.summit_climb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PoliciesScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .background(Color(0xFFFAF3EB))
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PolicyTitle("Legal")
        PolicySection("Terms of Service", navController, "policy_details/terms")
        PolicySection("Privacy Policy", navController, "policy_details/privacy")
        PolicySection("Data Use", navController, "policy_details/data_use")

        PolicyTitle("Community")
        PolicySection("Community Guidelines", navController, "policy_details/guidelines")
        PolicySection("Safety Disclaimer", navController, "policy_details/disclaimer")

        PolicyTitle("Support")
        PolicySection("Report a problem", navController, "policy_details/report")
    }
}

@Composable
fun PolicySection(
    title: String,
    navController: NavController,
    nextScreen: String
) {
    Card(
        onClick = { navController.navigate(nextScreen) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, Color(0xFFEBDCCB))
    ) {
        Column(
            Modifier
                .background(Color(0xFFFFFFFF))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = Color(0xFF2B1D16),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ">",
                    color = Color(0xFF2B1D16)
                )

            }
        }
    }
}

@Composable
fun PolicyTitle(
    title: String
) {
    Text(
        text = title,
        fontSize = 20.sp,
        color = Color(0xFF2B1D16)
    )
}
