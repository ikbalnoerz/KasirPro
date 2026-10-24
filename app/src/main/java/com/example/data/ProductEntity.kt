package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"]),
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val name: String,
    val category: String,
    val costPrice: Double, // Harga modal
    val sellPrice: Double, // Harga jual
    val stock: Int,
    val minStockAlert: Int = 5,
    val unit: String = "pcs",
    val updatedAt: Long = System.currentTimeMillis()
)
