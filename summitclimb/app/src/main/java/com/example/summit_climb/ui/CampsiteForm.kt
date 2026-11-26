package com.example.summit_climb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summit_climb.ui.charcoal
import com.example.summit_climb.ui.greyBlue
import com.example.summit_climb.ui.lightBlue
import com.example.summit_climb.ui.owner.AllAmenities
import com.example.summit_climb.ui.owner.AmenityOption

/**
 * Shared input fields for OwnerRegisterCampsite and OwnerEditCampsite.
 *
 * This component only displays/updates field values. Each parent screen keeps
 * its own image upload, save/delete buttons, validation, and navigation.
 * It intentionally uses the existing amenities list and existing color palette.
 */
@Composable
fun CampsiteForm(
    name: String,
    onNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    occupancy: Int,
    onOccupancyChange: (Int) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    amenities: Set<String>,
    onAmenitiesChange: (Set<String>) -> Unit,
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    rules: String,
    onRulesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var addingTag by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = charcoal,
        unfocusedTextColor = charcoal,
        cursorColor = charcoal,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = charcoal,
        unfocusedBorderColor = greyBlue
    )

    Column(modifier = modifier.fillMaxWidth()) {
        FormLabel("Campsite name")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth().testTag("campsiteName"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        FormLabel("Location")
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier.fillMaxWidth().testTag("campLocation"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                FormLabel("Price / night")
                OutlinedTextField(
                    value = price,
                    onValueChange = { text -> onPriceChange(text.filter(Char::isDigit)) },
                    modifier = Modifier.fillMaxWidth().testTag("pricePerNight"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors
                )
            }
            Column(Modifier.weight(1f)) {
                FormLabel("Max occupancy")
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .border(1.dp, greyBlue, RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Less people",
                        modifier = Modifier.size(36.dp)
                            .background(greyBlue, RoundedCornerShape(10.dp))
                            .clickable { if (occupancy > 1) onOccupancyChange(occupancy - 1) }
                            .padding(8.dp),
                        tint = charcoal
                    )
                    Text(occupancy.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "More people",
                        modifier = Modifier.size(36.dp)
                            .background(lightBlue, RoundedCornerShape(10.dp))
                            .clickable { onOccupancyChange(occupancy + 1) }
                            .padding(8.dp),
                        tint = charcoal
                    )
                }
            }
        }

        FormLabel("Description")
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth().height(130.dp).testTag("descriptionInput"),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        FormLabel("Amenities · ${amenities.size} selected")
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(greyBlue)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllAmenities.forEach { option ->
                AmenityChoice(
                    amenity = option,
                    selected = option.key in amenities,
                    onClick = {
                        onAmenitiesChange(
                            if (option.key in amenities) amenities - option.key
                            else amenities + option.key
                        )
                    }
                )
            }
        }

        FormLabel("Tags")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { tag ->
                Row(
                    modifier = Modifier.background(greyBlue, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tag, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = charcoal)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove tag $tag",
                        modifier = Modifier.size(14.dp).clickable {
                            onTagsChange(tags - tag)
                        },
                        tint = charcoal
                    )
                }
            }
            Text(
                "+ Add tag",
                fontSize = 13.sp,
                color = charcoal,
                modifier = Modifier.border(1.dp, charcoal, RoundedCornerShape(20.dp))
                    .clickable { addingTag = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        if (addingTag) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.weight(1f).testTag("newTag"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
                TextButton(onClick = {
                    val tag = newTag.trim().lowercase()
                    if (tag.isNotEmpty() && tag !in tags) onTagsChange(tags + tag)
                    newTag = ""
                    addingTag = false
                }) { Text("Add", color = charcoal) }
            }
        }

        FormLabel("Rules & policies")
        OutlinedTextField(
            value = rules,
            onValueChange = onRulesChange,
            modifier = Modifier.fillMaxWidth().height(130.dp).testTag("rulesAndPolicies"),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )
        Text(
            "Guests must agree to these before confirming a reservation.",
            fontSize = 12.sp,
            color = charcoal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FormLabel(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun AmenityChoice(
    amenity: AmenityOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(86.dp).height(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) lightBlue else Color.White)
            .border(1.dp, if (selected) charcoal else greyBlue, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("amenity_${amenity.key}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(amenity.icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = charcoal)
        Spacer(Modifier.height(4.dp))
        Text(amenity.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = charcoal)
    }
}
