package com.example.whynotkotlin.view.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.AdminDataset
import com.example.whynotkotlin.model.AdminInsights
import com.example.whynotkotlin.model.PurchasedProductsStats
import com.example.whynotkotlin.model.SavedProductsOverview
import com.example.whynotkotlin.model.UserActivityMetrics
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

private const val MONTHS_WINDOW_KEY = "admin_months_window"

@Composable
fun AdminSavedProductsRoute(navigator: WhyNotNavigator) {
    val overviewResult by LocalAppDependencies.current
        .adminSavedProductsController
        .overview
        .collectAsStateWithLifecycle()

    AdminSavedProductsScreen(
        overview = overviewResult?.getOrNull() ?: SavedProductsOverview(),
        loading = overviewResult == null,
        errorMessage = overviewResult?.exceptionOrNull()?.adminMessage("Unable to load admin metrics."),
        onRecommendedSavesClick = navigator::toAdminRecommendedSaves,
        onRepeatSaversClick = navigator::toAdminRepeatSavers,
        onPurchasesByCategoryClick = navigator::toAdminPurchasesByCategory,
        onZeroProductsClick = navigator::toAdminZeroProducts,
        onDemographicProfileClick = navigator::toAdminDemographicProfile
    )
}

@Composable
fun AdminRecommendedSavesRoute(navigator: WhyNotNavigator) {
    val overviewResult by LocalAppDependencies.current
        .adminSavedProductsController
        .overview
        .collectAsStateWithLifecycle()

    AdminRecommendedSavesScreen(
        overview = overviewResult?.getOrNull() ?: SavedProductsOverview(),
        loading = overviewResult == null,
        errorMessage = overviewResult?.exceptionOrNull()?.adminMessage("Unable to load admin metrics."),
        onSavedProductsClick = navigator::toAdminSavedProducts,
        onRepeatSaversClick = navigator::toAdminRepeatSavers,
        onPurchasesByCategoryClick = navigator::toAdminPurchasesByCategory,
        onZeroProductsClick = navigator::toAdminZeroProducts,
        onDemographicProfileClick = navigator::toAdminDemographicProfile
    )
}

@Composable
fun AdminRepeatSaversRoute(navigator: WhyNotNavigator) {
    val metricsResult by LocalAppDependencies.current
        .adminUserMetricsController
        .metrics
        .collectAsStateWithLifecycle()

    AdminRepeatSaversScreen(
        metrics = metricsResult?.getOrNull() ?: UserActivityMetrics(),
        loading = metricsResult == null,
        errorMessage = metricsResult?.exceptionOrNull()?.adminMessage("Unable to load user metrics."),
        onSavedProductsClick = navigator::toAdminSavedProducts,
        onRecommendedSavesClick = navigator::toAdminRecommendedSaves,
        onPurchasesByCategoryClick = navigator::toAdminPurchasesByCategory,
        onZeroProductsClick = navigator::toAdminZeroProducts,
        onDemographicProfileClick = navigator::toAdminDemographicProfile
    )
}

@Composable
fun AdminZeroProductsRoute(navigator: WhyNotNavigator) {
    val metricsResult by LocalAppDependencies.current
        .adminUserMetricsController
        .metrics
        .collectAsStateWithLifecycle()

    AdminZeroProductsScreen(
        metrics = metricsResult?.getOrNull() ?: UserActivityMetrics(),
        loading = metricsResult == null,
        errorMessage = metricsResult?.exceptionOrNull()?.adminMessage("Unable to load user metrics."),
        onSavedProductsClick = navigator::toAdminSavedProducts,
        onRepeatSaversClick = navigator::toAdminRepeatSavers,
        onRecommendedSavesClick = navigator::toAdminRecommendedSaves,
        onPurchasesByCategoryClick = navigator::toAdminPurchasesByCategory,
        onDemographicProfileClick = navigator::toAdminDemographicProfile
    )
}

@Composable
fun AdminPurchasesByCategoryRoute(navigator: WhyNotNavigator) {
    // The window lives on the admin graph so it survives moving between tabs.
    val adminGraphState = remember(navigator) { navigator.adminGraphState() }

    val monthsWindow by adminGraphState
        .getStateFlow(MONTHS_WINDOW_KEY, PurchasedProductsStats.DEFAULT_MONTHS_WINDOW)
        .collectAsStateWithLifecycle()

    AdminInsightsHost(monthsWindow) { insights, loading, errorMessage ->
        AdminPurchasesByCategoryScreen(
            insights = insights,
            loading = loading,
            errorMessage = errorMessage,
            onSavedProductsClick = navigator::toAdminSavedProducts,
            onRepeatSaversClick = navigator::toAdminRepeatSavers,
            onRecommendedSavesClick = navigator::toAdminRecommendedSaves,
            onZeroProductsClick = navigator::toAdminZeroProducts,
            onDemographicProfileClick = navigator::toAdminDemographicProfile,
            onWindowChange = { months -> adminGraphState[MONTHS_WINDOW_KEY] = months }
        )
    }
}

@Composable
fun AdminDemographicProfileRoute(navigator: WhyNotNavigator) {
    AdminInsightsHost(PurchasedProductsStats.DEFAULT_MONTHS_WINDOW) { insights, loading, errorMessage ->
        AdminDemographicProfileScreen(
            insights = insights,
            loading = loading,
            errorMessage = errorMessage,
            onSavedProductsClick = navigator::toAdminSavedProducts,
            onRepeatSaversClick = navigator::toAdminRepeatSavers,
            onRecommendedSavesClick = navigator::toAdminRecommendedSaves,
            onPurchasesByCategoryClick = navigator::toAdminPurchasesByCategory,
            onZeroProductsClick = navigator::toAdminZeroProducts
        )
    }
}

@Composable
private fun AdminInsightsHost(
    monthsWindow: Int,
    content: @Composable (insights: AdminInsights, loading: Boolean, errorMessage: String?) -> Unit
) {
    val insightsController = LocalAppDependencies.current.adminInsightsController
    val datasetResult by insightsController.dataset.collectAsStateWithLifecycle()

    val dataset = datasetResult?.getOrNull()

    val insights = remember(dataset, monthsWindow) {
        insightsController.insights(dataset ?: AdminDataset(), monthsWindow)
    }

    content(
        insights,
        datasetResult == null,
        datasetResult?.exceptionOrNull()?.adminMessage("Unable to load admin insights.")
    )
}

/** The admin screens show the backend message as is, with a fallback. */
private fun Throwable.adminMessage(fallback: String): String =
    message ?: fallback
