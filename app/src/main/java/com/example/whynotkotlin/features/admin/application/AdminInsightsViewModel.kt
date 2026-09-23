package com.example.whynotkotlin.features.admin.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.admin.domain.AgeGroupShare
import com.example.whynotkotlin.features.admin.domain.CategoryDemographics
import com.example.whynotkotlin.features.admin.domain.CategoryPurchaseCount
import com.example.whynotkotlin.features.admin.domain.CityShare
import com.example.whynotkotlin.features.admin.domain.DemographicProfileStats
import com.example.whynotkotlin.features.admin.domain.GenderShare
import com.example.whynotkotlin.features.admin.domain.PurchaseMonth
import com.example.whynotkotlin.features.admin.domain.PurchasedProductsStats
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
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

/**
 * Miguel implements BQ4 and BQ6 here.
 *
 * This is a separate ViewModel from [AdminViewModel] on purpose: BQ1/BQ3,
 * BQ2/BQ5 and BQ4/BQ6 belong to three different people, and one shared
 * ViewModel would have all three editing the same file. Both read the same
 * [AdminRepository], so the data path stays single.
 *
 * Every figure is derived in memory from the two collection-wide reads, which
 * the rules only allow to an account carrying the `admin` claim.
 */
class AdminInsightsViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminInsightsUiState())
    val state: StateFlow<AdminInsightsUiState> = _state.asStateFlow()

    // Kept outside the flow so changing the window does not refetch anything.
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

    /**
     * BQ4:
     * Which category has the highest number of products marked as purchased
     * per month?
     */
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

                val byCategory = entries
                    .groupingBy { (product, _) -> product.categoryId }
                    .eachCount()
                    .map { (categoryId, count) ->
                        CategoryPurchaseCount(categoryId, count)
                    }
                    .sortedByDescending { it.purchases }

                val top = byCategory.firstOrNull()

                PurchaseMonth(
                    year = yearMonth.year,
                    month = yearMonth.month,
                    label = yearMonth.label(),
                    totalPurchases = entries.size,
                    topCategoryId = top?.categoryId,
                    topCategoryPurchases = top?.purchases ?: 0,
                    byCategory = byCategory
                )
            }
            // Newest first: no orderBy is possible on the query, so the whole
            // ordering happens here.
            .sortedByDescending { it.year * 100 + it.month }

        val overallTop = inWindow
            .groupingBy { (product, _) -> product.categoryId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return PurchasedProductsStats(
            months = months,
            totalPurchases = inWindow.size,
            undatedPurchases = purchased.size - dated.size,
            overallTopCategoryId = overallTop,
            monthsWindow = window
        )
    }

    /**
     * BQ6:
     * What is the demographic profile of the average buyer per category?
     */
    private fun calculateDemographicProfile(
        products: List<Product>,
        users: List<UserProfile>
    ): DemographicProfileStats {

        val profilesByUid = users.associateBy { it.uid }

        // One entry per (category, buyer) pair, so buying five phones counts
        // the same as buying one.
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

    /**
     * The leading cities, with everything else folded into a single remainder
     * row so the numbers still add up to the buyer count.
     */
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

/**
 * A year and month pair.
 *
 * `java.time` needs API 26 or desugaring and the app targets API 24, so the
 * calendar maths uses [Calendar].
 */
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
