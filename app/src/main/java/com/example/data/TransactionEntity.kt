package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["receiptNumber"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String, // "QRIS", "TUNAI", "TRANSFER"
    val cashTendered: Double = 0.0,
    val changeAmount: Double = 0.0,
    val cashierName: String = "Kasir 01",
    val customerNote: String = "",
    val paymentType: PaymentType = PaymentType.fromString(paymentMethod),
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transactionId"),
        Index("productId")
    ]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val costPrice: Double,
    val sellPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

/**
 * 1-to-Many Room Relation for reading a Transaction along with all its line items.
 */
data class TransactionWithItems(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItemEntity>
)

/**
 * Audit log entity for efficient inventory movement and stock tracing.
 */
@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productId"),
        Index("timestamp")
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: StockMovementType,
    val quantityChange: Int,
    val previousStock: Int,
    val newStock: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
