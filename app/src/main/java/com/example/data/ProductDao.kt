package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= minStockAlert AND stock > 0 ORDER BY stock ASC")
    fun getLowStockProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= 0 ORDER BY name ASC")
    fun getOutOfStockProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsFlow(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategoryFlow(category: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getCategoriesFlow(): Flow<List<String>>

    @Query("SELECT SUM(stock * costPrice) FROM products")
    fun getTotalInventoryCostValueFlow(): Flow<Double?>

    @Query("SELECT SUM(stock * sellPrice) FROM products")
    fun getTotalInventoryPotentialRevenueFlow(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET stock = stock + :qty, updatedAt = :timestamp WHERE id = :productId")
    suspend fun addStock(productId: Long, qty: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = MAX(0, stock - :qty), updatedAt = :timestamp WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, qty: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = :newStock, updatedAt = :timestamp WHERE id = :productId")
    suspend fun setStock(productId: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface StockMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProductFlow(productId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMovementsFlow(limit: Int = 50): Flow<List<StockMovementEntity>>
}
