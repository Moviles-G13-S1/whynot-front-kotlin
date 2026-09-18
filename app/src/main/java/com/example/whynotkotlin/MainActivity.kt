package com.example.whynotkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.whynotkotlin.ui.navigation.WhyNotNavigation
import com.example.whynotkotlin.ui.theme.WhynotkotlinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            WhynotkotlinTheme {
                WhyNotNavigation()
            }
        }
    }
}