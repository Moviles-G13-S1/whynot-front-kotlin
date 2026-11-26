package com.example.whynotkotlin.model.repositories

import android.content.Context
import com.example.whynotkotlin.model.CachedRecommendation
import com.example.whynotkotlin.model.ProductRecommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Local files: one small JSON file per user in the app's private storage,
 * `files/recommendations/<uid>.json`.
 *
 * A file (and not DataStore or a database) because it is a single object per
 * user that is always read and written whole. Every disk access runs on
 * [Dispatchers.IO], and a write goes to a temporary file that then replaces the
 * old one, so a crash mid-write never leaves a half-written recommendation.
 */
class LocalFileRecommendationStore(context: Context) : RecommendationFileStore {

    private val directory = File(context.applicationContext.filesDir, DIRECTORY)

    override suspend fun save(uid: String, recommendation: ProductRecommendation) =
        withContext(Dispatchers.IO) {
            directory.mkdirs()
            val json = JSONObject()
                .put("recommendationEventId", recommendation.recommendationEventId)
                .put("name", recommendation.name)
                .put("brand", recommendation.brand)
                .put("price", recommendation.price)
                .put("imageUrl", recommendation.imageUrl)
                .put("productUrl", recommendation.productUrl)
                .put("categoryId", recommendation.categoryId)
                .put("reason", recommendation.reason)
                .put("storedAtMillis", System.currentTimeMillis())

            val target = fileFor(uid)
            val temporary = File(directory, "${target.name}.tmp")
            temporary.writeText(json.toString())
            if (!temporary.renameTo(target)) {
                target.delete()
                temporary.renameTo(target)
            }
            Unit
        }

    override suspend fun load(uid: String): CachedRecommendation? =
        withContext(Dispatchers.IO) {
            val file = fileFor(uid)
            if (!file.exists()) return@withContext null

            runCatching {
                val json = JSONObject(file.readText())
                CachedRecommendation(
                    recommendation = ProductRecommendation(
                        recommendationEventId = json.getString("recommendationEventId"),
                        name = json.getString("name"),
                        brand = json.getString("brand"),
                        price = json.getDouble("price"),
                        imageUrl = json.optString("imageUrl"),
                        productUrl = json.optString("productUrl"),
                        categoryId = json.getString("categoryId"),
                        reason = json.optString("reason")
                    ),
                    storedAtMillis = json.getLong("storedAtMillis")
                )
            }.getOrNull() // A damaged file is treated as no saved recommendation.
        }

    override suspend fun delete(uid: String) = withContext(Dispatchers.IO) {
        fileFor(uid).delete()
        Unit
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        directory.listFiles()?.forEach { it.delete() }
        Unit
    }

    /** Firebase uids are letters and digits; anything else is dropped from the name. */
    private fun fileFor(uid: String): File =
        File(directory, uid.filter { it.isLetterOrDigit() || it == '-' || it == '_' } + ".json")

    private companion object {
        const val DIRECTORY = "recommendations"
    }
}
