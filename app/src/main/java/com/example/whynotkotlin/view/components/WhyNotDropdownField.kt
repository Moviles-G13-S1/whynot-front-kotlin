package com.example.whynotkotlin.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBlack
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotDanger
import com.example.whynotkotlin.view.theme.WhyNotDisabled
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.WhyNotWhite

/**
 * A labelled select styled like [WhyNotTextField].
 *
 * [options] maps a stable value to the label shown, so screens can bind ids
 * (category ids, wishlist ids) without displaying them.
 *
 * [error] (such as "Select a city") appears under the field when [showError]
 * is true, which forms set after a save attempt.
 */
@Composable
fun WhyNotDropdownField(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    error: String? = null,
    showError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val visibleError = error?.takeIf { showError }

    val selectedLabel = options.firstOrNull { it.first == selectedValue }?.second

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = WhyNotBlack
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .background(WhyNotWhite, RoundedCornerShape(10.dp))
                    .border(
                        BorderStroke(1.dp, if (visibleError != null) WhyNotDanger else WhyNotBorder),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enabled && options.isNotEmpty()) {
                        expanded = true
                    }
                    .semantics { if (visibleError != null) error(visibleError) }
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLabel ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        !enabled || options.isEmpty() -> WhyNotDisabled
                        selectedLabel == null -> WhyNotGray
                        else -> WhyNotBlack
                    }
                )

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = label,
                    tint = WhyNotGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = WhyNotWhite,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, WhyNotBorder)
            ) {
                options.forEach { (value, optionLabel) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }

        FieldSupportText(error = visibleError)
    }
}
