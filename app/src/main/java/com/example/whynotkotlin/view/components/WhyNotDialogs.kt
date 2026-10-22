package com.example.whynotkotlin.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.whynotkotlin.view.theme.Poppins
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotWhite
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

/**
 * WhyNot-styled two-choice dialog, as shown by iOS when the user taps Add
 * without having a wishlist.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhyNotChoiceDialog(
    title: String,
    message: String,
    dismissLabel: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = WhyNotWhite,
            border = BorderStroke(1.dp, WhyNotBorder)
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp)
            ) {
                Text(text = title, style = whyNotSerif(24))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = message, style = whyNotMuted(14))

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Text(
                        text = dismissLabel,
                        style = whyNotMuted(15),
                        modifier = Modifier.clickable { onDismiss() }
                    )

                    Text(
                        text = confirmLabel,
                        style = whyNotMuted(15),
                        modifier = Modifier.clickable { onConfirm() }
                    )
                }
            }
        }
    }
}

private val DialogSurface = Color(0xFFF2E5E7)
private val DialogText = Color(0xFF21191C)
private val DialogAction = Color(0xFF834D60)
private val DialogActionContainer = Color(0xFFE9D9DD)

/**
 * Plain Material confirmation dialog, matching the one iOS shows before
 * deleting a product.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhyNotConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val actionStyle = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = DialogAction
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DialogSurface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        color = DialogText
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = DialogText
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(text = "Cancel", style = actionStyle)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DialogActionContainer)
                            .clickable { onConfirm() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(text = confirmLabel, style = actionStyle)
                    }
                }
            }
        }
    }
}
