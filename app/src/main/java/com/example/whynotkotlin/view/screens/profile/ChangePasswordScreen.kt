package com.example.whynotkotlin.view.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.PasswordPolicy
import com.example.whynotkotlin.view.components.PasswordRequirements
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotGray

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onUpdatePasswordClick: (String, String, String) -> Unit,
    submitting: Boolean,
    errorMessage: String?,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isOnline: Boolean = true
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val canUpdate =
        currentPassword.isNotBlank() &&
                PasswordPolicy.isValid(newPassword) &&
                newPassword == confirmPassword

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
                message = "You are offline. Changing your password needs internet.",
                modifier = Modifier.padding(bottom = 12.dp)
            )

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

                PasswordRequirements(
                    password = newPassword,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                WhyNotTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm new password",
                    placeholder = "Confirm new password",
                    visualTransformation = PasswordVisualTransformation()
                )

                if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                    Text(
                        text = "The new passwords do not match.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WhyNotGray,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
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
                text = if (submitting) "Updating…" else "Update password",
                onClick = { onUpdatePasswordClick(currentPassword, newPassword, confirmPassword) },
                enabled = canUpdate && !submitting
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
            selectedTab = WhyNotTab.PROFILE,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}