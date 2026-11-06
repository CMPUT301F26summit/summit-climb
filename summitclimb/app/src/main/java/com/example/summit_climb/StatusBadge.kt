package com.example.summit_climb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Displays a reusable coloured badge showing a short status.
// Used to indicate lottery, reservation, or waitlist statuses.

/**
 * Displays a coloured badge containing a short status label.
 *
 * Used throughout the app to show statuses such as "Open" or "No Lottery".
 *
 * @param text status label displayed inside the badge
 * @param color background colour of the badge
 * @param modifier modifier applied to the badge
 */
@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
