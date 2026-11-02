package com.example.summit_climb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController


@Composable
fun ProfileActivity(
    navController: NavController,
    name: String,
    email: String,
    phone: String
) {
    var nameInput by remember(name) { mutableStateOf(name) }
    var emailInput by remember(email) { mutableStateOf(email) }
    var phoneInput by remember(phone) { mutableStateOf(phone) }
    var notificationStatus by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8E8E8))
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Profile",
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF627F5C)),
                contentAlignment = Alignment.Center
            ) {}

            Spacer(Modifier.height(10.dp))

            Text(
                text = name,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Change photo",
                fontSize = 13.sp,
                color = Color(0xFF627F5C),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { /* open photo selector */ }
                    .padding(vertical = 4.dp)
            )

        }

        Spacer(Modifier.height(20.dp))

        Card(
            shape = (RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personal Information",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { /* save profile function */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF627F5C),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Save Changes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column() {
                        Text(
                            text = "Notifications",
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                    Switch(
                        checked = notificationStatus,
                        onCheckedChange = { notificationStatus = it }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* show policy */ }
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Policies & guidelines",
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { /* delete account */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB96666),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Delete Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
