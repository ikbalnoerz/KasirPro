package com.example.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    private val dateOnlyFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    fun toRupiah(amount: Double): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeOnlyFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }
}
