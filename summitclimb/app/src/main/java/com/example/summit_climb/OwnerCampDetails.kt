package com.example.summit_climb

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import java.time.LocalDate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
@Composable
fun OwnerCampDetails(navController: NavController) {
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    // Sample owner details screen (don't worry about this, this is for testing)

    // Sample data
    val booked = setOf(
        LocalDate.of(2026, 11, 5),
        LocalDate.of(2026, 11, 6),
        LocalDate.of(2026, 11, 20)
    )

    Column(Modifier.fillMaxSize()) {

        Spacer(Modifier.weight(1f))

        AvailabilityCalendar(
            bookedDates = booked,
            selectedDate = selected,
            onDateSelected = { selected = it }
        )
    }
}