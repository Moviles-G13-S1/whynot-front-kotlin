package com.example.whynotkotlin.model.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONTokener
import kotlin.coroutines.resume

/** Finds the main picture of a web page the way a browser would see it. */
interface PagePictureFinder {
    suspend fun pictureOf(pageLink: String): String?
}

/**
 * Loads the page in an off-screen WebView (the same engine as Chrome), so
 * stores that block plain requests or add their pictures with JavaScript
 * still work. Once loaded, it reads, in order: the Open Graph / Twitter
 * picture, the product picture in the page's structured data (JSON-LD), and
 * finally the largest picture shown on the page.
 *
 * Nothing is shown to the user; the WebView is destroyed afterwards.
 */
class WebViewPagePictureFinder(
    private val context: Context
) : PagePictureFinder {

    override suspend fun pictureOf(pageLink: String): String? =
        withTimeoutOrNull(TIMEOUT_MILLIS) {
            withContext(Dispatchers.Main) { load(pageLink) }
        }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun load(pageLink: String): String? = suspendCancellableCoroutine { continuation ->
        val handler = Handler(Looper.getMainLooper())
        val webView = WebView(context.applicationContext)
        var finished = false

        fun finish(result: String?) {
            if (finished) return
            finished = true
            handler.removeCallbacksAndMessages(null)
            webView.stopLoading()
            webView.destroy()
            if (continuation.isActive) continuation.resume(result)
        }

        fun extract() {
            if (finished) return
            webView.evaluateJavascript(EXTRACT_SCRIPT) { raw ->
                val value = runCatching { JSONTokener(raw).nextValue() as? String }.getOrNull()
                if (!value.isNullOrBlank() && value.startsWith("http")) finish(value)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Pages often add the product picture right after loading.
                extract()
                handler.postDelayed(::extract, 1_500)
                handler.postDelayed(::extract, 4_000)
            }
        }

        // Give up quietly if nothing was found; the save goes on regardless.
        handler.postDelayed({ finish(null) }, GIVE_UP_MILLIS)
        continuation.invokeOnCancellation { handler.post { finish(null) } }

        webView.loadUrl(pageLink)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 15_000L
        const val GIVE_UP_MILLIS = 12_000L

        val EXTRACT_SCRIPT = """
            (function () {
              function attr(selector) {
                var e = document.querySelector(selector);
                return e ? (e.getAttribute('content') || e.getAttribute('href') || e.getAttribute('src')) : null;
              }
              var found = attr('meta[property="og:image:secure_url"]') || attr('meta[property="og:image"]') ||
                attr('meta[name="og:image"]') || attr('meta[name="twitter:image"]') ||
                attr('meta[property="twitter:image"]') || attr('link[rel="image_src"]');
              if (!found) {
                document.querySelectorAll('script[type="application/ld+json"]').forEach(function (s) {
                  if (found) return;
                  try {
                    [].concat(JSON.parse(s.textContent)).forEach(function (o) {
                      [].concat((o && o['@graph']) || o).forEach(function (x) {
                        if (found || !x || !x.image) return;
                        var im = [].concat(x.image)[0];
                        found = typeof im === 'string' ? im : (im && im.url);
                      });
                    });
                  } catch (e) {}
                });
              }
              if (!found) {
                var best = null, area = 0;
                document.querySelectorAll('img').forEach(function (i) {
                  var a = i.naturalWidth * i.naturalHeight;
                  if (i.naturalWidth >= 200 && i.naturalHeight >= 200 && a > area) {
                    area = a; best = i.currentSrc || i.src;
                  }
                });
                found = best;
              }
              return found ? new URL(found, location.href).href : null;
            })();
        """.trimIndent()
    }
}
