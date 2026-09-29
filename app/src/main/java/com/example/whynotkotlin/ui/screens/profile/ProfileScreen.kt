package com.example.whynotkotlin.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun ProfileScreen(
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = 24.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(82.dp)
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        WhyNotBeige,
                        CircleShape
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = "Juliana Durán",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge
            )

            Spacer(
                modifier =
                    Modifier.height(36.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotBeige,
                        RoundedCornerShape(
                            24.dp
                        )
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 22.dp
                    )
            ) {

                ProfileInfo(
                    "Email",
                    "j.duranl@uniandes.edu.co"
                )

                ProfileInfo(
                    "Gender",
                    "Female"
                )

                ProfileInfo(
                    "Age",
                    "22"
                )

                ProfileInfo(
                    "Password",
                    "********"
                )

                ProfileInfo(
                    "Preferred Category",
                    "Beauty"
                )
            }

            Text(
                text = "✎ Edit Profile",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color = WhyNotGray,
                modifier = Modifier
                    .align(
                        Alignment.End
                    )
                    .padding(
                        top = 12.dp,
                        end = 10.dp
                    )
                    .clickable {
                        onEditProfileClick()
                    }
            )

            /*
             * Permanent Admin entry point.
             *
             * Normal users never see this button.
             */
            if (isAdmin) {

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                WhyNotButton(
                    text = "Admin panel",
                    onClick = onAdminClick,
                    filled = true
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            WhyNotButton(
                text = "Sign out",
                onClick = onLogoutClick
            )
        }

        WhyNotBottomBar(
            onHomeClick = onHomeClick,
            onWishlistsClick =
                onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick =
                onPurchasesClick,
            onProfileClick =
                onProfileClick
        )
    }
}

@Composable
private fun ProfileInfo(
    label: String,
    value: String
) {
    Column(
        modifier =
            Modifier.padding(
                vertical = 7.dp
            )
    ) {

        Text(
            text = label,
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(
            text = value,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            color = WhyNotGray
        )
    }
}