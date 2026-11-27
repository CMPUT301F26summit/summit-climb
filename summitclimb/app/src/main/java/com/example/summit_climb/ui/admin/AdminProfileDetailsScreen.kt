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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.summit_climb.ui.ConfirmDialog
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Security

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.summit_climb.model.AdminProfileView

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.darkGreen
import com.example.summit_climb.ui.lightGreen
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.lightBlue


/**
 * One booking shown in the admin's reservations panel.
 *
 * @param id: reservation id.
 * @param campsiteName: name of the booked campsite.
 * @param details: check-in and length of stay
 * @param active: set true if the booking is active.
 */
data class AdminBookingView(
    val id: String,
    val campsiteName: String,
    val details: String,
    val active: Boolean
)

private enum class Menu { NONE, ACCOUNT_STATUS, RESERVATIONS }

/**
 * Admin view of a single profile.
 * Account Status and reservations open a menu below
 * Suspend Account opens a confirmation dialog.
 *
 * @param navController: navigation for the back button.
 * @param profile: the profile being viewed.
 * @param phone: the user's phone number.
 * @param reservations: the user's Reservations.
 * @param onListingsClick: when the admin taps Listings.
 * @param onStatusChange:  new status state (deactivate / re-activate).
 * @param onRemoveOwner: called when the admin revokes campsite ownership.
 * @param onReservStatusChange: called with a booking and its new active state.
 * @param onSuspend: called when the admin confirms suspending the account.
 */
@Composable
fun AdminProfileDetailsScreen(
    navController: NavController,
    profile: AdminProfileView,
    phone: String = "(xxx) xxx - xxxx",
    reservations: List<AdminBookingView> = emptyList(),
    onListingsClick: () -> Unit = {},
    onStatusChange: (Boolean) -> Unit = {},
    onRemoveOwner: () -> Unit = {},
    onReservStatusChange: (AdminBookingView, Boolean) -> Unit = { _, _ -> },
    onSuspend: () -> Unit = {}
) {
    var menu by remember { mutableStateOf(Menu.NONE) }
    var showSuspend by remember { mutableStateOf(false) }

    fun menuClicked(target: Menu) {
        menu =
            if (menu == target)
                Menu.NONE
            else
                target
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ){
            Text(
                text = "Admin View",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = charcoal
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "← Back",
                    color = Color.Black,
                    fontSize = 12.sp
                )

            }

            Spacer(Modifier.height(16.dp))

            ProfileHeader(profile)

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))
            ){
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "Personal Information",
                        fontSize = 14.sp,
                        color = charcoal
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text =
                            if (profile.name == "")
                                "Unnamed user"
                            else profile.name,
                        fontSize = 13.sp,
                        color = charcoal
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(text =
                        if (profile.email == "")
                            "No email entered"
                        else
                            profile.email,
                        fontSize = 12.sp,
                        color = charcoal
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = phone,
                        fontSize = 12.sp,
                        color = charcoal)
                    Spacer(Modifier.height(24.dp))

                    TextBubble("Account Status") { menuClicked(Menu.ACCOUNT_STATUS) }
                    TextBubble("Reservations (${profile.reservations})") { menuClicked(Menu.RESERVATIONS) }
                    TextBubble("Listings (${profile.listings})", onListingsClick)
                }
            }

            Spacer(Modifier.height(12.dp))

            when (menu) {
                Menu.ACCOUNT_STATUS -> AccountStatusMenu(
                    active = profile.activeProfile,
                    onClose = { menu = Menu.NONE },
                    onStatusChange = { menu = Menu.NONE; onStatusChange(it) },
                    onRemoveOwner = { menu = Menu.NONE; onRemoveOwner() }
                )
                Menu.RESERVATIONS -> ReservationsMenu(
                    reservations = reservations,
                    onClose = { menu = Menu.NONE },
                    onReservStatusChange = onReservStatusChange
                )
                Menu.NONE -> {}
            }

            Spacer(Modifier.height(16.dp))
        }

        OutlinedButton(
            onClick = { showSuspend = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(48.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFFB0AEAE)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFD2D0D0),
                contentColor = Color(0xFF7A1F1F)
            )
        ) {
            Text("Suspend Account", fontSize = 12.sp)
        }
    }

    if (showSuspend) {
        SuspendDialog(
            profile = profile,
            onCancel = { showSuspend = false },
            onConfirm = { showSuspend = false; onSuspend() }
        )
    }
}

/**
 * Admin view of a single profile.
 *
 * @param profile: the profile being viewed.
 */
