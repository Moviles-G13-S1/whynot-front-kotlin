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
        .build(),
    /**
     * Fallback for stores whose page cannot be read with a plain request
     * (robot checks, pictures added by JavaScript): renders the page like a
     * browser and reads its main picture. Null in unit tests.
     */
    private val pageRenderer: PagePictureFinder? = null
) : ImageLinkResolver {

    override suspend fun resolve(pictureLink: String, pageLink: String): String {
        val typed = ImageLinks.secure(pictureLink)
        // A photo uploaded from the gallery is already a displayable link.
        if (typed.startsWith(STORAGE_PREFIX)) return typed
        val target = typed.ifEmpty { ImageLinks.secure(pageLink) }
        if (target.isEmpty()) return typed

        // 1) Fast path: a direct image, an og:image page or an Amazon link.
        val quick = withTimeoutOrNull(TOTAL_TIMEOUT_MILLIS) {
            withContext(Dispatchers.IO) { imageFrom(target) }
        }

        // 2) Any other store: open the page like a browser and read its picture.
        val resolved = quick ?: pageRenderer?.let { renderer ->
            renderer.pictureOf(target)?.let { found ->
                withTimeoutOrNull(TOTAL_TIMEOUT_MILLIS) {
                    withContext(Dispatchers.IO) { imageFrom(found, allowPage = false) }
                } ?: found
            }
        }

        return resolved?.takeIf { it.length <= InputRules.LINK_MAX_LENGTH } ?: typed
    }

    /** The displayable image behind [link], following one page hop at most. */
    private fun imageFrom(link: String, allowPage: Boolean = true): String? {
        if (allowPage) ImageLinks.amazonPicture(link)?.let { return imageFrom(it, allowPage = false) }

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
                    // A tiny image is a placeholder (Amazon answers an unknown
                    // product id with a 1x1 gif), not a product picture.
                    type.startsWith("image/") ->
                        ImageLinks.secure(finalUrl.toString()).takeUnless {
                            response.body?.contentLength()?.let { it in 0 until MIN_IMAGE_BYTES } == true
                        }

                    // Short links (amzn.to) only reveal the product after redirects.
                    allowPage && ImageLinks.amazonPicture(finalUrl.toString()) != null ->
                        imageFrom(ImageLinks.amazonPicture(finalUrl.toString())!!, allowPage = false)

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
        const val MIN_IMAGE_BYTES = 1_000L

        /** Store sites often refuse requests that do not look like a browser. */
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0 Mobile Safari/537.36 WhyNot/1.0"
    }
}
