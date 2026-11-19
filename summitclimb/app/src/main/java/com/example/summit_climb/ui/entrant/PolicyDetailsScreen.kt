package com.example.summit_climb.ui.entrant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.summit_climb.ui.ScreensBackground
import com.example.summit_climb.ui.charcoal

/**
 * The content for each policy
 *
 * @property code: references the specific policy
 * @property title: the title of the policy
 * @property details: policy description
 */
data class PolicyDetailContent(
    val code: String,
    val title: String,
    val details: String
)

// code, title, and description of each policy
val TERMS_OF_SERVICE = PolicyDetailContent(
    code = "terms",
    title = "Terms of Service",
    details = "By using Summit, you agree to follow our community rules and platform policies. " +
            "You are solely responsible for all activity on your account, including maintaining the accuracy of campsite listings and honoring reservation policies. " +
            "We reserve the right to suspend or terminate accounts that misuse the app or violate these terms."
)

val PRIVACY_POLICY = PolicyDetailContent(
    code = "privacy",
    title = "Privacy Policy",
    details = "By using Summit, you consent to the collection and use of your personal information in accordance with this policy. " +
            "Collected data is used strictly to provide app services, process bookings, and improve your overall outdoor experience. " +
            "Your personal information is never sold or shared with third parties without your consent."
)

val DATA_USE = PolicyDetailContent(
    code = "data_use",
    title = "Data Use",
    details = "We collect information including name, email, phone number and location to support app services. " +
            "Your information is exclusively used to deliver and improve user experience and is never shared with third parties.")

val COMMUNITY_GUIDELINES = PolicyDetailContent(
    code = "guidelines",
    title = "Community Guidelines",
    details = "Be respectful to other campers and hosts. " +
            "Always provide accurate information regarding listings, lotteries, and reservations. " +
            "Content or behavior that is misleading, discriminatory, or unsafe will be removed and offenders may face permanent account termination."
)

val SAFETY_DISCLAIMER = PolicyDetailContent(
    code = "disclaimer",
    title = "Safety Disclaimer",
    details = "Camping, hiking, and related outdoor activities carry inherent and potentially serious risks, " +
            "including personal injury, severe illness, and death. " +
            "Environmental hazards such as extreme weather, wildfires, and wildlife can create sudden, unpredictable emergency situations. " +
            "Summit cannot guarantee conditions, security, or safety at any campsite. By using Summit, you freely assume all risks associated with outdoor activities."
)

val REPORT_PROBLEM = PolicyDetailContent(
    code = "report",
    title = "Report a problem",
    details = "Encountered a user experience issue or a violation of our community guidelines? Let us know at support@summitclimb.com. " +
            "Please include a detailed description of the issue along with any relevant names, dates, or campsites involved to assist our team investigation."
)

val CURRENT_POLICIES = mutableListOf(TERMS_OF_SERVICE, PRIVACY_POLICY, DATA_USE, COMMUNITY_GUIDELINES, SAFETY_DISCLAIMER, REPORT_PROBLEM)

/**
 * Purpose: displays the title and description of each policy (US 01.11.01)
 * @param policyCode: specific code associated with a policy to direct towards appropriate screen
 */
@Composable
fun PolicyDetailsScreen(
    policyCode: String,
    navController: NavController
) {
    val currentPolicy = CURRENT_POLICIES.firstOrNull { it.code == policyCode }

    Box(Modifier.fillMaxSize()) {
        ScreensBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // back button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
            ) {
                BackRow(onBackClick = { navController.popBackStack() })
            }
            Spacer(Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, charcoal)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // displays policy title
                        Text(
                            text = currentPolicy?.title ?: "Sorry, there was an error. Please try again later",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B1D16)
                        )

                        Spacer(Modifier.height(20.dp))

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = charcoal
                        )

                        Spacer(Modifier.height(20.dp))

                        // displays policy description
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
}

