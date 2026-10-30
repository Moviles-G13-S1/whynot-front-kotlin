package com.example.whynotkotlin.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.PasswordPolicy
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted

/**
 * Live checklist of the password requirements, so the user sees what is
 * missing while typing instead of only after submitting.
 *
 * With the user's [email] or [name] it also checks that the password does not
 * include them.
 */
@Composable
fun PasswordRequirements(
    password: String,
    modifier: Modifier = Modifier,
    email: String = "",
    name: String = ""
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PasswordPolicy.rulesFor(email, name).forEach { rule ->
            val met = password.isNotEmpty() && rule.isMetBy(password)
            val color = if (met) Color.Black else WhyNotGray

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (met) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (met) "Met" else "Not met",
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = rule.label, style = whyNotMuted(12, color))
            }
        }
    }
}
