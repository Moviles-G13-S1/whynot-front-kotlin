package com.example.whynotkotlin.app

import android.app.Application
import android.content.pm.ApplicationInfo
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath

/**
 * Builds the dependency graph once, after Firebase has been initialised by the
 * Google Services plugin's content provider.
 *
 * It also provides the single app-wide Coil [ImageLoader], so every image the
 * app shows shares the same memory and disk caches.
 */
class WhyNotApplication : Application(), SingletonImageLoader.Factory {

    lateinit var dependencies: AppDependencies
        private set

    override fun onCreate() {
        super.onCreate()
        dependencies = AppDependencies.firebase(this)
    }

    /**
     * Memory cache for images on screen right now, disk cache so a picture seen
     * once still shows after a restart or offline. In debug builds the logger
     * prints whether each image came from memory, disk or the network.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(IMAGE_CACHE_DIR).toOkioPath())
                    .maxSizeBytes(DISK_CACHE_BYTES)
                    .build()
            }
            .components {
                // Wikimedia and several CDNs reject OkHttp's default
                // "okhttp/x" agent with 403, so images identify the app.
                add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient() }))
            }
            .crossfade(true)
            .apply { if (isDebuggable()) logger(DebugLogger()) }
            .build()

    private fun imageHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", IMAGE_USER_AGENT)
                        .build()
                )
            }
            .build()

    private fun isDebuggable(): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private companion object {
        const val MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_CACHE_DIR = "image_cache"
        const val DISK_CACHE_BYTES = 50L * 1024 * 1024
        const val IMAGE_USER_AGENT = "WhyNot/1.0 (Android)"
    }
}
