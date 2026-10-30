package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.*
import com.example.whynotkotlin.model.repositories.*
import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingAndWishlistTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth : AuthRepository {
        val user = AuthUser("uid", "user@example.com")
        var signedUpWith: String? = null
        var deleted = false
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user
        override suspend fun signUp(email: String, password: String): AuthUser {
            signedUpWith = email
            return user
        }
        override suspend fun signOut() = Unit
        override suspend fun deleteCurrentUser() {
            deleted = true
        }
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
    }

    private class Users : UserRepository {
        var created = false
        var failure: Exception? = null
        override fun observeProfile(uid: String) = flowOf<UserProfile?>(null)
        override suspend fun getProfile(uid: String): UserProfile? = null
        override suspend fun createProfile(uid: String, email: String, draft: UserProfileDraft) {
            failure?.let { throw it }
            created = true
        }
        override suspend fun updateProfile(uid: String, update: UserProfileUpdate) = Unit
    }

    private class Onboarding : OnboardingRepository {
        val steps = mutableListOf<Pair<String, OnboardingStep>>()
        var failure: Exception? = null
        override suspend fun recordStep(userId: String, step: OnboardingStep) {
            failure?.let { throw it }
            steps += userId to step
        }
    }

    private class Lists(existing: List<Wishlist> = emptyList()) : WishlistRepository {
        val wishlists = existing.toMutableList()
        val categories = listOf(Category("tech", "Technology"), Category("beauty", "Beauty"))
        override suspend fun getCategories() = categories
        override fun observeCategories() = flowOf(categories)
        override fun observeWishlists(ownerId: String) = flowOf(wishlists.toList())
        override suspend fun getWishlist(wishlistId: String): Wishlist? = null
        override suspend fun createWishlist(ownerId: String, draft: WishlistDraft): String {
            wishlists += wishlist(draft.categoryId, draft.imageUrl)
            return "wishlist-${wishlists.size}"
        }
    }

    private class Products : ProductRepository {
        override fun observeProducts(ownerId: String) = flowOf(emptyList<Product>())
        override fun observeWishlistProducts(ownerId: String, wishlistId: String) = flowOf(emptyList<Product>())
        override fun observeProduct(productId: String) = flowOf<Product?>(null)
        override suspend fun getProduct(productId: String): Product? = null
        override suspend fun createProduct(ownerId: String, draft: ProductDraft) = "unused"
        override suspend fun updateProduct(productId: String, update: ProductUpdate) = Unit
        override suspend fun markPurchased(productId: String) = Unit
        override suspend fun deleteProduct(productId: String) = Unit
        override suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) = Unit
        override suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double) = Unit
    }

    private suspend fun AuthController.register(
        email: String = "new@example.com",
        password: String = "Wishlist2026",
        confirmPassword: String = password,
        name: String = "New User",
        age: String = "24"
    ) = signUp(name, email, password, confirmPassword, "Female", age, "tech", "bogota")

    // --- Sign-up -------------------------------------------------------------

    @Test fun `a completed sign-up records the onboarding step`() = runTest(dispatcher) {
        val onboarding = Onboarding()
        val users = Users()
        val controller = AuthController(Auth(), users, onboarding, backgroundScope)

        controller.register()
        runCurrent()

        assertTrue(users.created)
        assertEquals(listOf("uid" to OnboardingStep.SIGN_UP_COMPLETED), onboarding.steps)
    }

    @Test fun `a weak password stops sign-up and records nothing`() = runTest(dispatcher) {
        val onboarding = Onboarding()
        val auth = Auth()
        val controller = AuthController(auth, Users(), onboarding, backgroundScope)

        val error = runCatching { controller.register(password = "wishlist") }.exceptionOrNull()
        runCurrent()

        assertEquals("The password needs at least one uppercase letter", error?.message)
        assertNull(auth.signedUpWith)
        assertTrue(onboarding.steps.isEmpty())
    }

    @Test fun `a malformed email is rejected before the backend`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        val error = runCatching { controller.register(email = "not-an-email") }.exceptionOrNull()

        assertEquals("Enter a valid email, such as name@example.com", error?.message)
        assertNull(auth.signedUpWith)
    }

    @Test fun `an age outside 13 to 120 is rejected before the backend`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        val young = runCatching { controller.register(age = "12") }.exceptionOrNull()
        val old = runCatching { controller.register(age = "121") }.exceptionOrNull()

        assertEquals("Age must be between 13 and 120", young?.message)
        assertEquals("Age must be between 13 and 120", old?.message)
        assertNull(auth.signedUpWith)
    }

    @Test fun `a name with emojis, only numbers or symbols is rejected`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        val emoji = runCatching { controller.register(name = "Ana \uD83D\uDE00") }.exceptionOrNull()
        val numbers = runCatching { controller.register(name = "12345") }.exceptionOrNull()
        val script = runCatching { controller.register(name = "<script>") }.exceptionOrNull()

        assertEquals("Your name cannot contain emojis", emoji?.message)
        assertEquals("Your name needs at least one letter", numbers?.message)
        assertEquals("Use only letters, numbers, spaces, hyphens or apostrophes", script?.message)
        assertNull(auth.signedUpWith)
    }

    @Test fun `the confirmation must match the password`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        val error = runCatching {
            controller.register(password = "Wishlist2026", confirmPassword = "Wishlist2025")
        }.exceptionOrNull()

        assertEquals("The passwords do not match", error?.message)
        assertNull(auth.signedUpWith)
    }

    @Test fun `a password with the user's name is rejected`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        val error = runCatching {
            controller.register(name = "Valentina", password = "Valentina2026")
        }.exceptionOrNull()

        assertEquals("The password cannot include your name or email", error?.message)
        assertNull(auth.signedUpWith)
    }

    @Test fun `the email is sent in lowercase`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), Onboarding(), backgroundScope)

        controller.register(email = "  New.User@Example.COM ")

        assertEquals("new.user@example.com", auth.signedUpWith)
    }

    @Test fun `a failed profile removes the new account so the email stays free`() = runTest(dispatcher) {
        val auth = Auth()
        val users = Users().apply { failure = IllegalStateException("PERMISSION_DENIED") }
        val onboarding = Onboarding()
        val controller = AuthController(auth, users, onboarding, backgroundScope)

        val error = runCatching { controller.register() }.exceptionOrNull()
        runCurrent()

        assertEquals("PERMISSION_DENIED", error?.message)
        assertTrue(auth.deleted)
        assertFalse(users.created)
        assertTrue(onboarding.steps.isEmpty())
    }

    @Test fun `failing to record the step does not fail sign-up`() = runTest(dispatcher) {
        val onboarding = Onboarding().apply { failure = IllegalStateException("offline") }
        val users = Users()
        val controller = AuthController(Auth(), users, onboarding, backgroundScope)

        controller.register()
        runCurrent()

        assertTrue(users.created)
    }

    // --- Wishlists -----------------------------------------------------------

    private fun TestScope.wishlistController(lists: Lists, onboarding: Onboarding = Onboarding()) =
        WishlistController(Auth(), lists, Products(), onboarding, backgroundScope)

    @Test fun `creating a wishlist offline is refused and nothing is sent`() = runTest(dispatcher) {
        val lists = Lists()
        val controller = WishlistController(
            Auth(), lists, Products(), Onboarding(), backgroundScope,
            FakeConnectivityRepository(initiallyOnline = false)
        )

        val error = runCatching { controller.createWishlist("tech", "") }.exceptionOrNull()

        assertTrue(error is OfflineException)
        assertEquals(WishlistController.CREATE_OFFLINE, error?.message)
        assertTrue(lists.wishlists.isEmpty())
    }

    @Test fun `the first wishlist records the onboarding step`() = runTest(dispatcher) {
        val onboarding = Onboarding()
        val lists = Lists()

        wishlistController(lists, onboarding).createWishlist("tech", "")
        runCurrent()

        assertEquals(1, lists.wishlists.size)
        assertEquals(listOf("uid" to OnboardingStep.FIRST_WISHLIST_CREATED), onboarding.steps)
    }

    @Test fun `later wishlists do not record the step again`() = runTest(dispatcher) {
        val onboarding = Onboarding()
        val lists = Lists(listOf(wishlist("tech")))

        wishlistController(lists, onboarding).createWishlist("beauty", "")
        runCurrent()

        assertEquals(2, lists.wishlists.size)
        assertTrue(onboarding.steps.isEmpty())
    }

    @Test fun `a second wishlist in the same category is rejected`() = runTest(dispatcher) {
        val lists = Lists(listOf(wishlist("tech")))

        val error = runCatching {
            wishlistController(lists).createWishlist("tech", "")
        }.exceptionOrNull()

        assertEquals("You already have a Technology wishlist", error?.message)
        assertEquals(1, lists.wishlists.size)
    }

    @Test fun `an unknown category is rejected`() = runTest(dispatcher) {
        val lists = Lists()

        val error = runCatching {
            wishlistController(lists).createWishlist("cars", "")
        }.exceptionOrNull()

        assertEquals("That category is no longer available", error?.message)
        assertTrue(lists.wishlists.isEmpty())
    }

    @Test fun `a picture link must be a web address`() = runTest(dispatcher) {
        val lists = Lists()
        val controller = wishlistController(lists)

        val error = runCatching { controller.createWishlist("tech", "my photo") }.exceptionOrNull()
        assertEquals("The picture link must start with http:// or https://", error?.message)

        val noHost = runCatching { controller.createWishlist("tech", "https://cover") }.exceptionOrNull()
        assertEquals("The picture link is not a valid web address", noHost?.message)

        controller.createWishlist("tech", "  https://example.com/cover.png ")
        assertEquals("https://example.com/cover.png", lists.wishlists.single().imageUrl)
    }
}

private fun wishlist(categoryId: String, imageUrl: String = "") =
    Wishlist("w-$categoryId", "uid", categoryId, imageUrl, null, null)
