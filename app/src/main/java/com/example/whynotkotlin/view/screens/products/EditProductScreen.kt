package com.example.whynotkotlin.view.screens.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.theme.Poppins
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotField
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

/** Form for changing a saved product, matching the iOS "Edit Item" screen. */
@Composable
fun EditProductScreen(
    product: Product?,
    loading: Boolean,
    wishlists: List<WishlistSummary>,
    sourceTab: WhyNotTab,
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
    onCancelClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                product != null -> EditProductForm(
                    product = product,
                    wishlists = wishlists,
                    saving = saving,
                    errorMessage = errorMessage,
                    onSave = onSave,
                    onCancelClick = onCancelClick
                )

                else -> Text(
                    text = "Product not found.",
                    style = whyNotMuted(14),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        WhyNotBottomBar(
            selectedTab = sourceTab,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun EditProductForm(
    product: Product,
    wishlists: List<WishlistSummary>,
    saving: Boolean,
    errorMessage: String?,
    onSave: (String, String, String, String, String, String) -> Unit,
    onCancelClick: () -> Unit
) {
    var name by rememberSaveable(product.id) { mutableStateOf(product.name) }
    var brand by rememberSaveable(product.id) { mutableStateOf(product.brand) }
    var price by rememberSaveable(product.id) {
        mutableStateOf(
            if (product.price % 1.0 == 0.0) product.price.toLong().toString()
            else product.price.toString()
        )
    }
    var imageUrl by rememberSaveable(product.id) { mutableStateOf(product.imageUrl) }
    var productUrl by rememberSaveable(product.id) { mutableStateOf(product.productUrl) }
    var wishlistId by rememberSaveable(product.id) { mutableStateOf(product.wishlistId) }

    val parsedPrice = price.trim().replace(",", ".").toDoubleOrNull()
    val canSave = name.isNotBlank() &&
            brand.isNotBlank() &&
            parsedPrice != null &&
            parsedPrice >= 0 &&
            wishlistId.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 40.dp)
    ) {
        Text(text = "Edit Item", style = whyNotSerif(30))

        Spacer(modifier = Modifier.height(42.dp))

        LabeledField("Name", name, "Product name") { name = it }
        LabeledField("Brand", brand, "Brand name") { brand = it }
        LabeledField("Price", price, "Product price", KeyboardType.Decimal) { price = it }
        LabeledField("Picture link", imageUrl, "https://...", KeyboardType.Uri) { imageUrl = it }
        LabeledField("Product link", productUrl, "https://...", KeyboardType.Uri, last = true) {
            productUrl = it
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "Save to:", style = whyNotMuted(15))

        Spacer(modifier = Modifier.height(18.dp))

        if (wishlists.isEmpty()) {
            Text(text = "No wishlists available.", style = whyNotMuted(14))
        } else {
            wishlists.forEach { summary ->
                val selected = summary.wishlist.id == wishlistId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { wishlistId = summary.wishlist.id }
                        .padding(horizontal = 7.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (selected) Color.Black else WhyNotGray,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = summary.categoryName, style = whyNotMuted(15))
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = errorMessage, style = whyNotMuted(13))
        }

        Spacer(modifier = Modifier.height(44.dp))

        Button(
            onClick = { onSave(wishlistId, name, brand, price, imageUrl, productUrl) },
            enabled = canSave && !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
                disabledContainerColor = Color.Black.copy(alpha = 0.25f),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = if (saving) "Saving..." else "Save Changes",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cancel",
            style = whyNotMuted(15),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onCancelClick() }
                .padding(12.dp)
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    last: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Text(text = label, style = whyNotSerif(20))

    Spacer(modifier = Modifier.height(14.dp))

    val shape = RoundedCornerShape(18.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            fontFamily = Poppins,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black
        ),
        cursorBrush = SolidColor(Color.Black),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(WhyNotField, shape)
                    .border(BorderStroke(1.dp, WhyNotBorder), shape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(text = hint, style = whyNotMuted(14))
                }
                innerTextField()
            }
        }
    )

    if (!last) {
        Spacer(modifier = Modifier.height(30.dp))
    }
}
