package com.example.whynotkotlin.model

/**
 * Small, pure helpers for picture links, shared by the resolver and the views.
 */
object ImageLinks {

    /**
     * Android blocks plain-http traffic by default, so an `http://` picture
     * would never load. Almost every image host also serves https, so the
     * link is upgraded instead of failing silently.
     */
    fun secure(link: String): String {
        val trimmed = link.trim()
        return if (trimmed.startsWith("http://", ignoreCase = true)) {
            "https://" + trimmed.substring("http://".length)
        } else {
            trimmed
        }
    }

    private val metaTag = Regex("<meta\\b[^>]*>", RegexOption.IGNORE_CASE)
    private val linkTag = Regex("<link\\b[^>]*>", RegexOption.IGNORE_CASE)
    private val keyAttribute = Regex("\\b(?:property|name|itemprop)\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
    private val contentAttribute = Regex("\\bcontent\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
    private val relAttribute = Regex("\\brel\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
    private val hrefAttribute = Regex("\\bhref\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)

    /** Preferred order: the picture a store shares when its page is linked. */
    private val imageKeys = listOf(
        "og:image:secure_url", "og:image", "og:image:url",
        "twitter:image", "twitter:image:src", "image"
    )

    /**
     * The main picture a product page declares for link previews (Open Graph,
     * Twitter card or `image_src`), or null when the page declares none.
     * The value may be relative; the caller resolves it against the page.
     */
    fun pagePicture(html: String): String? {
        val metas = metaTag.findAll(html).mapNotNull { match ->
            val tag = match.value
            val key = keyAttribute.find(tag)?.groupValues?.get(1)?.lowercase() ?: return@mapNotNull null
            val content = contentAttribute.find(tag)?.groupValues?.get(1) ?: return@mapNotNull null
            key to content
        }.toList()

        imageKeys.forEach { key ->
            metas.firstOrNull { it.first == key }?.second?.let { return unescape(it) }
        }

        return linkTag.findAll(html)
            .firstOrNull { relAttribute.find(it.value)?.groupValues?.get(1)?.lowercase() == "image_src" }
            ?.let { hrefAttribute.find(it.value)?.groupValues?.get(1) }
            ?.let(::unescape)
    }

    private val amazonProduct = Regex(
        "^https?://(?:[\\w-]+\\.)*amazon\\.[a-z.]+/(?:.*/)?(?:dp|gp/product|gp/aw/d)/([A-Z0-9]{10})(?:[/?#].*)?$",
        RegexOption.IGNORE_CASE
    )

    /**
     * Amazon product pages declare no og:image and often answer automated
     * requests with a robot check, so their picture is taken from the product
     * id (ASIN) in the link, through Amazon's own image server.
     */
    fun amazonPicture(link: String): String? =
        amazonProduct.find(link.trim())?.groupValues?.get(1)?.uppercase()?.let { asin ->
            "https://m.media-amazon.com/images/P/$asin.01.L.jpg"
        }

    private fun unescape(value: String): String =
        value.trim()
            .replace("&amp;", "&")
            .replace("&#38;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
}
