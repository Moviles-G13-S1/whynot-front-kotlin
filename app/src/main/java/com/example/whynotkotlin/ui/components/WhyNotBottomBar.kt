package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun WhyNotBottomBar(
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhyNotWhite)
    ) {
        HorizontalDivider(
            color = WhyNotBorder,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                icon = {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = "Home",
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )

            BottomItem(
                icon = {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlists"
                    )
                },
                label = "Wishlists",
                onClick = onWishlistsClick,
                modifier = Modifier.weight(1f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WhyNotBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add",
                        tint = WhyNotWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelSmall,
                    color = WhyNotGray
                )
            }

            BottomItem(
                icon = {
                    Icon(
                        Icons.Outlined.ShoppingBag,
                        contentDescription = "Purchases"
                    )
                },
                label = "Purchases",
                onClick = onPurchasesClick,
                modifier = Modifier.weight(1f)
            )

            BottomItem(
                icon = {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = "Profile",
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides WhyNotGray,
                content = icon
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WhyNotGray
        )
    }
}