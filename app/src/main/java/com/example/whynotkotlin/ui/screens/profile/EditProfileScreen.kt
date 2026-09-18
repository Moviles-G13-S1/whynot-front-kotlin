package com.example.whynotkotlin.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotTextField
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveChangesClick: () -> Unit,
    onChangePasswordClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("Juliana Durán") }
    var email by remember { mutableStateOf("j.duranl@uniandes.edu.co") }
    var gender by remember { mutableStateOf("Female") }
    var age by remember { mutableStateOf("22") }
    var category by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "‹ Profile",
                style = MaterialTheme.typography.bodyMedium,
                color = WhyNotGray,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(WhyNotBeige, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Change photo",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 14.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotBeige,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(14.dp)
            ) {
                WhyNotTextField(name, { name = it }, "Name")

                Spacer(modifier = Modifier.height(10.dp))

                WhyNotTextField(email, { email = it }, "Email")

                Spacer(modifier = Modifier.height(10.dp))

                WhyNotTextField(gender, { gender = it }, "Gender")

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WhyNotTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = "Age",
                        modifier = Modifier.weight(0.42f)
                    )

                    Spacer(modifier = Modifier.size(14.dp))

                    WhyNotTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Preferred Category",
                        modifier = Modifier.weight(0.58f)
                    )
                }

                Text(
                    text = "Change password                                      ›",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 4.dp)
                        .clickable { onChangePasswordClick() }
                )
            }

            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray,
                modifier = Modifier
                    .padding(top = 20.dp, start = 10.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WhyNotButton(
                text = "Save changes",
                onClick = onSaveChangesClick,
                enabled = false
            )

            Text(
                text = "No changes to save",
                style = MaterialTheme.typography.labelSmall,
                color = WhyNotGray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }

        WhyNotBottomBar()
    }
}