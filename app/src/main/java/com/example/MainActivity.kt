package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CashierScreen
import com.example.ui.CheckoutDialog
import com.example.ui.InventoryScreen
import com.example.ui.PosTab
import com.example.ui.PosViewModel
import com.example.ui.ReceiptDialog
import com.example.ui.ReportsScreen
import com.example.ui.SupplierFinderScreen
import com.example.ui.theme.KasirProTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

class MainActivity : ComponentActivity() {
    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KasirProTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: PosViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cart.collectAsStateWithLifecycle()
    val subtotal by viewModel.subtotal.collectAsStateWithLifecycle()
    val discount by viewModel.discount.collectAsStateWithLifecycle()
    val isTaxEnabled by viewModel.isTaxEnabled.collectAsStateWithLifecycle()
    val taxAmount by viewModel.taxAmount.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    // Checkout & Dialog states
    val isCheckoutOpen by viewModel.isCheckoutOpen.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val cashTenderedInput by viewModel.cashTenderedInput.collectAsStateWithLifecycle()
    val changeAmount by viewModel.changeAmount.collectAsStateWithLifecycle()
    val completedTransaction by viewModel.completedTransaction.collectAsStateWithLifecycle()

    // Inventory dialog states
    val showProductDialog by viewModel.showProductDialog.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()
    val showRestockDialog by viewModel.showRestockDialog.collectAsStateWithLifecycle()
    val restockTargetProduct by viewModel.restockTargetProduct.collectAsStateWithLifecycle()

    // Reports states
    val reportPeriod by viewModel.reportPeriod.collectAsStateWithLifecycle()
    val salesSummary by viewModel.salesSummary.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val topProducts by viewModel.topProducts.collectAsStateWithLifecycle()

    // Suppliers state
    val supplierQuery by viewModel.supplierQuery.collectAsStateWithLifecycle()
    val supplierLocation by viewModel.supplierLocation.collectAsStateWithLifecycle()
    val suppliersList by viewModel.suppliersList.collectAsStateWithLifecycle()
    val isSearchingSuppliers by viewModel.isSearchingSuppliers.collectAsStateWithLifecycle()

    // Back button handling: return to Cashier tab if on another tab
    BackHandler(enabled = currentTab != PosTab.CASHIER) {
        viewModel.setTab(PosTab.CASHIER)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PosTopAppBar(
                currentTab = currentTab,
                lowStockCount = lowStockProducts.size
            )
        },
        bottomBar = {
            PosBottomNavigationBar(
                currentTab = currentTab,
                lowStockCount = lowStockProducts.size,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                PosTab.CASHIER -> {
                    CashierScreen(
                        viewModel = viewModel,
                        products = filteredProducts,
                        cartItems = cartItems,
                        subtotal = subtotal,
                        discount = discount,
                        isTaxEnabled = isTaxEnabled,
                        taxAmount = taxAmount,
                        totalAmount = totalAmount,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory
                    )
                }

                PosTab.INVENTORY -> {
                    InventoryScreen(
                        viewModel = viewModel,
                        products = allProducts,
                        showProductDialog = showProductDialog,
                        editingProduct = editingProduct,
                        showRestockDialog = showRestockDialog,
                        restockProduct = restockTargetProduct
                    )
                }

                PosTab.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        salesSummary = salesSummary,
                        period = reportPeriod,
                        transactions = allTransactions,
                        topProducts = topProducts
                    )
                }

                PosTab.SUPPLIERS -> {
                    SupplierFinderScreen(
                        viewModel = viewModel,
                        query = supplierQuery,
                        location = supplierLocation,
                        suppliers = suppliersList,
                        isSearching = isSearchingSuppliers
                    )
                }
            }
        }
    }

    // Checkout Modal Dialog
    if (isCheckoutOpen) {
        CheckoutDialog(
            totalAmount = totalAmount,
            subtotal = subtotal,
            discount = discount,
            tax = taxAmount,
            paymentMethod = paymentMethod,
            cashTenderedInput = cashTenderedInput,
            changeAmount = changeAmount,
            onPaymentMethodSelected = { viewModel.setPaymentMethod(it) },
            onCashTenderedChange = { viewModel.setCashTendered(it) },
            onConfirmPayment = { viewModel.completeCheckout() },
            onDismiss = { viewModel.closeCheckout() }
        )
    }

    // Digital Receipt Modal Dialog
    completedTransaction?.let { detail ->
        ReceiptDialog(
            detail = detail,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
fun PosTopAppBar(currentTab: PosTab, lowStockCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "KasirPro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = when (currentTab) {
                            PosTab.CASHIER -> "Mode Penjualan Toko"
                            PosTab.INVENTORY -> "Manajemen Stok Barang"
                            PosTab.REPORTS -> "Laporan Penjualan Real-time"
                            PosTab.SUPPLIERS -> "Cari Distributor Terdekat (Maps)"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Staff & Online Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kasir 01",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

@Composable
fun PosBottomNavigationBar(
    currentTab: PosTab,
    lowStockCount: Int,
    onTabSelected: (PosTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == PosTab.CASHIER,
            onClick = { onTabSelected(PosTab.CASHIER) },
            icon = {
                Icon(Icons.Default.Storefront, contentDescription = "Kasir")
            },
            label = { Text("Kasir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentTab == PosTab.INVENTORY,
            onClick = { onTabSelected(PosTab.INVENTORY) },
            icon = {
                if (lowStockCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = WarningAmber) {
                                Text(lowStockCount.toString(), color = Color.White)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = "Stok")
                    }
                } else {
                    Icon(Icons.Default.Inventory2, contentDescription = "Stok")
                }
            },
            label = { Text("Stok", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentTab == PosTab.REPORTS,
            onClick = { onTabSelected(PosTab.REPORTS) },
            icon = {
                Icon(Icons.Default.BarChart, contentDescription = "Laporan")
            },
            label = { Text("Laporan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentTab == PosTab.SUPPLIERS,
            onClick = { onTabSelected(PosTab.SUPPLIERS) },
            icon = {
                Icon(Icons.Default.LocationOn, contentDescription = "Pemasok")
            },
            label = { Text("Pemasok", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
