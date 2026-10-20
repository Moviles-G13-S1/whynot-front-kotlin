package com.example.whynotkotlin.model

/**
 * Why an attempt to save a product failed (BQ Type 2).
 *
 * Every attempt is recorded in `productSaveEvents`, a success without a reason
 * and a failure with one of these codes. The codes are short and stable so the
 * admin analytics can group failures the same way for Android and iOS.
 */
enum class ProductSaveFailure(val code: String) {
    MISSING_NAME("missing_name"),
    MISSING_BRAND("missing_brand"),
    INVALID_PRICE("invalid_price"),
    NEGATIVE_PRICE("negative_price"),
    MISSING_WISHLIST("missing_wishlist"),
    BACKEND_ERROR("backend_error")
}
