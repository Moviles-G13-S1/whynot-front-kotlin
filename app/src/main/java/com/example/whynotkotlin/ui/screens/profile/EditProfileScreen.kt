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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.whynotkotlin.features.profile.application.ProfileUiState
import com.example.whynotkotlin.features.profile.domain.Genders
import com.example.whynotkotlin.ui.components.WhyNotDropdownField
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
    state: ProfileUiState,
    onBackClick: () -> Unit,
    onSaveChangesClick: (String, String, String, String) -> Unit,
    onChangePasswordClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val profile = state.profile
    if (profile == null) {
        Column(Modifier.padding(24.dp)) {
            Text(state.errorMessage ?: if (state.loading) "Loading profile…" else "Profile unavailable")
            WhyNotButton(text = "Back", onClick = onBackClick)
        }
        return
    }
    var name by rememberSaveable(profile.uid) { mutableStateOf(profile.name) }
    var gender by rememberSaveable(profile.uid) { mutableStateOf(profile.gender) }
    var age by rememberSaveable(profile.uid) { mutableStateOf(profile.age.toString()) }
    var category by rememberSaveable(profile.uid) { mutableStateOf(profile.preferredCategoryId) }
    val changed = name != profile.name || gender != profile.gender ||
        age != profile.age.toString() || category != profile.preferredCategoryId

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
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

                Text("Email", style = MaterialTheme.typography.titleMedium)
                Text(profile.email, color = WhyNotGray)

                Spacer(modifier = Modifier.height(10.dp))

                WhyNotDropdownField("Gender", gender, Genders.all.map { it to it }, { gender = it }, enabled = !state.saving)

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

                    WhyNotDropdownField(
                        selectedValue = category,
                        options = state.categories.map { it.id to it.name },
                        placeholder = state.preferredCategoryName,
                        enabled = !state.saving,
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
                text = if (state.saving) "Saving…" else "Save changes",
                onClick = { onSaveChangesClick(name, gender, age, category) },
                enabled = changed && !state.saving
            )

            Text(
                text = state.errorMessage ?: if (changed) "" else "No changes to save",
                style = MaterialTheme.typography.labelSmall,
                color = WhyNotGray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }

        WhyNotBottomBar(
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}