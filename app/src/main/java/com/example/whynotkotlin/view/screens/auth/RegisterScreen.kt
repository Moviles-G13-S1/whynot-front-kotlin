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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.model.CanonicalCities
import com.example.whynotkotlin.model.Genders
import com.example.whynotkotlin.model.CanonicalCategories
import com.example.whynotkotlin.model.InputRules
import com.example.whynotkotlin.model.PasswordPolicy
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.PasswordRequirements
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotDropdownField
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotGray

/**
 * Gender, preferred category and city are selects, not free text: the Security
 * Rules accept only the three gender values, a `preferredCategoryId` that
 * references a seeded category and a `cityId` that references a seeded city, so
 * typed input would fail on submit.
 *
 * Each field shows its own error underneath ("Enter your age", "Age must be
 * between 13 and 120") once the user leaves it, using the same rules as
 * [com.example.whynotkotlin.controller.AuthController]. Tapping Create shows
 * every remaining error at once instead of sending an invalid form.
 */
@Composable
fun RegisterScreen(
    submitting: Boolean,
    errorMessage: String?,
    onSubmit: (
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        cityId: String
    ) -> Unit,
    onBackToLoginClick: () -> Unit,
    isOnline: Boolean = true
) {
    // Answers survive rotation, process death and a failed offline submit;
    // the password stays in memory only, never in the saved instance state.
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf("") }
    var cityId by rememberSaveable { mutableStateOf("") }
    var showAllErrors by rememberSaveable { mutableStateOf(false) }

    val nameError = InputRules.name(name)
    val emailError = InputRules.email(email)
    val genderError = if (gender.isBlank()) "Select a gender" else null
    val ageError = InputRules.age(age)
    val passwordError =
        if (password.isEmpty()) "Enter a password" else PasswordPolicy.firstError(password, email, name)
    val confirmError = when {
        confirmPassword.isEmpty() -> "Confirm your password"
        confirmPassword != password -> "The passwords do not match"
        else -> null
    }
    val categoryError = if (categoryId.isBlank()) "Select a preferred category" else null
    val cityError = if (cityId.isBlank()) "Select a city" else null
    val formValid = listOf(
        nameError, emailError, genderError, ageError,
        passwordError, confirmError, categoryError, cityError
    ).all { it == null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OfflineBanner(
            isOnline = isOnline,
            message = "You are offline. Creating an account needs internet; your answers are kept.",
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
            WhyNotTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                error = nameError,
                showError = showAllErrors,
                maxLength = InputRules.NAME_MAX_LENGTH
            )

            WhyNotTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "name@example.com",
                error = emailError,
                showError = showAllErrors,
                keyboardType = KeyboardType.Email,
                maxLength = InputRules.EMAIL_MAX_LENGTH
            )

            WhyNotDropdownField(
                label = "Gender",
                selectedValue = gender,
                options = Genders.all.map { it to it },
                onValueChange = { gender = it },
                placeholder = "Select",
                error = genderError,
                showError = showAllErrors
            )

            WhyNotTextField(
                value = age,
                onValueChange = { age = it },
                label = "Age",
                placeholder = "18",
                error = ageError,
                showError = showAllErrors,
                keyboardType = KeyboardType.Number,
                maxLength = InputRules.AGE_MAX_LENGTH
            )

            WhyNotTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                error = passwordError,
                showError = showAllErrors,
                isPassword = true
            )

            PasswordRequirements(password = password, email = email, name = name)

            WhyNotTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                error = confirmError,
                showError = showAllErrors,
                isPassword = true
            )

            WhyNotDropdownField(
                label = "Preferred Category",
                selectedValue = categoryId,
                options = CanonicalCategories.all.map { it.id to it.name },
                onValueChange = { categoryId = it },
                placeholder = "Select",
                error = categoryError,
                showError = showAllErrors
            )

            WhyNotDropdownField(
                label = "City",
                selectedValue = cityId,
                options = CanonicalCities.all.map { it.id to it.name },
                onValueChange = { cityId = it },
                placeholder = "Select",
                error = cityError,
                showError = showAllErrors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotErrorBanner(message = errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        WhyNotButton(
            text = if (submitting) "Creating..." else "Create an account",
            onClick = {
                if (formValid) {
                    onSubmit(name, email, password, confirmPassword, gender, age, categoryId, cityId)
                } else {
                    showAllErrors = true
                }
            },
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
