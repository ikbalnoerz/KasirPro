package com.example

import com.example.data.CartItem
import com.example.data.Converters
import com.example.data.PaymentType
import com.example.data.ProductEntity
import com.example.data.QrisService
import com.example.data.StockMovementType
import com.example.data.TransactionStatus
import com.example.ui.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ExampleUnitTest {
    @Test
    fun testRupiahFormatting() {
        val formatted = Formatters.toRupiah(35000.0)
        assertTrue(formatted.contains("35.000") || formatted.contains("35,000"))
    }

    @Test
    fun testCartItemCalculations() {
        val product = ProductEntity(
            id = 1,
            barcode = "12345",
            name = "Indomie Goreng",
            category = "Makanan",
            costPrice = 2800.0,
            sellPrice = 3500.0,
            stock = 10
        )
        val cartItem = CartItem(product = product, quantity = 3)
        assertEquals(10500.0, cartItem.subtotal, 0.001)
        assertEquals(8400.0, cartItem.totalCost, 0.001)
    }

    @Test
    fun testTypeConverters() {
        val converters = Converters()

        // Date converter test
        val now = Date()
        val timestamp = converters.dateToTimestamp(now)
        assertNotNull(timestamp)
        val convertedDate = converters.fromTimestamp(timestamp)
        assertEquals(now.time, convertedDate?.time)

        // PaymentType converter test
        val paymentType = PaymentType.QRIS
        val paymentStr = converters.fromPaymentType(paymentType)
        assertEquals("QRIS", paymentStr)
        assertEquals(PaymentType.QRIS, converters.toPaymentType(paymentStr))

        // TransactionStatus converter test
        val status = TransactionStatus.COMPLETED
        val statusStr = converters.fromTransactionStatus(status)
        assertEquals("COMPLETED", statusStr)
        assertEquals(TransactionStatus.COMPLETED, converters.toTransactionStatus(statusStr))

        // StockMovementType converter test
        val movementType = StockMovementType.RESTOCK
        val movementStr = converters.fromStockMovementType(movementType)
        assertEquals("RESTOCK", movementStr)
        assertEquals(StockMovementType.RESTOCK, converters.toStockMovementType(movementStr))

        // String list converter test
        val tags = listOf("promo", "sembako", "laris")
        val serialized = converters.fromStringList(tags)
        val deserialized = converters.toStringList(serialized)
        assertEquals(tags, deserialized)
    }

    @Test
    fun testQrisServiceGeneration() {
        val service = QrisService()
        val invoice = service.generateDynamicQris(
            amount = 55000.0,
            merchantName = "TOKO KASIRPRO",
            nmid = "ID102030405060"
        )

        assertNotNull(invoice)
        assertTrue(invoice.rawPayload.startsWith("000201010212")) // EMVCo Dynamic QR
        assertTrue(invoice.rawPayload.contains("5303360")) // IDR currency
        assertTrue(invoice.rawPayload.contains("540555000")) // Tag 54, length 05, value 55000
        assertTrue(invoice.rawPayload.contains("5802ID")) // Country ID
        assertTrue(invoice.rawPayload.contains("TOKO KASIRPRO")) // Merchant Name
        assertEquals(4, invoice.crcChecksum.length)
        assertTrue(invoice.rawPayload.endsWith(invoice.crcChecksum))
    }
}
