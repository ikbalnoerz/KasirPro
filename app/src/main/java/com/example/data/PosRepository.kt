package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val subtotal: Double get() = product.sellPrice * quantity
    val totalCost: Double get() = product.costPrice * quantity
}

data class TransactionDetail(
    val transaction: TransactionEntity,
    val items: List<TransactionItemEntity>
)

data class SalesSummary(
    val totalSalesAmount: Double,
    val totalTransactionsCount: Int,
    val estimatedGrossProfit: Double,
    val qrisAmount: Double,
    val cashAmount: Double,
    val transferAmount: Double
)

class PosRepository(private val database: KasirDatabase) {
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val stockMovementDao = database.stockMovementDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProductsFlow()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProductsFlow()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val topProducts: Flow<List<TopProductSummary>> = transactionDao.getTopProductsFlow()
    val recentStockMovements: Flow<List<StockMovementEntity>> = stockMovementDao.getRecentMovementsFlow(50)
    val totalInventoryCostValue: Flow<Double?> = productDao.getTotalInventoryCostValueFlow()

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        productDao.searchProductsFlow(query)

    suspend fun checkAndSeedIfEmpty() {
        val current = productDao.getAllProductsFlow().firstOrNull()
        if (current.isNullOrEmpty()) {
            seedInitialProducts(productDao)
        }
    }

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: ProductEntity): Long {
        val id = productDao.insert(product)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = id,
                productName = product.name,
                type = StockMovementType.INITIAL,
                quantityChange = product.stock,
                previousStock = 0,
                newStock = product.stock,
                notes = "Produk baru didaftarkan"
            )
        )
        return id
    }

    suspend fun updateProduct(product: ProductEntity) {
        val existing = productDao.getProductById(product.id)
        productDao.update(product)
        if (existing != null && existing.stock != product.stock) {
            val delta = product.stock - existing.stock
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    productId = product.id,
                    productName = product.name,
                    type = StockMovementType.ADJUSTMENT,
                    quantityChange = delta,
                    previousStock = existing.stock,
                    newStock = product.stock,
                    notes = "Penyesuaian stok manual"
                )
            )
        }
    }

    suspend fun deleteProduct(product: ProductEntity) =
        productDao.delete(product)

    suspend fun addStock(productId: Long, qty: Int, note: String = "Restock barang") {
        val product = productDao.getProductById(productId) ?: return
        val prevStock = product.stock
        val newStock = prevStock + qty
        productDao.addStock(productId, qty)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = product.name,
                type = StockMovementType.RESTOCK,
                quantityChange = qty,
                previousStock = prevStock,
                newStock = newStock,
                notes = note
            )
        )
    }

    suspend fun setStock(productId: Long, newStock: Int, note: String = "Penyesuaian opname stok") {
        val product = productDao.getProductById(productId) ?: return
        val prevStock = product.stock
        val delta = newStock - prevStock
        productDao.setStock(productId, newStock)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = product.name,
                type = StockMovementType.ADJUSTMENT,
                quantityChange = delta,
                previousStock = prevStock,
                newStock = newStock,
                notes = note
            )
        )
    }

    suspend fun getTransactionDetail(transactionId: Long): TransactionDetail? {
        val trx = transactionDao.getTransactionById(transactionId) ?: return null
        val items = transactionDao.getItemsForTransaction(transactionId)
        return TransactionDetail(trx, items)
    }

    suspend fun getTransactionWithItems(transactionId: Long): TransactionWithItems? {
        return transactionDao.getTransactionWithItemsById(transactionId)
    }

    /**
     * Executes checkout: records transaction, saves items, decreases inventory stock,
     * and records stock movement logs in an atomic transaction.
     */
    suspend fun executeCheckout(
        items: List<CartItem>,
        paymentMethod: String, // "QRIS", "TUNAI", "TRANSFER"
        subtotal: Double,
        discount: Double,
        tax: Double,
        totalAmount: Double,
        cashTendered: Double,
        changeAmount: Double,
        cashierName: String = "Kasir 01",
        customerNote: String = ""
    ): TransactionDetail {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomSuffix = (1000..9999).random()
        val receiptNumber = "TRX-$dateStr-$randomSuffix"

        val transaction = TransactionEntity(
            receiptNumber = receiptNumber,
            timestamp = System.currentTimeMillis(),
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            cashTendered = cashTendered,
            changeAmount = changeAmount,
            cashierName = cashierName,
            customerNote = customerNote,
            paymentType = PaymentType.fromString(paymentMethod),
            status = TransactionStatus.COMPLETED
        )

        var createdTrxId = 0L
        val itemEntities = mutableListOf<TransactionItemEntity>()

        database.withTransaction {
            createdTrxId = transactionDao.insertTransaction(transaction)

            for (cartItem in items) {
                val itemEntity = TransactionItemEntity(
                    transactionId = createdTrxId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    costPrice = cartItem.product.costPrice,
                    sellPrice = cartItem.product.sellPrice,
                    quantity = cartItem.quantity,
                    subtotal = cartItem.subtotal
                )
                itemEntities.add(itemEntity)

                // Decrease stock
                productDao.decreaseStock(cartItem.product.id, cartItem.quantity)

                // Audit log stock movement
                val prevStock = cartItem.product.stock
                val newStock = (prevStock - cartItem.quantity).coerceAtLeast(0)
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        type = StockMovementType.SALE,
                        quantityChange = -cartItem.quantity,
                        previousStock = prevStock,
                        newStock = newStock,
                        notes = "Penjualan struk: $receiptNumber"
                    )
                )
            }

            transactionDao.insertTransactionItems(itemEntities)
        }

        return TransactionDetail(
            transaction = transaction.copy(id = createdTrxId),
            items = itemEntities
        )
    }

    suspend fun calculateSalesSummary(transactions: List<TransactionEntity>): SalesSummary {
        var totalSales = 0.0
        var qrisSales = 0.0
        var cashSales = 0.0
        var transferSales = 0.0

        for (trx in transactions) {
            totalSales += trx.totalAmount
            when (trx.paymentType) {
                PaymentType.QRIS -> qrisSales += trx.totalAmount
                PaymentType.TUNAI -> cashSales += trx.totalAmount
                PaymentType.TRANSFER -> transferSales += trx.totalAmount
            }
        }

        // Calculate approximate gross profit from all items
        val allItems = transactionDao.getAllTransactionItems()
        var totalProfit = 0.0
        for (item in allItems) {
            val profitPerItem = (item.sellPrice - item.costPrice) * item.quantity
            totalProfit += profitPerItem
        }

        return SalesSummary(
            totalSalesAmount = totalSales,
            totalTransactionsCount = transactions.size,
            estimatedGrossProfit = totalProfit,
            qrisAmount = qrisSales,
            cashAmount = cashSales,
            transferAmount = transferSales
        )
    }
}
