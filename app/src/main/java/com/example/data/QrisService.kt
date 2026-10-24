package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model representing a generated dynamic QRIS invoice for checkout.
 */
data class QrisInvoice(
    val rawPayload: String,
    val merchantName: String,
    val nmid: String,
    val referenceId: String,
    val amount: Double,
    val currency: String = "IDR",
    val expiryTimestamp: Long,
    val formattedAmount: String,
    val crcChecksum: String
)

/**
 * Service for generating dynamic QRIS strings conforming to the EMVCo MPM & Bank Indonesia QRIS specifications.
 */
class QrisService {

    /**
     * Generates a mock dynamic QRIS string according to the official EMVCo / Bank Indonesia QRIS TLV specifications.
     *
     * @param amount The dynamic transaction amount in Rupiah.
     * @param merchantName The name of the store merchant.
     * @param merchantCity The city of the store merchant.
     * @param nmid The National Merchant Identifier.
     * @param referenceId An optional reference or bill transaction ID.
     * @param validityMinutes Validity duration before dynamic QR expires (default: 5 minutes).
     */
    fun generateDynamicQris(
        amount: Double,
        merchantName: String = "TOKO KASIRPRO",
        merchantCity: String = "JAKARTA",
        nmid: String = "ID102030405060",
        referenceId: String? = null,
        validityMinutes: Int = 5
    ): QrisInvoice {
        val dateStr = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val refId = referenceId ?: "INV-$dateStr-${(100..999).random()}"
        val expiryTimestamp = System.currentTimeMillis() + (validityMinutes * 60 * 1000L)

        // Subtag 26: Merchant Account Information
        val tag26Sub00 = formatTlv("00", "ID.CO.QRIS.WWW")
        val tag26Sub01 = formatTlv("01", nmid)
        val tag26Sub02 = formatTlv("02", "01") // UMI (Usaha Mikro)
        val tag26Value = tag26Sub00 + tag26Sub01 + tag26Sub02
        val tag26 = formatTlv("26", tag26Value)

        // Subtag 51: Acquirer Information (GPN / Bank Acquirer)
        val tag51Sub00 = formatTlv("00", "ID.OR.GPN")
        val tag51Sub02 = formatTlv("02", "00000000")
        val tag51Value = tag51Sub00 + tag51Sub02
        val tag51 = formatTlv("51", tag51Value)

        // Subtag 62: Additional Data Field (Reference Bill Number)
        val tag62Sub01 = formatTlv("01", refId)
        val tag62Sub07 = formatTlv("07", "01")
        val tag62Value = tag62Sub01 + tag62Sub07
        val tag62 = formatTlv("62", tag62Value)

        // Formatted amount (e.g. "45000" or "45000.00")
        val amountStr = String.format(Locale.US, "%.0f", amount)

        val payloadWithoutCrc = StringBuilder().apply {
            append(formatTlv("00", "01")) // Payload Format Indicator
            append(formatTlv("01", "12")) // Point of Initiation: 12 = Dynamic QR
            append(tag26)                 // Merchant Account Information
            append(tag51)                 // Acquirer Information
            append(formatTlv("52", "5411")) // Merchant Category Code (Grocery / Store)
            append(formatTlv("53", "360"))  // Transaction Currency: 360 = IDR
            append(formatTlv("54", amountStr)) // Transaction Amount
            append(formatTlv("58", "ID"))   // Country Code
            append(formatTlv("59", merchantName.take(25))) // Merchant Name
            append(formatTlv("60", merchantCity.take(15))) // Merchant City
            append(formatTlv("61", "11530")) // Postal Code
            append(tag62)                   // Additional Data
            append("6304")                  // CRC Tag 63 with length 04
        }.toString()

        val checksum = calculateCrc16Ccitt(payloadWithoutCrc)
        val completeQrisString = payloadWithoutCrc + checksum

        return QrisInvoice(
            rawPayload = completeQrisString,
            merchantName = merchantName,
            nmid = nmid,
            referenceId = refId,
            amount = amount,
            currency = "IDR",
            expiryTimestamp = expiryTimestamp,
            formattedAmount = "Rp ${amountStr.reversed().chunked(3).joinToString(".").reversed()}",
            crcChecksum = checksum
        )
    }

    /**
     * Formats a Tag-Length-Value (TLV) string as per EMVCo specifications.
     */
    private fun formatTlv(tag: String, value: String): String {
        val length = value.length.toString().padStart(2, '0')
        return "$tag$length$value"
    }

    /**
     * Calculates the CRC16-CCITT checksum (polynomial 0x1021, initial 0xFFFF)
     * as required by the EMVCo / Bank Indonesia QRIS specification.
     */
    fun calculateCrc16Ccitt(data: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021
        val bytes = data.toByteArray(Charsets.ISO_8859_1)

        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        crc = crc and 0xFFFF
        return String.format(Locale.US, "%04X", crc)
    }
}
