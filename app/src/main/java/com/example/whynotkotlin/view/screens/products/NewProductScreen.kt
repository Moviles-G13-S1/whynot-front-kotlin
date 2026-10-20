package com.example.whynotkotlin.view.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.SpeechUnavailableException
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.CategoryChip
import com.example.whynotkotlin.view.components.ProductPictureField
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.components.VoiceInputButton
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** The fields that can be filled by voice. */
private enum class VoiceField { NAME, BRAND }

/**
 * The manual product form.
 *
 * `brand` and `price` are required by the Security Rules, so they are part of
 * the form even though the original mockup only showed name and picture.
 *
 * Name and brand can be dictated through the microphone (the sensor feature).
 * There is one microphone per field; the screen remembers which one was
 * tapped, writes the transcription there, and then hands the state back with
 * [onVoiceInputConsumed] so the same result is never applied twice.
 */
@Composable
fun NewProductScreen(
    wishlists: List<WishlistSummary>,
    prefilledProductUrl: String,
    saving: Boolean,
    errorMessage: String?,
    onSave: (
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String
    ) -> Unit,
    listenForSpeech: suspend () -> String = { throw SpeechUnavailableException() },
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var productUrl by remember { mutableStateOf(prefilledProductUrl) }
    var selectedWishlistId by remember { mutableStateOf("") }

    var voiceTarget by remember { mutableStateOf<VoiceField?>(null) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    var listening by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun startVoiceInput(field: VoiceField) {
        voiceTarget = field
        voiceError = null

        // One recognition at a time; a tap while listening only retargets it.
        if (listening) return
        listening = true

        scope.launch {
            try {
                // Recognisers return lower case; a product name reads better
                // with a capital first letter.
                val text = listenForSpeech().replaceFirstChar { it.uppercaseChar() }

                when (voiceTarget) {
                    VoiceField.NAME -> name = text
                    VoiceField.BRAND -> brand = text
                    null -> Unit
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                voiceError = error.message ?: "Voice input failed. Try again."
            } finally {
                listening = false
                voiceTarget = null
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "New Product",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotCream,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    WhyNotTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Name",
                        modifier = Modifier.weight(1f)
                    )

                    VoiceInputButton(
                        onVoiceInput = { startVoiceInput(VoiceField.NAME) },
                        enabled = !saving,
                        isListening = listening && voiceTarget == VoiceField.NAME,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    WhyNotTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "Brand",
                        modifier = Modifier.weight(0.6f)
                    )

                    VoiceInputButton(
                        onVoiceInput = { startVoiceInput(VoiceField.BRAND) },
                        enabled = !saving,
                        isListening = listening && voiceTarget == VoiceField.BRAND,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    WhyNotTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Price",
                        placeholder = "0",
                        modifier = Modifier.weight(0.4f)
                    )
                }

                if (listening) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Listening… say the product ${
                            if (voiceTarget == VoiceField.BRAND) "brand" else "name"
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhyNotGray
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = productUrl,
                    onValueChange = { productUrl = it },
                    label = "Store link",
                    placeholder = "https://www.somestore.com/..."
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Save to:",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (wishlists.isEmpty()) {
                Text(
                    text = "Create a wishlist first to save products into it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhyNotGray
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    wishlists.forEach { summary ->
                        CategoryChip(
                            text = summary.categoryName,
                            selected = selectedWishlistId == summary.wishlist.id,
                            onClick = {
                                selectedWishlistId =
                                    if (selectedWishlistId == summary.wishlist.id) {
                                        ""
                                    } else {
                                        summary.wishlist.id
                                    }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = errorMessage ?: voiceError)

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotButton(
                text = if (saving) "Saving..." else "Save Item",
                onClick = {
                    onSave(selectedWishlistId, name, brand, price, imageUrl, productUrl)
                },
                filled = true,
                enabled = !saving &&
                    name.isNotBlank() &&
                    brand.isNotBlank() &&
                    price.isNotBlank() &&
                    selectedWishlistId.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        WhyNotBottomBar(
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}
