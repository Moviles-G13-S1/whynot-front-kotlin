package com.example.whynotkotlin.model.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.whynotkotlin.model.LocalSearchSnapshot
import com.example.whynotkotlin.model.Product
import java.util.Date

/**
 * Local storage for Global Product Search with Room (Jetpack's database layer
 * over SQLite).
 *
 * Why a database and not key-value storage: the search needs the whole list of
 * a user's products as rows (name, brand, category, price...), replaced
 * atomically every time the server confirms a new list. Room gives typed
 * entities, a DAO checked at compile time and transactions, and runs its
 * suspend queries on its own background executor, never on the main thread.
 *
 * Two tables, both keyed by the owner, so two accounts on one phone never mix:
 * - `search_product`: one row per product of each user.
 * - `search_sync`: when each user's rows were last confirmed by the server.
 */
class RoomSearchProductStore(context: Context) : SearchProductStore {

    private val dao: SearchProductDao by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            WhyNotSearchDatabase::class.java,
            DATABASE_NAME
        ).build().searchProductDao()
    }

    override suspend fun load(ownerId: String): LocalSearchSnapshot? {
        val syncedAt = dao.syncedAt(ownerId) ?: return null
        val products = dao.productsOf(ownerId).map { it.toProduct() }
        return LocalSearchSnapshot(products, syncedAt)
    }

    override suspend fun replace(ownerId: String, products: List<Product>, syncedAtMillis: Long) {
        dao.replace(
            ownerId = ownerId,
            rows = products.map { it.toEntity(ownerId) },
            sync = SearchSyncEntity(ownerId, syncedAtMillis)
        )
    }

    override suspend fun clear(ownerId: String) {
        dao.clear(ownerId)
    }

    private companion object {
        const val DATABASE_NAME = "whynot_search.db"
    }
}

@Entity(
    tableName = "search_product",
    primaryKeys = ["ownerId", "id"],
    indices = [Index("ownerId")]
)
data class SearchProductEntity(
    val ownerId: String,
    val id: String,
    val wishlistId: String,
    val categoryId: String,
    val name: String,
    val brand: String,
    val price: Double,
    val imageUrl: String,
    val productUrl: String,
    val purchased: Boolean,
    val createdAtMillis: Long?
)

@Entity(tableName = "search_sync")
data class SearchSyncEntity(
    @PrimaryKey val ownerId: String,
    val syncedAtMillis: Long
)

@Dao
abstract class SearchProductDao {

    @Query("SELECT * FROM search_product WHERE ownerId = :ownerId")
    abstract suspend fun productsOf(ownerId: String): List<SearchProductEntity>

    @Query("SELECT syncedAtMillis FROM search_sync WHERE ownerId = :ownerId")
    abstract suspend fun syncedAt(ownerId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertProducts(rows: List<SearchProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertSync(row: SearchSyncEntity)

    @Query("DELETE FROM search_product WHERE ownerId = :ownerId")
    protected abstract suspend fun deleteProductsOf(ownerId: String)

    @Query("DELETE FROM search_sync WHERE ownerId = :ownerId")
    protected abstract suspend fun deleteSyncOf(ownerId: String)

    /** All or nothing: a half-written list can never be searched. */
    @Transaction
    open suspend fun replace(ownerId: String, rows: List<SearchProductEntity>, sync: SearchSyncEntity) {
        deleteProductsOf(ownerId)
        insertProducts(rows)
        insertSync(sync)
    }

    @Transaction
    open suspend fun clear(ownerId: String) {
        deleteProductsOf(ownerId)
        deleteSyncOf(ownerId)
    }
}

@Database(
    entities = [SearchProductEntity::class, SearchSyncEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WhyNotSearchDatabase : RoomDatabase() {
    abstract fun searchProductDao(): SearchProductDao
}

private fun Product.toEntity(ownerId: String) = SearchProductEntity(
    ownerId = ownerId,
    id = id,
    wishlistId = wishlistId,
    categoryId = categoryId,
    name = name,
    brand = brand,
    price = price,
    imageUrl = imageUrl,
    productUrl = productUrl,
    purchased = purchased,
    createdAtMillis = createdAt?.time
)

private fun SearchProductEntity.toProduct() = Product(
    id = id,
    ownerId = ownerId,
    wishlistId = wishlistId,
    categoryId = categoryId,
    name = name,
    brand = brand,
    price = price,
    imageUrl = imageUrl,
    productUrl = productUrl,
    purchased = purchased,
    purchasedAt = null,
    createdAt = createdAtMillis?.let(::Date),
    updatedAt = null
)
