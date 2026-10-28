package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.ProductFormDraft

/**
 * Local storage for the unsaved New Product form, one draft per user.
 *
 * It works without a connection and survives closing the app. Drafts are kept
 * apart from Firestore on purpose: a draft is never shown or counted as a
 * saved product.
 */
interface ProductDraftRepository {

    /** The draft [ownerId] left, or null when there is none. */
    suspend fun loadDraft(ownerId: String): ProductFormDraft?

    /** Replaces the draft of [ownerId]. */
    suspend fun saveDraft(ownerId: String, draft: ProductFormDraft)

    /** Removes the draft of [ownerId], for example once the product is saved. */
    suspend fun clearDraft(ownerId: String)
}
