package com.example.whynotkotlin.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBlack
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotDanger
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.WhyNotWhite

/**
 * The labelled text field of every form.
 *
 * [error] is what is wrong with [value] right now (null when it is valid). It
 * appears under the field once the user leaves the field, or as soon as
 * [showError] is true, which forms set after a save attempt. [maxLength] stops
 * typing at the limit and says so. [isPassword] hides the text and adds a
 * button to show it.
 */
@Composable
fun WhyNotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    error: String? = null,
    showError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    isPassword: Boolean = false
) {
    val visit = rememberFieldVisit()
    val visibleError = visit.visibleError(error, showError)
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = WhyNotBlack
        )

        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.limitedTo(maxLength)) },
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WhyNotGray
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = WhyNotGray
                        )
                    }
                }
            } else {
                null
            },
            isError = visibleError != null,
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = when {
                isPassword && !passwordVisible -> PasswordVisualTransformation()
                isPassword -> VisualTransformation.None
                else -> visualTransformation
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            singleLine = singleLine,
            // A minimum instead of a fixed height, so large system fonts are not cut.
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .then(visit.modifier)
                .semantics { if (visibleError != null) error(visibleError) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WhyNotWhite,
                unfocusedContainerColor = WhyNotWhite,
                errorContainerColor = WhyNotWhite,
                focusedBorderColor = WhyNotBlack,
                unfocusedBorderColor = WhyNotBorder,
                errorBorderColor = WhyNotDanger,
                focusedTextColor = WhyNotBlack,
                unfocusedTextColor = WhyNotBlack,
                errorTextColor = WhyNotBlack,
                cursorColor = WhyNotBlack,
                errorCursorColor = WhyNotBlack,
                errorTrailingIconColor = WhyNotGray
            )
        )

        FieldSupportText(error = visibleError, length = value.length, maxLength = maxLength)
    }
}
