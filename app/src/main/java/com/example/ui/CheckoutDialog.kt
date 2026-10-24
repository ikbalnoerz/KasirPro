package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BankBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.QrisRed
import com.example.ui.theme.SuccessGreen

@Composable
fun CheckoutDialog(
    totalAmount: Double,
    subtotal: Double,
    discount: Double,
    tax: Double,
    paymentMethod: String,
    cashTenderedInput: String,
    changeAmount: Double,
    onPaymentMethodSelected: (String) -> Unit,
    onCashTenderedChange: (String) -> Unit,
    onConfirmPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("checkout_modal"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pembayaran",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Pilih metode pembayaran pembeli",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_checkout_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal", fontSize = 12.sp, color = Color(0xFF0F766E))
                            Text(text = Formatters.toRupiah(subtotal), fontSize = 12.sp, color = Color(0xFF0F766E))
                        }
                        if (discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Diskon", fontSize = 12.sp, color = Color(0xFFB45309))
                                Text(text = "-${Formatters.toRupiah(discount)}", fontSize = 12.sp, color = Color(0xFFB45309))
                            }
                        }
                        if (tax > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "PPN (11%)", fontSize = 12.sp, color = Color(0xFF0F766E))
                                Text(text = Formatters.toRupiah(tax), fontSize = 12.sp, color = Color(0xFF0F766E))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL TAGIHAN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF115E59)
                            )
                            Text(
                                text = Formatters.toRupiah(totalAmount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF115E59)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodTab(
                        title = "QRIS",
                        icon = Icons.Default.QrCode,
                        isSelected = paymentMethod == "QRIS",
                        activeColor = QrisRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onPaymentMethodSelected("QRIS") }
                    )
                    PaymentMethodTab(
                        title = "Tunai",
                        icon = Icons.Default.Payments,
                        isSelected = paymentMethod == "TUNAI",
                        activeColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onPaymentMethodSelected("TUNAI") }
                    )
                    PaymentMethodTab(
                        title = "Transfer",
                        icon = Icons.Default.AccountBalance,
                        isSelected = paymentMethod == "TRANSFER",
                        activeColor = BankBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onPaymentMethodSelected("TRANSFER") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Method Specific Content
                when (paymentMethod) {
                    "QRIS" -> {
                        QrisCard(
                            totalAmount = totalAmount,
                            onPaymentConfirmed = onConfirmPayment
                        )
                    }

                    "TUNAI" -> {
                        val tendered = cashTenderedInput.toDoubleOrNull() ?: 0.0
                        val isShort = tendered < totalAmount

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Uang Diterima dari Pembeli",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cashTenderedInput,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        onCashTenderedChange(input)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cash_tendered_input"),
                                label = { Text("Nominal Uang (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Denomination Chips
                            Text(
                                text = "Pilihan Uang Cepat:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickCashChip(label = "Uang Pas", onClick = {
                                    onCashTenderedChange(totalAmount.toInt().toString())
                                })
                                QuickCashChip(label = "20rb", onClick = { onCashTenderedChange("20000") })
                                QuickCashChip(label = "50rb", onClick = { onCashTenderedChange("50000") })
                                QuickCashChip(label = "100rb", onClick = { onCashTenderedChange("10000") })
                                QuickCashChip(label = "200rb", onClick = { onCashTenderedChange("200000") })
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Kembalian Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isShort) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isShort) Color(0xFFFECACA) else Color(0xFFBBF7D0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isShort) "Uang Kurang:" else "Kembalian:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isShort) DangerRed else Color(0xFF166534)
                                    )
                                    Text(
                                        text = if (isShort)
                                            "-${Formatters.toRupiah(totalAmount - tendered)}"
                                        else
                                            Formatters.toRupiah(changeAmount),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isShort) DangerRed else Color(0xFF166534)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onConfirmPayment,
                                enabled = !isShort && tendered > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_cash_payment_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Selesaikan Transaksi & Cetak Struk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    "TRANSFER" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            BankOptionCard(
                                bankName = "BCA (Bank Central Asia)",
                                vaNumber = "7810 8829 3010 449",
                                accountName = "KASIRPRO STORE"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BankOptionCard(
                                bankName = "Mandiri Virtual Account",
                                vaNumber = "8890 0123 4567 890",
                                accountName = "KASIRPRO STORE"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BankOptionCard(
                                bankName = "BRI (BRIVA)",
                                vaNumber = "1280 9988 7766 554",
                                accountName = "KASIRPRO STORE"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onConfirmPayment,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_transfer_payment_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BankBlue)
                            ) {
                                Text(
                                    text = "Konfirmasi Transfer Diterima",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodTab(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else null,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun QuickCashChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BankOptionCard(bankName: String, vaNumber: String, accountName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = bankName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text(text = vaNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BankBlue)
                Text(text = "a/n $accountName", fontSize = 10.sp, color = Color(0xFF64748B))
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = BankBlue.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "VA Otomatis",
                    fontSize = 10.sp,
                    color = BankBlue,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
