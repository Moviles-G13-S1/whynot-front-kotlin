package com.example.whynotkotlin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.whynotkotlin.view.navigation.WhyNotNavigation
import com.example.whynotkotlin.view.theme.WhynotkotlinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val dependencies = (application as WhyNotApplication).dependencies

        setContent {
            CompositionLocalProvider(LocalAppDependencies provides dependencies) {
                WhynotkotlinTheme {
                    WhyNotNavigation()
                }
            }
        }
    }
}