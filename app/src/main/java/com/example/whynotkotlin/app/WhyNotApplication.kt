package com.example.whynotkotlin.app

import android.app.Application
import com.example.whynotkotlin.app.AppDependencies

/**
 * Builds the dependency graph once, after Firebase has been initialised by the
 * Google Services plugin's content provider.
 */
class WhyNotApplication : Application() {

    lateinit var dependencies: AppDependencies
        private set

    override fun onCreate() {
        super.onCreate()
        dependencies = AppDependencies.firebase(this)
    }
}
