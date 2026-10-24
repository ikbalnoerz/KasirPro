package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StockMovementEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KasirDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: KasirDatabase? = null

        fun getDatabase(context: Context): KasirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasirDatabase::class.java,
                    "kasir_pro_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed initial retail products in a background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialProducts(database.productDao())
                }
            }
        }
    }
}

suspend fun seedInitialProducts(dao: ProductDao) {
    val initialProducts = listOf(
        ProductEntity(
            barcode = "899238810111",
            name = "Indomie Goreng Spesial",
            category = "Makanan",
            costPrice = 2800.0,
            sellPrice = 3500.0,
            stock = 45,
            minStockAlert = 10,
            unit = "bungkus"
        ),
        ProductEntity(
            barcode = "899238810222",
            name = "Indomie Kuah Soto Mie",
            category = "Makanan",
            costPrice = 2800.0,
            sellPrice = 3500.0,
            stock = 32,
            minStockAlert = 10,
            unit = "bungkus"
        ),
        ProductEntity(
            barcode = "899886610001",
            name = "Aqua Air Mineral 600ml",
            category = "Minuman",
            costPrice = 2500.0,
            sellPrice = 4000.0,
            stock = 60,
            minStockAlert = 12,
            unit = "botol"
        ),
        ProductEntity(
            barcode = "899886610002",
            name = "Teh Botol Sosro 250ml",
            category = "Minuman",
            costPrice = 3200.0,
            sellPrice = 4500.0,
            stock = 25,
            minStockAlert = 8,
            unit = "kotak"
        ),
        ProductEntity(
            barcode = "899886610003",
            name = "Ultra Milk Cokelat 250ml",
            category = "Minuman",
            costPrice = 5200.0,
            sellPrice = 6500.0,
            stock = 4, // Menipis
            minStockAlert = 6,
            unit = "kotak"
        ),
        ProductEntity(
            barcode = "899100210004",
            name = "Kopi Kenangan Mantancino",
            category = "Minuman",
            costPrice = 7500.0,
            sellPrice = 9500.0,
            stock = 3, // Menipis
            minStockAlert = 5,
            unit = "botol"
        ),
        ProductEntity(
            barcode = "899100110005",
            name = "Minyak Goreng Bimoli 1L",
            category = "Sembako",
            costPrice = 15000.0,
            sellPrice = 18500.0,
            stock = 14,
            minStockAlert = 5,
            unit = "pouch"
        ),
        ProductEntity(
            barcode = "899100110006",
            name = "Beras Ramos Super 5kg",
            category = "Sembako",
            costPrice = 66000.0,
            sellPrice = 76000.0,
            stock = 8,
            minStockAlert = 3,
            unit = "karung"
        ),
        ProductEntity(
            barcode = "899100110007",
            name = "Gula Pasir Gulaku 1kg",
            category = "Sembako",
            costPrice = 14500.0,
            sellPrice = 17500.0,
            stock = 22,
            minStockAlert = 5,
            unit = "kg"
        ),
        ProductEntity(
            barcode = "899100110008",
            name = "Telur Ayam Negeri Fresh 1kg",
            category = "Sembako",
            costPrice = 24000.0,
            sellPrice = 28500.0,
            stock = 15,
            minStockAlert = 4,
            unit = "kg"
        ),
        ProductEntity(
            barcode = "899277510009",
            name = "Chitato Sapi Panggang 68g",
            category = "Snack",
            costPrice = 9200.0,
            sellPrice = 11500.0,
            stock = 18,
            minStockAlert = 5,
            unit = "bungkus"
        ),
        ProductEntity(
            barcode = "899277510010",
            name = "Oreo Vanilla Cream 133g",
            category = "Snack",
            costPrice = 7800.0,
            sellPrice = 9800.0,
            stock = 12,
            minStockAlert = 5,
            unit = "bungkus"
        ),
        ProductEntity(
            barcode = "899554410011",
            name = "Buku Tulis Sinar Dunia 38",
            category = "ATK",
            costPrice = 3000.0,
            sellPrice = 4500.0,
            stock = 30,
            minStockAlert = 10,
            unit = "buku"
        ),
        ProductEntity(
            barcode = "899554410012",
            name = "Pulpen Standard AE7 Hitam",
            category = "ATK",
            costPrice = 1800.0,
            sellPrice = 2500.0,
            stock = 50,
            minStockAlert = 10,
            unit = "pcs"
        )
    )
    dao.insertAll(initialProducts)
}
