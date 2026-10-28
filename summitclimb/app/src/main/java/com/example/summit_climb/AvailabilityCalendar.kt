package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import java.time.YearMonth
import java.time.LocalDate
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AvailabilityCalendar(
    bookedDates: Set<LocalDate>,
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {}
    ){

    var month by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = month.lengthOfMonth()
    val space = month.atDay(1).dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(
                Color(0xFFD3D3D3),
                RoundedCornerShape(16.dp))
            .padding(12.dp)
    ){

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(
                text = "Availability",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp)

            Spacer(Modifier.width(8.dp))

            Box(
                Modifier
                    .size(8.dp)
                    .background(
                        Color(0xFF96B38E),
                        RoundedCornerShape(50))
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = "Booked",
                fontSize = 11.sp
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "<",
                fontSize = 25.sp,
                modifier = Modifier
                    .clickable { month = month.minusMonths(1) }
                    .padding(horizontal = 8.dp))

            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ">",
                fontSize = 25.sp,
                modifier = Modifier
                    .clickable { month = month.plusMonths(1) }
                    .padding(horizontal = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ){
            for (day in "SMTWTFS") {
                Text(
                    day.toString(),
                    Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        for (week in 0..5) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                for (column in 0..6) {
                    val day = week * 7 + column - space + 1

                    if (day !in 1..daysInMonth) {

                        Spacer(Modifier.weight(1f))

                    } else {

                        val date = month.atDay(day)
                        val booked = date in bookedDates
                        val selected = date == selectedDate

                        Box(
                            Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(2.dp)
                                .background(
                                    if (selected)
                                        Color(0xFF06402B)

                                    else if (booked)
                                        Color(0xFF96B38E)

                                    else
                                        Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !booked)
                                {
                                    onDateSelected(date)
                                },

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "$day",

                                color = if (selected)
                                    Color.White

                                else if (booked)
                                    Color(0xFF4A4A4A)

                                else
                                    Color(0xFF4A4A4A)
                            )
                        }
                    }
                }
            }
        }
    }
}