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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import java.time.LocalDate
import com.example.summit_climb.model.LotteryStatus

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// Color Palette
private val charcoal = Color(0xFF36454F)
private val lightGreen = Color(0xFF96B38E)
private val lightRed = Color(0xFFFF7F7F)
private val lightOrange = Color(0xFFFFA759)
private val greyBlue = Color(0xFFC9DEF3)
private val lightBlue = Color(0xFFA6C8EB)

/** Holds the amenity name and icon. */
private data class Amenity(val name: String, val icon: ImageVector)

private val amenityIcons = listOf(
    Amenity("Parking", Icons.Default.LocalParking),
    Amenity("Washrooms", Icons.Default.Wc),
    Amenity("Fire", Icons.Default.LocalFireDepartment),
    Amenity("Trails", Icons.Default.Hiking),
    Amenity("Picnic tables", Icons.Default.TableRestaurant),
    Amenity("Water Tap", Icons.Default.WaterDrop)
)

/**
 * Amenity border for an amenity icon.
 *
 * @param amenity displaying the amenity
 * @param modifier modifier applied to the box
 */
@Composable
private fun AmenityBorder(
    amenity: Amenity,
    modifier: Modifier = Modifier
){
    Column(
        modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(lightBlue),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Icon(amenity.icon,
            contentDescription = null,
            Modifier.size(26.dp),
            tint = charcoal
        )
    }
}

/**
 * Campsite detail screen for an owner's campsite listing.
 *
 * Shows campsite image, name, location, price, rating, lottery status, description,
 * amenities, and an availability calendar, with buttons to edit the campsite, manage it, or list it in a lottery
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
    val editCampsite = { navController.navigate("ownerEditCamp/${campsite.id}") }
    val manageCampsite = { navController.navigate("manageCampsiteScreen/${campsite.id}") }
    val listInLottery = { navController.navigate("ownerListLottery") }

    val selectedAmenities = amenityIcons.filter { it.name in campsite.amenities }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val bookedDates = remember(campsite.bookedDates){
        campsite.bookedDates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet()
    }

    Scaffold(modifier = modifier) { padding ->
        Column(Modifier.padding(padding)) {

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
                            null -> Color(0xFFDADAD8)
                        }

                        val lotteryTextColor =
                            if (lotteryStatus == null)
                                Color(0xFF757575)
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
                            Icon(Icons.Default.StarBorder, null,
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
                        selectedDate = selectedDate,
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
