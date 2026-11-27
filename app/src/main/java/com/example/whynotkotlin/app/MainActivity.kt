package com.example.whynotkotlin.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import com.example.whynotkotlin.view.navigation.WhyNotNavigation
import com.example.whynotkotlin.view.theme.WhynotkotlinTheme

/** A FragmentActivity because Android's biometric prompt is hosted by one. */
class MainActivity : FragmentActivity() {

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