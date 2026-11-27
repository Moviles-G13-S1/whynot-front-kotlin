package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.ImageLinks
import com.example.whynotkotlin.model.InputRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Turns what the user pasted into a link the app can actually display.
 *
 * People rarely paste a direct image link: they paste the product page, an
 * `http://` link, or a short link that redirects. Each of those used to be
 * saved as is and then showed no picture. Before saving, this resolves:
 * - a direct image (after redirects) to its final https address;
 * - a web page to the main picture it declares (og:image / twitter:image);
 * - no picture link at all to the picture of the product page, if given.
 */
interface ImageLinkResolver {

    /**
     * The picture link to store. [pictureLink] is what the user typed in the
     * picture field (may be blank); [pageLink] is the product's store link,
     * used when the picture field is blank. Returns [pictureLink] made secure
     * when nothing better is found, so a save is never blocked by this.
     */
    suspend fun resolve(pictureLink: String, pageLink: String = ""): String
}

class HttpImageLinkResolver(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) : ImageLinkResolver {

    override suspend fun resolve(pictureLink: String, pageLink: String): String {
        val typed = ImageLinks.secure(pictureLink)
        // A photo uploaded from the gallery is already a displayable link.
        if (typed.startsWith(STORAGE_PREFIX)) return typed
        val resolved = withTimeoutOrNull(TOTAL_TIMEOUT_MILLIS) {
            withContext(Dispatchers.IO) {
                when {
                    typed.isNotEmpty() -> imageFrom(typed)
                    pageLink.isNotBlank() -> imageFrom(ImageLinks.secure(pageLink))
                    else -> null
                }
            }
        }
        return resolved?.takeIf { it.length <= InputRules.LINK_MAX_LENGTH } ?: typed
    }

    /** The displayable image behind [link], following one page hop at most. */
    private fun imageFrom(link: String, allowPage: Boolean = true): String? {
        val request = Request.Builder()
            .url(link)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Accept", "text/html,image/webp,image/apng,image/*,*/*;q=0.8")
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null

                val finalUrl = response.request.url
                val type = response.header("Content-Type").orEmpty().lowercase()

                when {
                    type.startsWith("image/") -> ImageLinks.secure(finalUrl.toString())

                    allowPage && type.contains("html") -> {
                        // Only the <head> is needed for the meta tags.
                        val html = response.body?.source()?.let { source ->
                            source.request(MAX_HTML_BYTES)
                            source.buffer.snapshot().utf8()
                        }
                        html?.let(ImageLinks::pagePicture)
                            ?.let { finalUrl.resolve(it)?.toString() }
                            ?.let { imageFrom(ImageLinks.secure(it), allowPage = false) }
                    }

                    else -> null
                }
            }
        }.getOrNull()
    }

    private companion object {
        const val TOTAL_TIMEOUT_MILLIS = 12_000L
        const val STORAGE_PREFIX = "https://firebasestorage.googleapis.com/"
        const val MAX_HTML_BYTES = 512L * 1024

        /** Store sites often refuse requests that do not look like a browser. */
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0 Mobile Safari/537.36 WhyNot/1.0"
    }
}
