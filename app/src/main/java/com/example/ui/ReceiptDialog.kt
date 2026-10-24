package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TransactionDetail

@Composable
fun ReceiptDialog(
    detail: TransactionDetail,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val trx = detail.transaction

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Thermal Paper Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Store Header
                        Text(
                            text = "KASIRPRO STORE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Jl. Raya Niaga No. 88, Jakarta Barat",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Telp: (021) 5890-1234",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Transaction Info
                        ReceiptRow(label = "No. Struk:", value = trx.receiptNumber)
                        ReceiptRow(label = "Tanggal:", value = Formatters.formatDateTime(trx.timestamp))
                        ReceiptRow(label = "Kasir:", value = trx.cashierName)
                        ReceiptRow(label = "Metode:", value = trx.paymentMethod)

                        Spacer(modifier = Modifier.height(10.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items
                        detail.items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity} x ${Formatters.toRupiah(item.sellPrice)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = Formatters.toRupiah(item.subtotal),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals
                        ReceiptRow(label = "Subtotal:", value = Formatters.toRupiah(trx.subtotal))
                        if (trx.discount > 0) {
                            ReceiptRow(label = "Diskon:", value = "-${Formatters.toRupiah(trx.discount)}")
                        }
                        if (trx.tax > 0) {
                            ReceiptRow(label = "PPN (11%):", value = Formatters.toRupiah(trx.tax))
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL BAYAR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = Formatters.toRupiah(trx.totalAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (trx.paymentMethod == "TUNAI") {
                            ReceiptRow(label = "Tunai Diterima:", value = Formatters.toRupiah(trx.cashTendered))
                            ReceiptRow(label = "Kembalian:", value = Formatters.toRupiah(trx.changeAmount))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        DottedDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer Note
                        Text(
                            text = "Terima kasih telah berbelanja!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Barang yang sudah dibeli tidak dapat ditukar/dikembalikan.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Mencetak struk ke printer thermal Bluetooth...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_receipt_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Cetak Struk", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_receipt_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DottedDivider() {
    Text(
        text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - -",
        color = Color(0xFFCBD5E1),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
    )
}
