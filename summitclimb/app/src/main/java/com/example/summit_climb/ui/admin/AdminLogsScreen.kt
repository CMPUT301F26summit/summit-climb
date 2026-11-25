package com.example.summit_climb.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.summit_climb.model.Campsite
import com.example.summit_climb.model.User
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

private object AdminReviewColors {
    val headerBlue = Color(0xFFCFE7F5)
    val lightBlue = Color(0xFFE4EDF5)
    val charcoal = Color(0xFF36454F)
    val darkBlue = Color(0xFF245C86)
    val mutedBlue = Color(0xFF597A94)
    val divider = Color(0xFFDCE8F1)
    val subtleText = Color(0xFF9AA8B5)
    val white = Color.White
}

/**
 * Type of Image
 */
enum class AdminImageType {
    CAMPSITE,
    PROFILE
}

/**
 * Image data
 */
data class AdminImageItem(
    val id: String,
    val imageUrl: String,
    val name: String,
    val subtitle: String? = null,
    val type: AdminImageType
)

/**
 * Admin page for reviewing images, notifications, and messages.
 */
@Composable
fun AdminLogsScreen(
    campsites: List<Campsite>,
    users: List<User>
) {
    var selectedTab by remember { mutableStateOf("Images") }

    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Images", "Notifications", "Messages")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // main header
        Text(
            text = "Admin View",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = AdminReviewColors.charcoal
        )

        Text(
            text = "Review",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            color = AdminReviewColors.charcoal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // main tabs
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab)
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(tab)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search by user or campsites")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminReviewColors.darkBlue,
                unfocusedBorderColor = AdminReviewColors.divider,
                focusedTextColor = AdminReviewColors.charcoal,
                unfocusedTextColor = AdminReviewColors.charcoal
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            "Images" -> {
                AdminImagesContent(
                    campsites = campsites,
                    users = users,
                    searchQuery = searchQuery
                )
            }

            "Notifications" -> {
                Text("Notifications logs will appear here.")
            }

            "Messages" -> {
                Text("Message logs will appear here.")
            }
        }
    }
}

/**
 * Image Tab for viewing campsite and profile pictures
 */
@Composable
fun AdminImagesContent(
    campsites: List<Campsite>,
    users: List<User>,
    searchQuery: String
) {
    var selectedFilter by remember {
        mutableStateOf("All")
    }

    val campsiteImages = campsites.mapNotNull { campsite ->
        if (campsite.promoImageUrl.isBlank()) {
            null
        } else {
            val owner = users.find {
                it.userId == campsite.ownerId
            }

            AdminImageItem(
                id = campsite.id,
                imageUrl = campsite.promoImageUrl,
                name = campsite.name,
                subtitle = "Host: ${owner?.name ?: "Unknown"}",
                type = AdminImageType.CAMPSITE
            )
        }
    }

    val profileImages = users.mapNotNull { user ->
        if (user.profileImageUrl.isBlank()) {
            null
        } else {
            AdminImageItem(
                id = user.userId,
                imageUrl = user.profileImageUrl,
                name = user.name,
                type = AdminImageType.PROFILE
            )
        }
    }

    val allImages = campsiteImages + profileImages

    val query = searchQuery.trim()

    val filteredImages = when (selectedFilter) {
        "Campsites" -> campsiteImages
        "Profiles" -> profileImages
        else -> allImages
    }.filter { item ->
        item.name.contains(query, ignoreCase = true) ||
                (item.subtitle?.contains(query, ignoreCase = true) == true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Campsites", "Profiles").forEach { filter ->

                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        selectedFilter = filter
                    },
                    label = {
                        Text(filter)
                    }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredImages,
                key = { "${it.type}_${it.id}" }
            ) { item ->

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF36454F)
                    )

                    // campsite images text
                    item.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
