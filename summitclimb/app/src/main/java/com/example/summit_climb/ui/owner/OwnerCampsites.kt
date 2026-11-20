package com.example.summit_climb.ui.owner
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.model.LotteryStatus
import com.example.summit_climb.ui.ScreensBackground
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.lazy.items
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.sonicSilver


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.summit_climb.R
import com.example.summit_climb.ui.Routes
import com.example.summit_climb.model.Campsite


/**
 * List of campsites listed by entrant (owner)
 *
 * US 02.01.01, US 02.03.01
 *
 * Shows a scrollable list of listed campsites or none if the owner has none yet
 * There is also a (+) button to  register a new campsite
 * Tapping a listing opens that campsite's details
 *
 *
 * @param navController navigation for the register button and for opening campsite details
 * @param modifier modifier applied to listing screen
 * @param lotteryStatus lottery status for each campsite by campsite id
 * @param campsites campsite model
 */
@Composable
fun OwnerCampsites(
    navController: NavController,
    modifier: Modifier = Modifier,
    lotteryStatus: Map<String, LotteryStatus> = emptyMap(),
    campsites: List<Campsite> = emptyList()

) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.White,

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("ownerRegisterCamp")
                },
                shape = CircleShape,
                containerColor = lightBlue,
                contentColor = charcoal

            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add campsite"
                )
            }
        }

    ) { padding ->
        Box(Modifier
                .fillMaxSize()
                .background(Color(0xD8D3E8F3))
            ) {
            Image(
                painter = painterResource(R.drawable.mountain_header),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) )
            Column(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(55.dp))

                Text(
                    text = "My Campsites",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color(0xFF2C4352),
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 12.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (campsites.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap + to list a campsite.",
                            color = sonicSilver
                        )

                    }

                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(campsites, key = { it.id }) { c ->
                            CampsiteRow(
                                campsite = c,
                                lotteryStatus = lotteryStatus[c.id]
                            ) // uncomment in actual { navController.navigate(Routes.OWNER_CAMPSITE_DETAILS) }
                            { navController.navigate(Routes.ownerCampsiteDetails(c.id)) } // for testing
                        }
                    }
                }
            }
        }
    }
}

