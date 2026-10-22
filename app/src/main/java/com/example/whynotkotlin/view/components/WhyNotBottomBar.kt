package com.example.whynotkotlin.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.R
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.navigation.LocalWhyNotNavigator
import com.example.whynotkotlin.view.theme.Poppins
import com.example.whynotkotlin.view.theme.WhyNotDivider
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted

/** The tab a screen belongs to, highlighted in the bottom bar. */
enum class WhyNotTab { HOME, WISHLISTS, ADD, PURCHASES, PROFILE }

/**
 * Five-item navigation bar shared by the main screens, matching the iOS app.
 *
 * As on iOS, Add only opens the product form when the user already has a
 * wishlist; otherwise it asks them to create one first.
 */
@Composable
fun WhyNotBottomBar(
    selectedTab: WhyNotTab? = null,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val catalogResult by LocalAppDependencies.current
        .wishlistController
        .catalog
        .collectAsStateWithLifecycle()
    val navigator = LocalWhyNotNavigator.current

    var askForWishlist by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
            .background(Color(0xF9FEFDFB))
    ) {
        HorizontalDivider(
            color = WhyNotDivider,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(91.dp)
                .padding(start = 14.dp, end = 14.dp, top = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                iconRes = R.drawable.ic_nav_home,
                label = "Home",
                selected = selectedTab == WhyNotTab.HOME,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )

            BottomItem(
                iconRes = R.drawable.ic_nav_wishlists,
                label = "Wishlists",
                selected = selectedTab == WhyNotTab.WISHLISTS,
                onClick = onWishlistsClick,
                modifier = Modifier.weight(1f)
            )

            AddItem(
                onClick = {
                    when (catalogResult?.getOrNull()?.summaries?.isNotEmpty()) {
                        true -> onAddClick()
                        false -> askForWishlist = true
                        // Still loading: wait for the next tap.
                        null -> Unit
                    }
                },
                modifier = Modifier.weight(1f)
            )

            BottomItem(
                iconRes = R.drawable.ic_nav_purchases,
                label = "Purchases",
                selected = selectedTab == WhyNotTab.PURCHASES,
                onClick = onPurchasesClick,
                modifier = Modifier.weight(1f)
            )

            BottomItem(
                iconRes = R.drawable.ic_nav_profile,
                label = "Profile",
                selected = selectedTab == WhyNotTab.PROFILE,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (askForWishlist) {
        WhyNotChoiceDialog(
            title = "Create a wishlist first",
            message = "You need at least one wishlist before adding products.",
            dismissLabel = "Cancel",
            confirmLabel = "Create Wishlist",
            onDismiss = { askForWishlist = false },
            onConfirm = {
                askForWishlist = false
                navigator?.toNewWishlist()
            }
        )
    }
}

@Composable
private fun BottomItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = if (selected) Color.Black else WhyNotGray,
            modifier = Modifier.size(width = 28.dp, height = 29.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            maxLines = 1,
            style = TextStyle(
                fontFamily = Poppins,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Light,
                color = if (selected) Color.Black else WhyNotGray
            )
        )
    }
}

@Composable
private fun AddItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_add),
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Add",
            style = whyNotMuted(10)
        )
    }
}
