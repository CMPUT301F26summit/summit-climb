package com.example.summit_climb.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.darkGreen
import com.example.summit_climb.ui.noLotteryGrey

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Flag

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import com.example.summit_climb.controller.AdminController
import com.example.summit_climb.model.AdminProfileView
import com.example.summit_climb.ui.lightGreen

private enum class StatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DEACTIVATED("Deactivated")
}

/**
 * The admin's list of all user profiles, active and deactivated.
 * An admin can search by name or email and filter by status,
 * show only flagged profiles, and use the menu on each card to open, flag or deactivate a profile.
 *
 * @param navController: navigation for the back button.
 * @param profiles: profiles on firebase
 * @param profileClicked: called when the admin opens a profile from the card menu.
 * @param onStatusChange: called with the profile and its new active state when an admin deactivates or reactivates it.
 * @param onFlagChange: called with the profile and its new flagged state when an admin flags or unflags it.
 */
@Composable
fun AdminProfilesScreen(
    navController: NavController,
    profiles: List<AdminProfileView>,
    errorMessage: String? = null,
    profileClicked: (AdminProfileView) -> Unit = {},
    onStatusChange: (AdminProfileView, Boolean) -> Unit = { _, _ -> },
    onFlagChange: (AdminProfileView, Boolean) -> Unit = { _, _ -> }
) {
    var searchText by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var flaggedProfiles by remember { mutableStateOf(false) }

    val visible = AdminController.filterProfiles(
        profiles = profiles,
        searchText = searchText,
        active = when (statusFilter) {
            StatusFilter.ALL -> null
            StatusFilter.ACTIVE -> true
            StatusFilter.DEACTIVATED -> false
        },
        flaggedProfiles = flaggedProfiles
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ){
        Text(
            text = "Admin View",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = charcoal
        )

        TextButton(onClick = { navController.popBackStack() }) {
            Text(
                text = "← Back",
                color = Color.Black
            )
        }
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text(
                    text = "Search profile by name or email",
                    color = Color.Gray
            )},
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ){
            Dropdown(
                label =
                    if (statusFilter == StatusFilter.ALL)
                        "Status"
                    else
                        statusFilter.label,
                options = listOf("All", "Active", "Deactivated"),
                onSelect = { index ->
                    statusFilter =
                        when (index) {
                            0 -> StatusFilter.ALL
                            1 -> StatusFilter.ACTIVE
                        else
                            -> StatusFilter.DEACTIVATED
                    }
                }
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (flaggedProfiles)
                            lightBlue
                        else
                            greyBlue
                    )
                    .clickable { flaggedProfiles = !flaggedProfiles }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "Flagged",
                    color =
                        if (flaggedProfiles)
                            greyBlue
                        else
                            charcoal,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${visible.size} profiles",
            fontSize = 11.sp,
            color = charcoal,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(visible, key = { it.id }) { profile ->
                ProfileRow(
                    profile = profile,
                    onOpen = { profileClicked(profile) },
                    onStatusClick= { onStatusChange(profile, !profile.activeProfile) },
                    onFlagClick = { onFlagChange(profile, !profile.flagged) }
                )
            }
        }
    }
}

/**
 * A rounded filter pill that opens a dropdown of options when tapped.
 *
 * @param label: text shown on the pill.
 * @param options: the choices shown in the dropdown.
 * @param onSelect: called with the index of the chosen option.
 */
@Composable
private fun Dropdown(
    label: String,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var dropdownClicked by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(greyBlue)
                .clickable { dropdownClicked = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = charcoal,
                fontSize = 14.sp
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = charcoal,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = dropdownClicked, onDismissRequest = { dropdownClicked = false }) {
            for (index in options.indices) {
                DropdownMenuItem(
                    text = { Text(options[index]) },
                    onClick = {
                        dropdownClicked = false
                        onSelect(index)
                    }
                )
            }
        }
    }
}

/**
 * One profile Row showing the avatar, name, email, status, and booking and listing counts.
 *
 * @param profile: the profile to show.
 * @param onOpen: called when the admin picks "View profile" from the menu.
 * @param onStatusClick: called when the admin picks Deactivate or Reactivate.
 * @param onFlagClick: called when the admin picks Flag or Remove flag.
 */
@Composable
private fun ProfileRow(
    profile: AdminProfileView,
    onOpen: () -> Unit,
    onStatusClick: () -> Unit,
    onFlagClick: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = noLotteryGrey)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = charcoal,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text =
                            if (profile.name == "")
                                "Unnamed user"
                            else
                                profile.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                    Text(
                        text =
                            if (profile.email == "")
                                "No email"
                            else
                                profile.email,
                        fontSize = 14.sp,
                        color = charcoal
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        StatusBubble(
                            text =
                                if (profile.activeProfile)
                                    "Active"
                                else
                                    "Deactivated",
                            background =
                                if (profile.activeProfile)
                                    lightGreen
                                else
                                    lightRed,
                            textColor =
                                if (profile.activeProfile)
                                    darkGreen
                                else
                                    Color.Red
                        )
                        if (profile.flagged) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Flagged",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(28.dp)
                ){
                    Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = darkGreen)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("View profile") },
                        onClick = { menuOpen = false; onOpen() }
                    )
                    DropdownMenuItem(
                        text = { Text(
                            if (profile.activeProfile)
                                "Deactivate"
                            else
                                "Reactivate") },
                        onClick = { menuOpen = false; onStatusClick() }
                    )
                    DropdownMenuItem(
                        text = { Text(
                            if (profile.flagged)
                                "Remove flag"
                            else
                                "Flag") },
                        onClick = { menuOpen = false; onFlagClick() }
                    )
                }
            }

            Text(
                text = "${profile.reservations} ${
                    if (profile.reservations == 1) 
                        "Reservation" 
                    else "Reservations"
                }" + " • ${profile.listings} ${
                    if (profile.listings == 1) 
                        "Listing" 
                    else 
                        "Listings"}",
                fontSize = 13.sp,
                color = charcoal,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

/**
 * A small rounded bubble for "Active" or "Deactivated".
 *
 * @param text: the label text.
 * @param background: colour behind the text.
 * @param textColor: colour of the text.
 */
@Composable
private fun StatusBubble(
    text: String,
    background: Color,
    textColor: Color
){
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ){
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}