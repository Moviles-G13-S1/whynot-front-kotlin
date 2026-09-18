package com.example.whynotkotlin.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotTextField
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onUpdatePasswordClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val canUpdate =
        currentPassword.isNotBlank() &&
                newPassword.length >= 8 &&
                newPassword == confirmPassword

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
                text = "‹ Edit Profile",
                style = MaterialTheme.typography.bodyMedium,
                color = WhyNotGray,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = "Change Password",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(52.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotBeige,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                WhyNotTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Current password",
                    placeholder = "Enter current password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(20.dp))

                WhyNotTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New password",
                    placeholder = "Enter new password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(20.dp))

                WhyNotTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm new password",
                    placeholder = "Confirm new password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Text(
                    text = "Use at least 8 characters.",
                    style = MaterialTheme.typography.labelSmall,
                    color = WhyNotGray,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }

            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray,
                modifier = Modifier
                    .padding(top = 24.dp, start = 10.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            WhyNotButton(
                text = "Update password",
                onClick = onUpdatePasswordClick,
                enabled = canUpdate
            )

            if (!canUpdate) {
                Text(
                    text = "Complete all fields to continue",
                    style = MaterialTheme.typography.labelSmall,
                    color = WhyNotGray,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
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