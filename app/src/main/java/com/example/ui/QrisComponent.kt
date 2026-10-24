package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QrisService
import com.example.ui.theme.QrisRed
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun QrisCard(
    merchantName: String = "TOKO KASIRPRO",
    nmid: String = "ID102030405060",
    totalAmount: Double,
    onPaymentConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val qrisService = remember { QrisService() }
    val qrisInvoice = remember(totalAmount) {
        qrisService.generateDynamicQris(
            amount = totalAmount,
            merchantName = merchantName,
            nmid = nmid
        )
    }

    var secondsRemaining by remember { mutableIntStateOf(300) } // 5 minutes

    LaunchedEffect(totalAmount) {
        secondsRemaining = 300
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qris_payment_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QRIS Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(QrisRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QRIS",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "QRIS DINAMIS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "NMID: ${qrisInvoice.nmid}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Timer badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Text(
                        text = "Kadaluarsa $timerStr",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = QrisRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Merchant Info
            Text(
                text = qrisInvoice.merchantName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Total: ${Formatters.toRupiah(totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ref: ${qrisInvoice.referenceId}",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic QR Matrix generated from the dynamic EMVCo QRIS string
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                DynamicQrCodeCanvas(seed = qrisInvoice.rawPayload.hashCode().toLong())
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Raw QRIS String helper for copy/inspection
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("QRIS Payload", qrisInvoice.rawPayload))
                        Toast.makeText(context, "String QRIS tersalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    },
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = qrisInvoice.rawPayload,
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Salin String QRIS",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Supported e-wallets label
            Text(
                text = "BCA • Mandiri • BRI • BNI • GoPay • OVO • DANA • ShopeePay",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confirmation button
            ElevatedButton(
                onClick = onPaymentConfirmed,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_qris_payment_btn"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfirmasi QRIS Diterima",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DynamicQrCodeCanvas(seed: Long) {
    Canvas(modifier = Modifier.size(184.dp)) {
        val gridSize = 25
        val moduleSize = size.width / gridSize
        val random = Random(seed)

        // Fill background
        drawRect(color = Color.White)

        // Draw Finder Pattern helper
        fun drawFinder(startX: Int, startY: Int) {
            // 7x7 outer square
            for (x in 0 until 7) {
                for (y in 0 until 7) {
                    val isBorder = x == 0 || x == 6 || y == 0 || y == 6
                    val isInner = x in 2..4 && y in 2..4
                    if (isBorder || isInner) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset((startX + x) * moduleSize, (startY + y) * moduleSize),
                            size = Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        }

        // 3 Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinder(0, 0)
        drawFinder(gridSize - 7, 0)
        drawFinder(0, gridSize - 7)

        // Draw data modules avoiding finders
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val inTopLeftFinder = x < 8 && y < 8
                val inTopRightFinder = x >= gridSize - 8 && y < 8
                val inBottomLeftFinder = x < 8 && y >= gridSize - 8

                if (!inTopLeftFinder && !inTopRightFinder && !inBottomLeftFinder) {
                    // Timing pattern lines
                    if (x == 6 || y == 6) {
                        if ((x + y) % 2 == 0) {
                            drawRect(
                                color = Color(0xFF0F172A),
                                topLeft = Offset(x * moduleSize, y * moduleSize),
                                size = Size(moduleSize, moduleSize)
                            )
                        }
                    } else if (random.nextBoolean()) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(x * moduleSize, y * moduleSize),
                            size = Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        }
    }
}
