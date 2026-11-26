package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.RecommendationCache
import com.example.whynotkotlin.model.UserFacingException
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.RecommendationFileStore
import com.example.whynotkotlin.model.repositories.RecommendationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * What the Home screen shows for the recommendation.
 *
 * [isStale] marks a cached recommendation shown because the backend could not
 * be reached (offline, or the request failed), so the view can say it may be
 * outdated. [fromCache] is true whenever no network call was made.
 */
data class RecommendationLoad(
    val recommendation: ProductRecommendation?,
    val isStale: Boolean = false,
    val fromCache: Boolean = false
)

/** Offline with nothing cached yet for this user. */
class RecommendationOfflineException :
    UserFacingException("You are offline and there is no saved recommendation yet.")

/**
 * Controls the recommendation flow presented to the user.
 *
 * Recommendations go through [cache] first, so returning to Home does not call
 * the backend again while the cached one is fresh. Offline, or when the
 * backend fails, the last cached recommendation is shown marked as stale.
 *
 * [fileStore] keeps the last recommendation on disk as well (local storage), so
 * after the app is closed and opened again without internet the user still
 * sees it, marked as stale. File work never runs on the caller's thread:
 * the store moves it to Dispatchers.IO, and the clean-ups on profile change and
 * sign-out run in [backgroundScope].
 *
 * This controller contains no fake data. It can be tested with
 * FakeRecommendationRepository, located exclusively under src/test.
 */
class RecommendationController(
    private val repository: RecommendationRepository,
    private val cache: RecommendationCache,
    private val connectivity: ConnectivityRepository,
    private val currentUid: () -> String?,
    private val fileStore: RecommendationFileStore? = null,
    private val backgroundScope: CoroutineScope? = null
) {

    suspend fun loadRecommendation(forceRefresh: Boolean = false): RecommendationLoad {
        val uid = currentUid()
            ?: return RecommendationLoad(repository.getRecommendation())

        if (!forceRefresh) {
            cache.getFresh(uid)?.let {
                return RecommendationLoad(it, fromCache = true)
            }
        }

        if (!connectivity.isOnline.value) {
            return staleFor(uid) ?: throw RecommendationOfflineException()
        }

        val fetched = try {
            repository.getRecommendation()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            return staleFor(uid) ?: throw error
        }

        if (fetched == null) {
            return staleFor(uid) ?: RecommendationLoad(null)
        }

        cache.put(uid, fetched)
        persist(uid, fetched)
        return RecommendationLoad(fetched)
    }

    /**
     * Saves [recommendation] into a wishlist and tells whether it ended up saved.
     *
     * recommendationEventId is preserved because the backend uses it
     * to identify that the saved product originally came from the
     * recommendation feature. A product saved earlier counts as saved.
     * Once saved, the cached recommendation is dropped so the next load asks
     * the backend for a new one.
     */
    suspend fun saveRecommendation(
        recommendation: ProductRecommendation,
        wishlistId: String
    ): Boolean {
        val result = repository.saveRecommendedProduct(
            recommendationEventId = recommendation.recommendationEventId,
            wishlistId = wishlistId
        )

        val saved = result.saved || result.alreadySaved
        if (saved) {
            currentUid()?.let { uid ->
                cache.invalidate(uid, reason = "saved")
                runFileWork { fileStore?.delete(uid) }
            }
        }

        return saved
    }

    /** The recommendation depends on the profile, so a new profile needs a new one. */
    fun onProfileUpdated(uid: String) {
        cache.invalidate(uid, reason = "profile_updated")
        backgroundScope?.launch { runFileWork { fileStore?.delete(uid) } }
    }

    /** Called on sign-out so the next user never sees this user's cache. */
    fun onSignedOut() {
        cache.clear(reason = "sign_out")
        backgroundScope?.launch { runFileWork { fileStore?.clearAll() } }
    }

    /**
     * The offline fallback: the stale entry in memory, or, after a restart,
     * the one saved on disk.
     */
    private suspend fun staleFor(uid: String): RecommendationLoad? {
        val cached = cache.getStale(uid)
            ?: runFileWork { fileStore?.load(uid) }
            ?: return null
        return RecommendationLoad(cached.recommendation, isStale = true, fromCache = true)
    }

    private suspend fun persist(uid: String, recommendation: ProductRecommendation) {
        runFileWork { fileStore?.save(uid, recommendation) }
    }

    /** Disk problems must never break the recommendation shown on screen. */
    private suspend fun <T> runFileWork(block: suspend () -> T?): T? =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
}
