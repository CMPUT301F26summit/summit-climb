
package com.example.summit_climb.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.summit_climb.R

private val PageBackground = Color(0xFFF2F7FB)

@Composable
fun CleanScreenLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Image(
            painter = painterResource(
                R.drawable.screens_background
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp)
                .background(
                    color = PageBackground,
                    shape = RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
        ) {
            content()
        }
    }
}
