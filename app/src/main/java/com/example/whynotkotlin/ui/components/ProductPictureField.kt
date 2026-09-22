package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

/**
 * A single-line field for an external image or product link.
 *
 * The backend stores images as URLs and does not use Firebase Storage, so this
 * takes a pasted link rather than opening the device gallery.
 */
@Composable
fun ProductPictureField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Picture",
    placeholder: String = "https://..."
) {
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
                .height(50.dp)
                .background(WhyNotWhite, RoundedCornerShape(10.dp))
                .border(
                    BorderStroke(1.dp, WhyNotBorder),
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
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = WhyNotBlack
                    ),
                    cursorBrush = SolidColor(WhyNotBlack),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = null,
                tint = WhyNotGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
