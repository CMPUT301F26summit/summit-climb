package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.example.summit_climb.model.LotteryStatus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person

// Color Palette
private val darkGrey = Color(0xFFA9A9A9)

/**
 * Campsite Row
 *
 * Displays one campsite in a list, includes:
 * image, name, location, lottery status, occupancy, rating, and price
 *
 * @param modifier modifier applied to a row
 * @param campsite campsite model
 * @param lotteryStatus the lottery status for this campsite, or null if not applicable
 * @param onClick called when the campsite row is clicked
 */

@Composable
fun CampsiteRow(
    campsite: Campsite,
    modifier: Modifier = Modifier,
    lotteryStatus: LotteryStatus? = null,
    onClick: () -> Unit = {}

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
            darkGrey
        else
            Color.White

    val rating =
        if (campsite.ratings.isEmpty())
            "No ratings"
        else
            "%.1f".format(campsite.avgRating)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(lightBlue, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ){
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = campsite.promoImageUrl,
                contentDescription = campsite.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            )

            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ){

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = campsite.name,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .background(
                                lotteryBorderColor,
                                RoundedCornerShape(10.dp)
                            )
                    ){
                        Text(
                            text = lotteryText,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 3.dp
                            ),
                            fontSize = 11.sp,
                            color = lotteryTextColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        Modifier.size(12.dp),
                        tint = charcoal
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = campsite.location,
                        fontSize = 11.sp,
                        color = charcoal
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person, null,
                            Modifier.size(12.dp),
                            tint = charcoal
                        )

                        Spacer(Modifier.width(2.dp))

                        Text(
                            text = "Up to ${campsite.maxOccupancy} guests · ★ $rating",
                            fontSize = 11.sp,
                            color = charcoal
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "$${campsite.price.toInt()} / night",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(greyBlue),
                        contentAlignment = Alignment.Center

                    ){
                        Text(
                            text = ">",
                            color = charcoal
                        )
                    }
                }
            }
        }
    }
}