package com.example.whynotkotlin.view.screens.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotWhite
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

/**
 * Asks what the user actually paid when marking a product as purchased.
 *
 * The field starts with the price the product was saved with, so a purchase at
 * the expected price is one tap. The difference between both prices is what
 * the Sprint 3 BQ adds up as money saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasePriceDialog(
    savedPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var paid by remember { mutableStateOf(savedPrice.asEditablePrice()) }

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
                Text(text = "Mark as purchased", style = whyNotSerif(24))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You saved it at ${savedPrice.asPriceLabel()}. How much did you pay?",
                    style = whyNotMuted(14)
                )

                Spacer(modifier = Modifier.height(16.dp))

                WhyNotTextField(
                    value = paid,
                    onValueChange = { paid = it },
                    label = "Price paid",
                    placeholder = "0"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        style = whyNotMuted(15),
                        modifier = Modifier.clickable { onDismiss() }
                    )

                    Text(
                        text = "Purchased",
                        style = whyNotSerif(16),
                        modifier = Modifier.clickable(enabled = paid.isNotBlank()) {
                            onConfirm(paid)
                        }
                    )
                }
            }
        }
    }
}

/** "120000" rather than "120000.0", so the field reads like what users type. */
private fun Double.asEditablePrice(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
