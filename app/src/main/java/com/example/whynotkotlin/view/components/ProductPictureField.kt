package com.example.whynotkotlin.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBlack
import com.example.whynotkotlin.model.InputRules
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotDanger
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.WhyNotWhite

/**
 * A single-line field for an external image or product link.
 *
 * The backend stores images as URLs and does not use Firebase Storage, so this
 * takes a pasted link rather than opening the device gallery.
 *
 * [error] appears under the field once the user leaves it, or when
 * [showError] is true after a save attempt.
 */
@Composable
fun ProductPictureField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Picture",
    placeholder: String = "https://...",
    error: String? = null,
    showError: Boolean = false
) {
    val visit = rememberFieldVisit()
    val visibleError = visit.visibleError(error, showError)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = WhyNotBlack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .background(WhyNotWhite, RoundedCornerShape(10.dp))
                .border(
                    BorderStroke(1.dp, if (visibleError != null) WhyNotDanger else WhyNotBorder),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WhyNotGray
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it.limitedTo(InputRules.LINK_MAX_LENGTH)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = WhyNotBlack
                    ),
                    cursorBrush = SolidColor(WhyNotBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(visit.modifier)
                        .semantics { if (visibleError != null) error(visibleError) }
                )
            }

            // One tap pastes the copied link: long-pressing a small field to
            // find "Paste" is easy to miss on a phone.
            val clipboard = LocalClipboardManager.current
            val context = androidx.compose.ui.platform.LocalContext.current
            Text(
                text = "Paste",
                style = MaterialTheme.typography.labelLarge,
                color = WhyNotBlack,
                modifier = Modifier
                    .clickable {
                        val pasted = clipboard.getText()?.text?.trim().orEmpty()
                        if (pasted.isNotEmpty()) {
                            onValueChange(pasted.limitedTo(InputRules.LINK_MAX_LENGTH))
                        } else {
                            // "Copy image" on Android copies the picture itself,
                            // not a link, so there is no text to paste.
                            android.widget.Toast.makeText(
                                context,
                                "Copy the product page link (Share → Copy link) instead of the image.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .padding(vertical = 8.dp)
            )
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = null,
                tint = WhyNotGray,
                modifier = Modifier.size(18.dp)
            )
        }

        FieldSupportText(error = visibleError)
    }
}
