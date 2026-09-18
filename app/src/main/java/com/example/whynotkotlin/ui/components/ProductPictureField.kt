package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun ProductPictureField(
    fileName: String,
    onPickPictureClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Picture",
    placeholder: String = ""
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
                .clickable { onPickPictureClick() }
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fileName.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (fileName.isBlank()) WhyNotGray else WhyNotBlack
            )

            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = "Upload picture",
                tint = WhyNotBlack,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