@Composable
private fun ProfileHeader(profile: AdminProfileView) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (profile.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = profile.profileImageUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Default Profile",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = darkGreen
            )
            if (profile.flagged) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Flag,
                    contentDescription = "Flagged",
                    tint = darkGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        if (profile.activeProfile) {
            Border("Active", lightGreen, darkGreen)
        } else {
            Border("Deactivated", lightRed, Color.Red)
        }
        if (profile.flagged) {
            Text(
                "This member has been flagged",
                fontSize = 10.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Border shown in profile header
 *
 * @param text: text in border
 * @param background: background color
 * @param textColor: text color
 */
@Composable
private fun Border(
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
            text = text,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

/**
 * Border shown in profile header
 *
 * @param text: text in border
 * @param onClick: when the border is clicked
 */

@Composable
private fun TextBubble(
    text: String,
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .height(48.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = lightBlue,
            contentColor = Color.White
        )
    ){
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Title and close button at the top of a menu.
 *
 * @param title: text shown on the left.
 * @param onClose: when the close button (x) is tapped.
 */
@Composable
private fun MenuRow(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = charcoal)
        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Menu opened by the Account Status button.
 * An active profile shows Deactivate and Remove campsite owner,
 * and a deactivated profile shows Re-activate.
 *
 * @param active true if the profile is active, false otherwise
 * @param onClose set when the close button is tapped.
 * @param onStatusChange set with false to deactivate or true to re-activate the profile.
 * @param onRemoveOwner set when the admin revokes the user's campsite ownership.
 */
@Composable
private fun AccountStatusMenu(
    active: Boolean,
    onClose: () -> Unit,
    onStatusChange: (Boolean) -> Unit,
    onRemoveOwner: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            MenuRow("Account Status", onClose)

            Text(
                text = "ADMIN ACTIONS",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )
            if (active) {
                ActionRow(
                    icon = Icons.Default.PersonOff,
                    title = "Deactivate profile",
                    description = "Deactivate this user's account.",
                    container = Color(0xFFFFC1C3),
                    accent = Color.Red,
                    onClick = { onStatusChange(false) }
                )
                Spacer(Modifier.height(8.dp))
                ActionRow(
                    icon = Icons.Default.Security,
                    title = "Remove campsite owner",
                    description = "Revoke ownership for policy violations.",
                    container = Color(0xFFFFC1C3),
                    accent = Color.Red,
                    onClick = onRemoveOwner
                )
            } else {
                ActionRow(
                    icon = Icons.Default.Person,
                    title = "Re-activate profile",
                    description = "Activate this user account.",
                    container = Color(0xFFE9F6E6),
                    accent = darkGreen,
                    onClick = { onStatusChange(true) }
                )
            }
        }
    }
}

/**
 * Clickable admin action with an icon, a title and a short description.
 *
 * @param icon icon shown in the circle on the left.
 * @param title bold action name.
 * @param description one-line explanation under the title.
 * @param container background color of the row.
 * @param accent color used for the border, icon, circle tint and text.
 * @param onClick called when the row is tapped.
 */
@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    container: Color,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(container)
            .border(BorderStroke(1.dp, accent), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = accent
            )
        }
    }
}

/**
 * Menu opened by the Reservation button.
 * Lists each of the user's Reservations with its status. Tapping a status
 * opens a small menu to activate or deactivate that booking.
 *
 * @param reservations the user's reservations to list.
 * @param onClose called when the close button is tapped.
 * @param onReservStatusChange called with a booking and its new active state.
 */
@Composable
private fun ReservationsMenu(
    reservations: List<AdminBookingView>,
    onClose: () -> Unit,
    onReservStatusChange: (AdminBookingView, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            MenuRow("Reservations", onClose)

            if (reservations.isEmpty()) {
                Text(
                    text = "No Reservations",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            for (booking in reservations) {
                var menuOpen by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(booking.campsiteName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(booking.details, fontSize = 11.sp, color = Color.Gray)
                    }
                    Box {
                        Box(modifier = Modifier.clickable { menuOpen = true }) {
                            if (booking.active)
                                Border("Active", lightGreen, darkGreen)
                            else
                                Border("Inactive", Color(0xFFFFC1C3), Color.Red)
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Activate") },
                                onClick = { menuOpen = false; onReservStatusChange(booking, true) }
                            )
                            DropdownMenuItem(
                                text = { Text("Deactivate") },
                                onClick = { menuOpen = false; onReservStatusChange(booking, false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Confirm dialog shown when the admin taps Suspend Account.
 * Asks whether to suspend the profile indefinitely.
 *
 * @param profile the profile to be suspended
 * @param onCancel when the admin taps Cancel or dismisses the dialog.
 * @param onConfirm when the admin taps Suspend.
 */
@Composable
private fun SuspendDialog(
    profile: AdminProfileView,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = "Suspend Account?",
        message = "Would you like to suspend this account indefinitely?",
        confirmText = "Suspend",
        cancelText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onCancel
    )
}