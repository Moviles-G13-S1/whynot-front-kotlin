package com.example.whynotkotlin.view.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotGray

@Composable
fun LoginScreen(
    submitting: Boolean,
    errorMessage: String?,
    onSubmit: (email: String, password: String) -> Unit,
    onCreateAccountClick: () -> Unit,
    isOnline: Boolean = true
) {
    // The email survives rotation and process death; the password never
    // leaves memory, so it is not written to the saved instance state.
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OfflineBanner(
            isOnline = isOnline,
            message = "You are offline. Signing in needs internet; what you type is kept.",
            modifier = Modifier.padding(top = 40.dp)
        )

        Spacer(modifier = Modifier.height(if (isOnline) 150.dp else 70.dp))

        Text(
            text = "W H Y N O T",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 2.sp
            ),
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(46.dp))

        Text(
            text = "Log in",
            style = MaterialTheme.typography.bodyLarge,
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = WhyNotBeige,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WhyNotTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            WhyNotTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotErrorBanner(message = errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotButton(
            text = if (submitting) "Signing in..." else "Log in",
            onClick = { onSubmit(email, password) },
            filled = false,
            enabled = !submitting
        )

        Spacer(modifier = Modifier.height(36.dp))

        HorizontalDivider(
            color = WhyNotBorder
        )

        TextButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = onCreateAccountClick
        ) {
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )
        }
    }
}
