package com.example.whynotkotlin.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.model.repositories.AdminRepository
import com.example.whynotkotlin.model.AgeGroupShare
import com.example.whynotkotlin.model.CategoryDemographics
import com.example.whynotkotlin.model.CategoryPurchaseCount
import com.example.whynotkotlin.model.CityShare
import com.example.whynotkotlin.model.DemographicProfileStats
import com.example.whynotkotlin.model.GenderShare
import com.example.whynotkotlin.model.PurchaseMonth
import com.example.whynotkotlin.model.PurchasedProductsStats
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AdminInsightsUiState(
    val purchasedProductsStats: PurchasedProductsStats = PurchasedProductsStats(),
    val demographicProfileStats: DemographicProfileStats = DemographicProfileStats(),
    val loading: Boolean = true,
    val errorMessage: String? = null
)

class AdminInsightsViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminInsightsUiState())
    val state: StateFlow<AdminInsightsUiState> = _state.asStateFlow()

    private val monthsWindow =
        MutableStateFlow(PurchasedProductsStats.DEFAULT_MONTHS_WINDOW)

    init {
        observeInsights()
    }

    private fun observeInsights() {
        viewModelScope.launch {
            combine(
                repository.observeAllProducts(),
                repository.observeAllUsers(),
                monthsWindow
            ) { products, users, window ->

                AdminInsightsUiState(
                    purchasedProductsStats =
                        calculatePurchasedProductsStats(products, window),
                    demographicProfileStats =
                        calculateDemographicProfile(products, users),
                    loading = false,
                    errorMessage = null
                )
            }
                .catch { exception ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = exception.message
                            ?: "Unable to load admin insights."
                    )
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    fun selectMonthsWindow(months: Int) {
        monthsWindow.value = months
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun calculatePurchasedProductsStats(
        products: List<Product>,
        window: Int
    ): PurchasedProductsStats {
        val purchased = products.filter { it.purchased }

        val dated = purchased.mapNotNull { product ->
            product.purchasedAt?.let { product to it.yearMonth() }
        }

        val oldestAllowed = monthsAgo(window - 1)

        val inWindow = dated.filter { (_, yearMonth) ->
            yearMonth >= oldestAllowed
        }

        val months = inWindow
            .groupBy { (_, yearMonth) -> yearMonth }
            .map { (yearMonth, entries) ->

                val counts = entries
                    .groupingBy { (product, _) -> product.categoryId }
                    .eachCount()

                val byCategory = counts
                    .map { (categoryId, count) ->
                        CategoryPurchaseCount(categoryId, count)
                    }
                    .sortedWith(
                        compareByDescending<CategoryPurchaseCount> { it.purchases }
                            .thenBy { it.categoryId }
                    )

                PurchaseMonth(
                    year = yearMonth.year,
                    month = yearMonth.month,
                    label = yearMonth.label(),
                    totalPurchases = entries.size,
                    topCategoryIds = categoriesTiedAtMax(counts),
                    topCategoryPurchases = counts.values.maxOrNull() ?: 0,
                    byCategory = byCategory
                )
            }

            .sortedByDescending { it.year * 100 + it.month }

        val overallCounts = inWindow
            .groupingBy { (product, _) -> product.categoryId }
            .eachCount()

        return PurchasedProductsStats(
            months = months,
            totalPurchases = inWindow.size,
            undatedPurchases = purchased.size - dated.size,
            overallTopCategoryIds = categoriesTiedAtMax(overallCounts),
            monthsWindow = window
        )
    }

    private fun categoriesTiedAtMax(counts: Map<String, Int>): List<String> {
        val max = counts.values.maxOrNull() ?: return emptyList()

        return counts
            .filterValues { it == max }
            .keys
            .sorted()
    }

    private fun calculateDemographicProfile(
        products: List<Product>,
        users: List<UserProfile>
    ): DemographicProfileStats {
        val profilesByUid = users.associateBy { it.uid }

        val buyersByCategory = products
            .filter { it.purchased }
            .groupBy { it.categoryId }
            .mapValues { (_, categoryProducts) ->
                categoryProducts
                    .map { it.ownerId }
                    .distinct()
                    .mapNotNull { profilesByUid[it] }
            }
            .filterValues { it.isNotEmpty() }

        val categories = buyersByCategory
            .map { (categoryId, buyers) ->
                CategoryDemographics(
                    categoryId = categoryId,
                    buyers = buyers.size,
                    medianAge = medianAge(buyers),
                    ageGroups = ageGroups(buyers),
                    genders = genderShares(buyers),
                    topCities = topCities(buyers)
                )
            }
            .sortedByDescending { it.buyers }

        val distinctBuyers = products
            .filter { it.purchased }
            .map { it.ownerId }
            .distinct()
            .mapNotNull { profilesByUid[it] }

        return DemographicProfileStats(
            categories = categories,
            totalBuyers = distinctBuyers.size,
            profilesWithoutCity = distinctBuyers.count { it.cityId.isBlank() }
        )
    }

    private fun medianAge(buyers: List<UserProfile>): Int? {
        val ages = buyers.map { it.age }.filter { it > 0 }.sorted()

        if (ages.isEmpty()) return null

        val middle = ages.size / 2

        return if (ages.size % 2 == 1) {
            ages[middle]
        } else {
            (ages[middle - 1] + ages[middle]) / 2
        }
    }

    private fun ageGroups(buyers: List<UserProfile>): List<AgeGroupShare> =
        DemographicProfileStats.AGE_GROUPS.map { (label, range) ->
            AgeGroupShare(
                label = label,
                userCount = buyers.count { it.age in range }
            )
        }

    private fun genderShares(buyers: List<UserProfile>): List<GenderShare> {
        val total = buyers.size

        return buyers
            .groupingBy { it.gender.ifBlank { "Unknown" } }
            .eachCount()
            .map { (gender, count) ->
                GenderShare(
                    gender = gender,
                    userCount = count,
                    percentage =
                        if (total == 0) 0.0 else count * 100.0 / total
                )
            }
            .sortedByDescending { it.userCount }
    }

    private fun topCities(buyers: List<UserProfile>): List<CityShare> {
        val counts = buyers
            .groupingBy {
                it.cityId.ifBlank { DemographicProfileStats.UNKNOWN_CITY }
            }
            .eachCount()
            .map { (cityId, count) -> CityShare(cityId, count) }
            .sortedByDescending { it.userCount }

        if (counts.size <= DemographicProfileStats.TOP_CITIES) return counts

        val leading = counts.take(DemographicProfileStats.TOP_CITIES)
        val rest = counts.drop(DemographicProfileStats.TOP_CITIES)

        return leading + CityShare(
            cityId = "other",
            userCount = rest.sumOf { it.userCount }
        )
    }
}

private data class YearMonth(
    val year: Int,
    val month: Int
) : Comparable<YearMonth> {
    override fun compareTo(other: YearMonth): Int =
        (year * 100 + month) - (other.year * 100 + other.month)

    fun label(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val monthName = calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale.getDefault()
        ).orEmpty()

        return "$monthName $year"
    }
}

private fun Date.yearMonth(): YearMonth {
    val calendar = Calendar.getInstance().also { it.time = this }

    return YearMonth(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1
    )
}

private fun monthsAgo(months: Int): YearMonth {
    val calendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, -months)
    }

    return YearMonth(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1
    )
}
