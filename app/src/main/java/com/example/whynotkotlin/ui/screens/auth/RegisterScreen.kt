package com.example.whynotkotlin.ui.screens.auth

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotTextField
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun RegisterScreen(
    onCreateAccountClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "W H Y N O T",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 2.sp
            ),
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Create an account",
            style = MaterialTheme.typography.bodyLarge,
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    WhyNotBeige,
                    RoundedCornerShape(22.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            WhyNotTextField(name, { name = it }, "Name")
            WhyNotTextField(email, { email = it }, "Email")
            WhyNotTextField(gender, { gender = it }, "Gender")
            WhyNotTextField(age, { age = it }, "Age")

            WhyNotTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                visualTransformation = PasswordVisualTransformation()
            )

            WhyNotTextField(
                value = category,
                onValueChange = { category = it },
                label = "Preferred Category"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        WhyNotButton(
            text = "Create an account",
            onClick = onCreateAccountClick
        )

        TextButton(onClick = onBackToLoginClick) {
            Text(
                text = "Back to log in",
                style = MaterialTheme.typography.bodyMedium,
                color = WhyNotGray
            )
        }
    }
}