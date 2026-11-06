package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import java.time.LocalDate
import com.example.summit_climb.model.LotteryStatus

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Campsite detail screen for an owner's campsite listing.
 *
 * US 02.01.03, US 02.01.04, US 02.04.01, US 02.04.06, US 02.05.01, US 02.06.01
 *
 * Shows campsite image, name, location, price, rating, lottery status, description,
 * amenities, and an availability calendar, with buttons to edit the campsite, manage it, or list it in a lottery
 *
 * Outstanding: the map shows coordinates until the shared map component is ready.
 *
 * @param navController navigation for manage campsite, edit lottery, edit campsite, and back buttons.
 * @param modifier modifier applied to the individual campsite listing screen
 * @param campsite campsite model
 * @param lotteryStatus the lottery status for this campsite, or null if it has no lottery
 */
@Composable
fun OwnerCampsiteDetails(
    navController: NavController,
    campsite: Campsite,
    modifier: Modifier = Modifier,
    lotteryStatus: LotteryStatus? = null,

){
    val backButton = { navController.popBackStack(); Unit }
    val editCampsite = { navController.navigate(Routes.OWNER_EDIT_CAMPSITE) }
    val manageCampsite = { navController.navigate(Routes.OWNER_MANAGE_CAMPSITE) }
    val listInLottery = { navController.navigate(Routes.OWNER_LIST_LOTTERY) }

    val selectedAmenities = amenityDrawables.filter { it.name in campsite.amenities }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val bookedDates = remember(campsite.bookedDates){
        campsite.bookedDates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet()
    }

    Scaffold(modifier = modifier) { padding ->
        Column(Modifier
            .padding(padding)
            .background(Color.White)
        ){

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ){
                Row(
                    Modifier
                        .clickable(onClick = backButton)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button",
                        modifier = Modifier.size(20.dp),
                        tint = charcoal
                    )

                    Text(
                        text = "Back",
                        color = charcoal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)

                    )
                }
                AsyncImage(
                    model = campsite.promoImageUrl,
                    contentDescription = campsite.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = campsite.name,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            Icon(
                                Icons.Default.LocationOn, null,
                                Modifier.size(15.dp),
                                tint = charcoal
                            )
                            Text(
                                text = campsite.location,
                                color = charcoal,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${campsite.price.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = " / night (up to ${campsite.maxOccupancy} guests)",
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ){
                        val lotteryText = when (lotteryStatus) {
                            LotteryStatus.OPEN -> "Lottery Open"
                            LotteryStatus.DRAWN -> "Drawn"
                            LotteryStatus.CLOSED -> "Closed"
                            null -> "No Lottery"
                        }

                        val lotteryBorderColor = when (lotteryStatus) {
                            LotteryStatus.OPEN -> lightGreen
                            LotteryStatus.DRAWN -> lightOrange
                            LotteryStatus.CLOSED -> lightRed
                            null -> beige
                        }

                        val lotteryTextColor =
                            if (lotteryStatus == null)
                                sonicSilver
                            else
                                Color.White

                        Box(
                            Modifier
                                .background(color = greyBlue, RoundedCornerShape(14.dp))
                                .clickable(onClick = editCampsite)
                        ){
                            Row(
                                Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "Edit Campsite",
                                    fontSize = 12.sp,
                                    color = charcoal
                                )

                                Spacer(Modifier.width(6.dp))

                                Icon(
                                    Icons.Default.Edit, null,
                                    Modifier.size(12.dp),
                                    tint = charcoal
                                )
                            }
                        }

                        Box(
                            Modifier
                                .background(
                                    lotteryBorderColor,
                                    RoundedCornerShape(10.dp)
                                )
                        ){
                            Text(
                                text = lotteryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = lotteryTextColor
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(Icons.Default.StarBorder,
                                null,
                                Modifier.size(14.dp)
                            )

                            val rating =
                                if (campsite.ratings.isEmpty())
                                    "No ratings yet"
                                else
                                    "%.1f".format(campsite.avgRating)

                            Text(
                                text = " $rating",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(greyBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ){
                    Text(
                        text = "About this Campsite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = campsite.description,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = charcoal
                    )
                }

                Column(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(greyBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ){
                    if (selectedAmenities.isEmpty()) {
                        Text(
                            text = "No amenities listed",
                            fontSize = 13.sp,
                            color = charcoal
                        )

                    } else {
                        Row(
                            Modifier
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ){
                            selectedAmenities.forEach { amenity -> AmenityBorder(amenity) }
                        }
                    }
                }

                Column(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(greyBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ){
                    AvailabilityCalendar(
                        bookedDates = bookedDates,
                        selectedDate = selectedDate ?: LocalDate.now(),
                        onDateSelected = { selectedDate = it }
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Button(
                        onClick = manageCampsite,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greyBlue,
                            contentColor = charcoal
                        )
                    ){
                        Text(
                            text = "Manage",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(12.dp))

                        Icon(
                            Icons.Default.ArrowCircleRight,
                            null,
                            Modifier.size(26.dp),
                            tint = charcoal
                        )
                    }
                    Button(
                        onClick = listInLottery,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greyBlue,
                            contentColor = Color.White
                        )
                    ){
                        Text(
                            text = "Edit Lottery",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = charcoal
                        )
                    }
                }
            }
        }
    }
}
