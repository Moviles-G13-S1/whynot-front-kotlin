package com.example.whynotkotlin.view.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.example.whynotkotlin.view.theme.WhyNotErrorText
import com.example.whynotkotlin.view.theme.WhyNotGray

/**
 * Whether the user has already left a field once.
 *
 * A field shows its error from then on, or as soon as its form asks for every
 * error after a save attempt. The user sees each problem under its own field
 * while filling the form, without being told off while typing the first
 * characters.
 */
@Stable
class FieldVisit {

    private var left by mutableStateOf(false)
    private var focused = false

    /** Put on the text field so leaving it counts as a visit. */
    val modifier: Modifier = Modifier.onFocusChanged { state ->
        if (focused && !state.isFocused) left = true
        focused = state.isFocused
    }

    /** [error] when it should be visible now, or null. */
    fun visibleError(error: String?, showAllErrors: Boolean): String? =
        error?.takeIf { left || showAllErrors }
}

@Composable
fun rememberFieldVisit(): FieldVisit = remember { FieldVisit() }

/**
 * The line under a field: its [error], or a note that the text reached
 * [maxLength], which explains why typing stopped.
 */
@Composable
fun FieldSupportText(
    error: String?,
    modifier: Modifier = Modifier,
    length: Int = 0,
    maxLength: Int? = null
) {
    when {
        error != null -> Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotErrorText,
            modifier = modifier
        )

        maxLength != null && length >= maxLength -> Text(
            text = "Maximum $maxLength characters",
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray,
            modifier = modifier
        )
    }
}

/** [this] cut to [maxLength] characters, or unchanged without a limit. */
fun String.limitedTo(maxLength: Int?): String =
    if (maxLength == null || length <= maxLength) this else take(maxLength)
