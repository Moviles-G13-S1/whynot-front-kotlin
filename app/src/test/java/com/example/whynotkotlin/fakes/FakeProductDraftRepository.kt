package com.example.whynotkotlin.fakes

import com.example.whynotkotlin.model.ProductFormDraft
import com.example.whynotkotlin.model.repositories.ProductDraftRepository

/** In-memory draft storage keyed by uid, counting writes to check debouncing. */
class FakeProductDraftRepository : ProductDraftRepository {

    val drafts = mutableMapOf<String, ProductFormDraft>()
    var writes = 0

    override suspend fun loadDraft(ownerId: String): ProductFormDraft? = drafts[ownerId]

    override suspend fun saveDraft(ownerId: String, draft: ProductFormDraft) {
        writes++
        drafts[ownerId] = draft
    }

    override suspend fun clearDraft(ownerId: String) {
        writes++
        drafts.remove(ownerId)
    }
}
