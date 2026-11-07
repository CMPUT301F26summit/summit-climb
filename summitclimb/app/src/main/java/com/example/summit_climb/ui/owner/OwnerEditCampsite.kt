package com.example.summit_climb.ui.owner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.OutlinedButton
import com.example.summit_climb.service.ImageService

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.WaterDrop
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.lightRed
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue

import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.summit_climb.model.Campsite


/** Holds the amenities for editing */
data class AmenityOption(val key: String, val label: String, val icon: ImageVector)

val AllAmenities = listOf(
    AmenityOption("Parking", "Parking", Icons.Default.LocalParking),
    AmenityOption("Washrooms", "Washrooms", Icons.Default.Wc),
    AmenityOption("Fire", "Fire", Icons.Default.LocalFireDepartment),
    AmenityOption("Trails", "Trails", Icons.Default.Hiking),
    AmenityOption("Picnic tables", "Picnic Tables", Icons.Default.TableRestaurant),
    AmenityOption("Water Tap", "Water Tap", Icons.Default.WaterDrop)
)

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
            .testTag("Amenity${amenity.key}"),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Icon(amenity.icon, null,
            Modifier.size(24.dp),
            tint = charcoal
        )

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
 * Owner Edit Campsite Screen
 *
 * US 02.01.03
 *
 * Displays an editing page for campsite listing clicked
 * allows for editing:
 * campsite name, location, photo, price per night, occupancy, description, amenities, map location, tags, and rules/policies
 * Includes a cancel button for discarding changes and a delete button
 *
 * Outstanding: the map shows coordinates until the shared map component is ready.
 *
 * @param navController navigation from campsite details screen
 * @param modifier modifier applied to campsite editing screen
 * @param campsite campsite model
 * @param imageService imaging service that handles images to url
 * @param saveCampsite saving campsite details after editing
 * @param deleteCampsite deleting campsite instead of editing listing
 */
@Composable
fun OwnerEditCampsite(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsite: Campsite,
    imageService: ImageService = ImageService(),
    saveCampsite: (Campsite) -> Unit = {},
    deleteCampsite: (Campsite) -> Unit = {}
){
    val backButton = { navController.popBackStack(); Unit }

    // copy of fields for saved changes
    var name by remember(campsite.id) { mutableStateOf(campsite.name) }
    var location by remember(campsite.id) { mutableStateOf(campsite.location) }
    var price by remember(campsite.id) { mutableStateOf(campsite.price.toInt().toString()) }
    var occupancy by remember(campsite.id) { mutableIntStateOf(campsite.maxOccupancy) }
    var description by remember(campsite.id) { mutableStateOf(campsite.description) }
    var amenities by remember(campsite.id) { mutableStateOf(campsite.amenities.toSet()) }
    var tags by remember(campsite.id) { mutableStateOf(campsite.tags) }
    var rules by remember(campsite.id) { mutableStateOf(campsite.rules) }
    var addingTag by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }

    var image by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }
    val newImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> image = uri }

    var deleteCampsite by remember { mutableStateOf(false) }

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
                    text = "Edit Campsite",
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
                        .border(2.dp,
                            lightRed, CircleShape)
                        .padding(4.dp)
                        .clickable(onClick = backButton)
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(22.dp))
            ){
                AsyncImage(
                    model = image ?: campsite.promoImageUrl,
                    contentDescription = campsite.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(greyBlue)
                )

                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .clickable { newImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(Icons.Default.PhotoCamera, null,
                        Modifier.size(16.dp),
                        tint = charcoal
                    )
                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "Change photo",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campsiteName"),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campLocation"),
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
                        onValueChange = { price = it.filter { c -> c.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pricePerNight"),
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
                            .border(1.dp,
                                greyBlue,
                                RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Remove, "Less people",
                            Modifier
                                .size(36.dp)
                                .background(greyBlue, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (occupancy > 1)
                                        occupancy --
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("descriptionInput"),
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
                color = charcoal,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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
                                if (selected)
                                    amenities - icon.key
                                else
                                    amenities + icon.key
                        }
                    }
                }
            }

            Text(
                text = "Map location",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(greyBlue)
            ){
                Icon(
                    Icons.Default.LocationOn,"Pin",
                    Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    tint = charcoal
                )

                Text(
                    text = "Move pin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .clickable { /* map repo*/ }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
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
                    .background(greyBlue, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = tag,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = charcoal)

                Spacer(Modifier.width(6.dp))

                Icon(
                    Icons.Default.Close,
                    "Remove $tag",
                    Modifier
                        .size(14.dp)
                        .clickable { tags = tags - tag },
                    tint = charcoal
                )
            }
        }

                Text(
                    "+ Add tag",
                    fontSize = 13.sp,
                    color = charcoal,
                    modifier = Modifier
                        .border(1.dp,
                            charcoal,
                            RoundedCornerShape(20.dp)
                        )
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
                ){
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("addingTag"),
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
                    })

                    { Text(
                        text = "Add",
                        color = charcoal
                    )}
                }
            }

            Text(
                text = "Rules & Policies",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = rules,
                onValueChange = { rules = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("rulesAndPolicies"),
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
                text = "Guests must agree to these before confirming a reservation.",
                fontSize = 12.sp,
                color = charcoal,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val updated = campsite.copy(
                        name = name.trim(),
                        location = location.trim(),
                        price = price.toDouble(),
                        maxOccupancy = occupancy,
                        description = description,
                        amenities = amenities.toList(),
                        tags = tags,
                        rules = rules
                    )

                    val pickedImage = image

                    if (pickedImage == null) {
                        saveCampsite(updated)
                        navController.popBackStack()
                    }

                    else {
                        saving = true
                        imageService.upload(pickedImage, "campsites") { url ->
                            saveCampsite(
                                if (url != null)
                                    updated.copy(promoImageUrl = url)
                                else
                                    updated
                            )
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("saveButton"),
                shape = CircleShape,
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = charcoal,
                    contentColor = Color.White
                )
            ){
                Text(
                    text = "Save changes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { deleteCampsite = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("deleteCampsite"),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = lightRed)
            ){
                Text(
                    text = "Delete Campsite",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (deleteCampsite) {
                Column(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(greyBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Delete this campsite?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                    Text(
                        text = "This will permanently delete ${campsite.name}. Are you sure?",
                        fontSize = 13.sp,
                        color = charcoal,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Button(
                            onClick = {
                                deleteCampsite = false
                                deleteCampsite(campsite)
                                navController.popBackStack("ownerCampsites", inclusive = false)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("doDeleteButton"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = lightRed, contentColor = Color.White)
                        ){
                            Text("Delete", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { deleteCampsite = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("undoDeleteButton"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = charcoal, contentColor = Color.White)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}