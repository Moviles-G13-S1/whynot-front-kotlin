package com.example.whynotkotlin.view.screens.spending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.CategorySpendingDetailLoad
import com.example.whynotkotlin.model.SpendingSummaryLoad
import com.example.whynotkotlin.model.readableMessage
import com.example.whynotkotlin.view.components.rememberIsOnline
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private fun WhyNotNavigator.spendingNavigation() = SpendingNavigation(
    onHomeClick = ::toHome,
    onWishlistsClick = ::toWishlists,
    onAddClick = ::toAdd,
    onPurchasesClick = ::toPurchases,
    onProfileClick = ::toProfile
)

@Composable
fun SpendingSummaryRoute(navigator: WhyNotNavigator) {
    val controller = LocalAppDependencies.current.spendingController ?: return
    val scope = rememberCoroutineScope()
    val isOnline = rememberIsOnline()

    var selectedMonth by rememberSaveable { mutableStateOf<Int?>(null) }
    var load by remember { mutableStateOf<SpendingSummaryLoad?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var wasOffline by remember { mutableStateOf(!isOnline) }

    fun refresh(force: Boolean) {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                val result = controller.loadSummary(selectedMonth, forceRefresh = force)
                load = result
                selectedMonth = result.month?.yearMonth
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = error.readableMessage()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh(force = false) }

    // Back online after being offline: replace what came from the phone.
    LaunchedEffect(isOnline) {
        if (isOnline && wasOffline) refresh(force = true)
        wasOffline = !isOnline
    }

    SpendingSummaryScreen(
        load = load,
        loading = loading,
        errorMessage = errorMessage,
        isOnline = isOnline,
        onMonthSelected = { month ->
            selectedMonth = month
            refresh(force = false)
        },
        onCategoryClick = navigator::toCategorySpending,
        onRetry = { refresh(force = true) },
        onBackClick = navigator::back,
        navigation = navigator.spendingNavigation()
    )
}

@Composable
fun CategorySpendingDetailRoute(yearMonth: Int, categoryId: String, navigator: WhyNotNavigator) {
    val controller = LocalAppDependencies.current.spendingController ?: return
    val scope = rememberCoroutineScope()
    val isOnline = rememberIsOnline()

    var load by remember { mutableStateOf<CategorySpendingDetailLoad?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                load = controller.loadCategory(yearMonth, categoryId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = error.readableMessage()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(yearMonth, categoryId, isOnline) { refresh() }

    CategorySpendingDetailScreen(
        load = load,
        loading = loading,
        errorMessage = errorMessage,
        isOnline = isOnline,
        onRetry = ::refresh,
        onBackClick = navigator::back,
        navigation = navigator.spendingNavigation()
    )
}
