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
import com.example.whynotkotlin.features.profile.domain.CanonicalCities
import com.example.whynotkotlin.features.profile.domain.Genders
import com.example.whynotkotlin.features.wishlists.domain.CanonicalCategories
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotDropdownField
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.components.WhyNotTextField
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

/**
 * Gender, preferred category and city are selects, not free text: the Security
 * Rules accept only the three gender values, a `preferredCategoryId` that
 * references a seeded category and a `cityId` that references a seeded city, so
 * typed input would fail on submit.
 */
@Composable
fun RegisterScreen(
    submitting: Boolean,
    errorMessage: String?,
    onSubmit: (
        name: String,
        email: String,
        password: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        cityId: String
    ) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var cityId by remember { mutableStateOf("") }

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

            WhyNotDropdownField(
                label = "Gender",
                selectedValue = gender,
                options = Genders.all.map { it to it },
                onValueChange = { gender = it },
                placeholder = "Select"
            )

            WhyNotTextField(age, { age = it }, "Age", placeholder = "18")

            WhyNotTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                visualTransformation = PasswordVisualTransformation()
            )

            WhyNotDropdownField(
                label = "Preferred Category",
                selectedValue = categoryId,
                options = CanonicalCategories.all.map { it.id to it.name },
                onValueChange = { categoryId = it },
                placeholder = "Select"
            )

            WhyNotDropdownField(
                label = "City",
                selectedValue = cityId,
                options = CanonicalCities.all.map { it.id to it.name },
                onValueChange = { cityId = it },
                placeholder = "Select"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotErrorBanner(message = errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotButton(
            text = if (submitting) "Creating..." else "Create an account",
            onClick = { onSubmit(name, email, password, gender, age, categoryId, cityId) },
            enabled = !submitting
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
