package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun WhyNotBottomBar(
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp)
            .background(WhyNotWhite)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("⌂", "Home", onHomeClick)
        BottomItem("♡", "Wishlists", onWishlistsClick)

        Column(
            modifier = Modifier.clickable { onAddClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(WhyNotBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = WhyNotWhite,
                    fontSize = 36.sp
                )
            }

            Text(
                text = "Add",
                style = MaterialTheme.typography.labelSmall,
                color = WhyNotGray
            )
        }

        BottomItem("▣", "Purchases", onPurchasesClick)

        BottomItem(
            icon = "♙",
            label = "Profile",
            onClick = onProfileClick
        )
    }
}

@Composable
private fun BottomItem(
    icon: String,
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 30.sp,
            color = WhyNotGray
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WhyNotGray
        )
    }
}