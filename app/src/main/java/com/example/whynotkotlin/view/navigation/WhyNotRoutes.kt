package com.example.whynotkotlin.view.navigation

import android.net.Uri

object WhyNotRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME = "home"
    const val WISHLISTS = "wishlists"
    const val PURCHASES = "purchases"
    const val PROFILE = "profile"

    const val EDIT_PROFILE =
        "edit_profile"

    const val CHANGE_PASSWORD =
        "change_password"

    const val NEW_WISHLIST =
        "new_wishlist"

    const val ARG_WISHLIST_ID = "wishlistId"

    const val WISHLIST_DETAIL =
        "wishlist_detail/{$ARG_WISHLIST_ID}"

    /** Optional link the manual form opens prefilled with. */
    const val ARG_PRODUCT_URL = "productUrl"

    const val NEW_PRODUCT =
        "new_product?$ARG_PRODUCT_URL={$ARG_PRODUCT_URL}"

    const val ARG_PRODUCT_ID = "productId"

    /** The tab the product was opened from, kept selected in the bottom bar. */
    const val ARG_SOURCE_TAB = "from"

    const val PRODUCT_DETAIL =
        "product_detail/{$ARG_PRODUCT_ID}?$ARG_SOURCE_TAB={$ARG_SOURCE_TAB}"

    const val EDIT_PRODUCT =
        "edit_product/{$ARG_PRODUCT_ID}?$ARG_SOURCE_TAB={$ARG_SOURCE_TAB}"

    fun wishlistDetail(wishlistId: String): String =
        "wishlist_detail/${Uri.encode(wishlistId)}"

    fun productDetail(productId: String, sourceTab: String): String =
        "product_detail/${Uri.encode(productId)}?$ARG_SOURCE_TAB=$sourceTab"

    fun editProduct(productId: String, sourceTab: String): String =
        "edit_product/${Uri.encode(productId)}?$ARG_SOURCE_TAB=$sourceTab"

    fun newProduct(productUrl: String): String =
        "new_product?$ARG_PRODUCT_URL=${Uri.encode(productUrl)}"
}
