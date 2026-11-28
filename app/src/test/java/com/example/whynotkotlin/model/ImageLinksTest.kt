package com.example.whynotkotlin.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageLinksTest {

    @Test fun `http links are upgraded to https and others are kept`() {
        assertEquals("https://cdn.shop.com/a.jpg", ImageLinks.secure("  http://cdn.shop.com/a.jpg "))
        assertEquals("https://cdn.shop.com/a.jpg", ImageLinks.secure("HTTP://cdn.shop.com/a.jpg"))
        assertEquals("https://cdn.shop.com/a.jpg", ImageLinks.secure("https://cdn.shop.com/a.jpg"))
        assertEquals("", ImageLinks.secure("   "))
    }

    @Test fun `og image is taken from a product page`() {
        val html = """<html><head><title>Shoe</title>
            <meta property="og:title" content="Shoe">
            <meta property="og:image" content="https://img.shop.com/shoe.jpg?w=800&amp;h=800" />
            </head></html>"""
        assertEquals("https://img.shop.com/shoe.jpg?w=800&h=800", ImageLinks.pagePicture(html))
    }

    @Test fun `secure og image wins and attribute order does not matter`() {
        val html = """<meta content="http://img.shop.com/plain.jpg" property="og:image">
            <meta content='https://img.shop.com/secure.jpg' property='og:image:secure_url'>"""
        assertEquals("https://img.shop.com/secure.jpg", ImageLinks.pagePicture(html))
    }

    @Test fun `twitter card and image_src are fallbacks`() {
        assertEquals("/img/t.png", ImageLinks.pagePicture("""<meta name="twitter:image" content="/img/t.png">"""))
        assertEquals("https://x.co/i.png", ImageLinks.pagePicture("""<link rel="image_src" href="https://x.co/i.png">"""))
    }

    @Test fun `amazon product links give the picture of their product id`() {
        val expected = "https://m.media-amazon.com/images/P/B09B8V1LZ3.01.L.jpg"
        assertEquals(expected, ImageLinks.amazonPicture("https://www.amazon.com/dp/B09B8V1LZ3"))
        assertEquals(expected, ImageLinks.amazonPicture("https://www.amazon.com/Echo-Dot/dp/B09B8V1LZ3/ref=sr_1_1?keywords=echo"))
        assertEquals(expected, ImageLinks.amazonPicture("https://www.amazon.com.mx/gp/product/b09b8v1lz3"))
        assertEquals(expected, ImageLinks.amazonPicture("https://amazon.es/gp/aw/d/B09B8V1LZ3?th=1"))
        assertNull(ImageLinks.amazonPicture("https://www.falabella.com.co/falabella-co/product/123"))
        assertNull(ImageLinks.amazonPicture("https://www.amazon.com/s?k=shoes"))
    }

    @Test fun `a page without a declared picture gives null`() {
        assertNull(ImageLinks.pagePicture("<html><head><meta name=\"description\" content=\"x\"></head></html>"))
    }
}
