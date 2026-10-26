package com.example.whynotkotlin.view.screens.profile

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
import com.example.whynotkotlin.model.UserProfile
import com.example.whynotkotlin.model.Category
import com.example.whynotkotlin.model.Genders
import com.example.whynotkotlin.view.components.WhyNotDropdownField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotGray

@Composable
fun EditProfileScreen(
    profile: UserProfile?,
    categories: List<Category>,
    preferredCategoryName: String,
    loading: Boolean,
    saving: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onSaveChangesClick: (String, String, String, String) -> Unit,
    onChangePasswordClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isOnline: Boolean = true
) {
    if (profile == null) {
        Column(Modifier.padding(24.dp)) {
            Text(errorMessage ?: if (loading) "Loading profile…" else "Profile unavailable")
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

            OfflineBanner(
                isOnline = isOnline,
                message = "You are offline. Your changes are kept and can be saved once you reconnect.",
                modifier = Modifier.padding(bottom = 12.dp)
            )

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
                WhyNotTextField(name, { if (!saving) name = it }, "Name")

                Spacer(modifier = Modifier.height(10.dp))

                Text("Email", style = MaterialTheme.typography.titleMedium)
                Text(profile.email, color = WhyNotGray)

                Spacer(modifier = Modifier.height(10.dp))

                WhyNotDropdownField("Gender", gender, Genders.all.map { it to it }, { gender = it }, enabled = !saving)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WhyNotTextField(
                        value = age,
                        onValueChange = { if (!saving) age = it },
                        label = "Age",
                        modifier = Modifier.weight(0.42f)
                    )

                    Spacer(modifier = Modifier.size(14.dp))

                    WhyNotDropdownField(
                        selectedValue = category,
                        options = categories.map { it.id to it.name },
                        placeholder = preferredCategoryName,
                        enabled = !saving,
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
                text = if (saving) "Saving…" else "Save changes",
                onClick = { onSaveChangesClick(name, gender, age, category) },
                enabled = changed && !saving
            )

            Text(
                text = errorMessage ?: if (changed) "" else "No changes to save",
                style = MaterialTheme.typography.labelSmall,
                color = WhyNotGray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.PROFILE,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}