package com.example.data

import androidx.room.TypeConverter
import java.util.Date

enum class PaymentType {
    QRIS,
    TUNAI,
    TRANSFER;

    companion object {
        fun fromString(value: String): PaymentType = try {
            valueOf(value.trim().uppercase())
        } catch (_: Exception) {
            TUNAI
        }
    }
}

enum class TransactionStatus {
    COMPLETED,
    REFUNDED,
    CANCELLED;

    companion object {
        fun fromString(value: String): TransactionStatus = try {
            valueOf(value.trim().uppercase())
        } catch (_: Exception) {
            COMPLETED
        }
    }
}

enum class StockMovementType {
    INITIAL,
    RESTOCK,
    SALE,
    ADJUSTMENT;

    companion object {
        fun fromString(value: String): StockMovementType = try {
            valueOf(value.trim().uppercase())
        } catch (_: Exception) {
            ADJUSTMENT
        }
    }
}

class Converters {
    // Date Converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // PaymentType Converters
    @TypeConverter
    fun fromPaymentType(type: PaymentType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toPaymentType(value: String?): PaymentType? {
        return value?.let { PaymentType.fromString(it) }
    }

    // TransactionStatus Converters
    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toTransactionStatus(value: String?): TransactionStatus? {
        return value?.let { TransactionStatus.fromString(it) }
    }

    // StockMovementType Converters
    @TypeConverter
    fun fromStockMovementType(type: StockMovementType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toStockMovementType(value: String?): StockMovementType? {
        return value?.let { StockMovementType.fromString(it) }
    }

    // List<String> Converters (for tags / notes)
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }
}
