package com.example.summit_climb.ui.owner

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.imePadding
import com.example.summit_climb.service.ImageService
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove

import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue

import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.summit_climb.model.Campsite

// Color Palette

/**
 * Amenity border for an amenity icon.
 *
 * @param amenity displaying the amenity for editing
 * @param selected for amenity selection
 * @param onClick  holds the clicked amenities
 */
@Composable
private fun AmenityBorder(
    amenity: AmenityOption,
    selected: Boolean,
    onClick: () -> Unit)
{
    Column(
        Modifier
            .width(86.dp)
            .height(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected)
                    lightBlue
                else
                    Color.White)
            .border(1.dp,
                if (selected)
                    charcoal
                else
                    greyBlue, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("amenity_${amenity.key}"),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(amenity.icon, null,
            Modifier.size(24.dp),
            tint = charcoal)

        Spacer(Modifier.height(4.dp))

        Text(
            text = amenity.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.
            SemiBold, color = charcoal
        )
    }
}

/**
 * Owner Register Campsite Screen
 *
 * US 02.01.01, US 02.01.02
 *
 * Displays a register page for new campsite listings
 * allows for adding of components:
 * campsite name, location, photo, price per night, occupancy, description, amenities, map location, tags, and rules/policies
 * Includes a cancel button for discarding changes
 *
 * Outstanding: the map shows coordinates until the shared map component is ready.
 *
 * @param navController navigation from (+) button on campsites listed screen
 * @param modifier modifier applied to register campsites screen
 * @param campsite campsite model
 * @param imageService imaging service that handles images to url
 * @param registerCampsite registering a campsite
 */
@Composable
fun OwnerRegisterCampsite(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsite: Campsite,
    imageService: ImageService = ImageService(),
    registerCampsite: (Campsite) -> Unit = {}
) {
    val backButton = { navController.popBackStack(); Unit }

    var name by remember { mutableStateOf(campsite.name) }
    var location by remember { mutableStateOf(campsite.location) }
    var price by remember(campsite.id) { mutableStateOf(campsite.price.toInt().toString()) }
    var occupancy by remember { mutableIntStateOf(maxOf(campsite.maxOccupancy, 1)) }
    var description by remember { mutableStateOf(campsite.description) }
    var amenities by remember { mutableStateOf(campsite.amenities.toSet()) }
    var tags by remember { mutableStateOf(campsite.tags) }
    var rules by remember { mutableStateOf(campsite.rules) }
    var addingTag by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }

    val newImage =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            imageUri = uri
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
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
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Register Campsite",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel button",
                    tint = lightRed,
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, lightRed, CircleShape)
                        .padding(4.dp)
                        .clickable(onClick = backButton)
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(greyBlue)
            ){
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = campsite.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .clickable {
                            newImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Default.PhotoCamera, null,
                        Modifier.size(16.dp),
                        tint = charcoal
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "Add photo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Campsite name",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().testTag("campsiteName"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        cursorColor = charcoal,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = charcoal,
                        unfocusedBorderColor = greyBlue
                    )
            )

            Text(
                text = "Location",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth().testTag("campLocation"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        cursorColor = charcoal,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = charcoal,
                        unfocusedBorderColor = greyBlue
                    )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Price / night",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() })
                                price = input
                        },
                        modifier = Modifier.fillMaxWidth().testTag("pricePerNight"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedTextColor = charcoal,
                                unfocusedTextColor = charcoal,
                                cursorColor = charcoal,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = charcoal,
                                unfocusedBorderColor = greyBlue
                            )
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Max occupancy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, greyBlue, RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.Default.Remove, "Less people",
                            Modifier
                                .size(36.dp)
                                .background(greyBlue, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (occupancy > 1)
                                        occupancy--
                                }
                                .padding(8.dp),
                            tint = charcoal
                        )

                        Text(
                            text = "$occupancy",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Icon(
                            Icons.Default.Add, "More people",
                            Modifier
                                .size(36.dp)
                                .background(lightBlue, RoundedCornerShape(10.dp))
                                .clickable { occupancy++ }
                                .padding(8.dp),
                            tint = charcoal
                        )
                    }
                }
            }

            Text(
                text = "Description",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(130.dp).testTag("descriptionInput"),
                shape = RoundedCornerShape(14.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        cursorColor = charcoal,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = charcoal,
                        unfocusedBorderColor = greyBlue
                    )
            )
            Text(
                text = "Amenities · ${amenities.size} selected",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(greyBlue)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ){
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    for (icon in AllAmenities) {
                        val selected = icon.key in amenities

                        AmenityBorder(icon, selected) {
                            amenities =
                                if (selected) amenities - icon.key
                                else
                                    amenities + icon.key
                        }
                    }
                }
            }

            Text(
                text = "Tags",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )

            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                for (tag in tags) {
                    Row(
                        Modifier
                            .background(
                                greyBlue,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = tag,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = charcoal
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close, "Remove tag $tag",
                            Modifier
                                .size(14.dp)
                                .clickable { tags = tags - tag },
                            tint = charcoal
                        )
                    }
                }
                Text(
                    text = "+ Add tag",
                    fontSize = 13.sp,
                    color = charcoal,
                    modifier = Modifier
                        .border(1.dp, charcoal, RoundedCornerShape(20.dp))
                        .clickable { addingTag = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            if (addingTag) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        modifier = Modifier.weight(1f).testTag("newTag"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedTextColor = charcoal,
                                unfocusedTextColor = charcoal,
                                cursorColor = charcoal,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = charcoal,
                                unfocusedBorderColor = greyBlue
                            )
                    )

                    TextButton(onClick = {
                        val t = newTag.trim().lowercase()
                        if (t.isNotEmpty() && t !in tags)
                            tags = tags + t

                        newTag = ""
                        addingTag = false
                    }) {
                        Text(
                            text = "Add",
                            color = charcoal
                        )
                    }
                }
            }

            Text(
                text = "Rules & policies ",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                modifier = Modifier.fillMaxWidth().height(130.dp).testTag("rulesAndPolicies"),
                shape = RoundedCornerShape(14.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        cursorColor = charcoal,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = charcoal,
                        unfocusedBorderColor = greyBlue
                    )
            )

            Text(
                "Guests must agree to these before confirming a reservation.",
                fontSize = 12.sp,
                color = charcoal,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val newCampsite = Campsite(
                        name = name.trim(),
                        location = location.trim(),
                        price = price.toDouble(),
                        maxOccupancy = occupancy,
                        description = description,
                        amenities = amenities.toList(),
                        tags = tags,
                        rules = rules
                    )

                    val pickedImage = imageUri

                    if (pickedImage == null) {
                        registerCampsite(newCampsite)
                        navController.popBackStack()
                    } else {
                        saving = true
                        imageService.upload(pickedImage, "campsites") { url ->
                            registerCampsite(
                                if (url != null)
                                    newCampsite.copy(promoImageUrl = url)
                                else
                                    newCampsite
                            )
                            navController.popBackStack()
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("registerButton"),
                shape = CircleShape,
                enabled = !saving && name.isNotBlank() && location.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = charcoal,
                    contentColor = Color.White
                )
            ){
                Text(
                    text = "Register campsite",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}