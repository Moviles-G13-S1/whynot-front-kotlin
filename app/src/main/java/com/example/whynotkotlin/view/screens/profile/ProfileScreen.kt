package com.example.whynotkotlin.view.screens.profile

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.R
import com.example.whynotkotlin.model.UserProfile
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotPillButton
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    preferredCategoryName: String,
    cityName: String,
    loading: Boolean,
    errorMessage: String?,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,

    /*
     * Admin access.
     *
     * The button is only displayed when Firebase Authentication
     * confirms that the current ID token contains admin=true.
     */
    isAdmin: Boolean = false,
    onAdminClick: () -> Unit = {},

    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                profile != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 39.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFEDE4D7), CircleShape)
                    )

                    Spacer(modifier = Modifier.height(31.dp))

                    Text(
                        text = profile.name.ifEmpty { "User" },
                        style = whyNotSerif(30)
                    )

                    Spacer(modifier = Modifier.height(45.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(WhyNotCream)
                            .padding(horizontal = 27.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        ProfileValue("Email", profile.email)
                        ProfileValue("Gender", profile.gender)
                        ProfileValue("Age", profile.age.toString())
                        ProfileValue("City", cityName)
                        ProfileValue("Password", "********", onClick = onChangePasswordClick)
                        ProfileValue("Preferred Category", preferredCategoryName.ifEmpty { "Not selected" })
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onEditProfileClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_profile),
                            contentDescription = null,
                            tint = WhyNotGray,
                            modifier = Modifier.size(13.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(text = "Edit Profile", style = whyNotMuted(15))
                    }

                    Spacer(modifier = Modifier.height(38.dp))

                    // Only admins see this entry; it is not part of the iOS app.
                    if (isAdmin) {
                        WhyNotPillButton(
                            text = "Admin panel",
                            onClick = onAdminClick
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    WhyNotPillButton(
                        text = "Log Out",
                        onClick = onLogoutClick
                    )
                }

                else -> Text(
                    text = errorMessage?.let { "Could not load profile." } ?: "Profile not found.",
                    style = whyNotMuted(14),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
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

/** Read-only label and value shown on the profile card. */
@Composable
private fun ProfileValue(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Text(text = label, style = whyNotSerif(20))

        Spacer(modifier = Modifier.height(9.dp))

        Text(text = value, style = whyNotMuted(15))
    }
}
