package com.example.whynotkotlin.view.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.R

val FrankRuhlLibre = FontFamily(
    Font(
        resId = R.font.frank_ruhl_libre,
        weight = FontWeight.Normal
    )
)

val Poppins = FontFamily(
    Font(
        resId = R.font.poppins_light,
        weight = FontWeight.Light
    ),
    Font(
        resId = R.font.poppins_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.poppins_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.poppins_semibold,
        weight = FontWeight.SemiBold
    )
)

/** Editorial style used by headings and field labels, as on iOS. */
fun whyNotSerif(size: Int, color: Color = Color.Black): TextStyle =
    TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = size.sp,
        color = color
    )

/** Light Poppins style used by secondary text, as on iOS. */
fun whyNotMuted(size: Int, color: Color = WhyNotGray): TextStyle =
    TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = size.sp,
        lineHeight = (size * 1.15f).sp,
        color = color
    )

val WhyNotTypography = Typography(

    displaySmall = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),

    titleLarge = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    titleMedium = TextStyle(
        fontFamily = FrankRuhlLibre,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),

    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),

    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),

    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)