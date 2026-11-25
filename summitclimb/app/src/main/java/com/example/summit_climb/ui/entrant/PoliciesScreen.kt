package com.example.summit_climb.ui.entrant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import com.example.summit_climb.ui.ScreensBackground
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.theme.screenBackground

/**
 * Purpose: directs users to each of the policies and guidelines for the app
 * @param navController: app's navigation controller, used to open other screens
 */
@Composable
fun PoliciesScreen(
    navController: NavController
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(screenBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
            ) {
                BackRow(onBackClick = { navController.popBackStack() })
            }

            PolicyTitle("Legal")
            PolicySection(TERMS_OF_SERVICE, navController)
            PolicySection(PRIVACY_POLICY, navController)
            PolicySection(DATA_USE, navController)

            PolicyTitle("Community")
            PolicySection(COMMUNITY_GUIDELINES, navController)
            PolicySection(SAFETY_DISCLAIMER, navController)

            PolicyTitle("Support")
            PolicySection(REPORT_PROBLEM, navController)
        }
    }
}

/**
 * Purpose: creates buttons for each policy that directs to its respective policy details screen
 * @param policy: policy content
 * @param navController: app's navigation controller, used to open other screens
 */
@Composable
fun PolicySection(
    policy: PolicyDetailContent,
    navController: NavController
) {
    Card(
        onClick = { navController.navigate("policy_details/${policy.code}") },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, greyBlue)
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
                    text = policy.title,
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

/**
 * Purpose: Establishes the formatting for each policy title
 * @param title: each policy's title
 */
@Composable
fun PolicyTitle(
    title: String
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 6.dp),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = charcoal
    )
}
