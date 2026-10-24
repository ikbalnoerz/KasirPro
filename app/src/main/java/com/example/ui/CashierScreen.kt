package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartItem
import com.example.data.ProductEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun CashierScreen(
    viewModel: PosViewModel,
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    isTaxEnabled: Boolean,
    taxAmount: Double,
    totalAmount: Double,
    searchQuery: String,
    selectedCategory: String,
    modifier: Modifier = Modifier
) {
    var isCartExpanded by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Makanan", "Minuman", "Sembako", "Snack", "ATK")

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // Adaptive 2-Column Split: Product Catalog (Left) + Persistent Summary Panel (Right)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Grid Column
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    ProductCatalogSection(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.setCategory(it) },
                        products = products,
                        cartItems = cartItems,
                        onAddToCart = { viewModel.addToCart(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dedicated Persistent Transaction Summary Panel
                Surface(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("persistent_summary_panel"),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    TransactionSummaryContent(
                        cartItems = cartItems,
                        subtotal = subtotal,
                        discount = discount,
                        isTaxEnabled = isTaxEnabled,
                        taxAmount = taxAmount,
                        totalAmount = totalAmount,
                        onIncreaseQty = { viewModel.updateCartQuantity(it, 1) },
                        onDecreaseQty = { viewModel.updateCartQuantity(it, -1) },
                        onRemoveItem = { viewModel.removeFromCart(it) },
                        onOpenDiscount = { showDiscountDialog = true },
                        onToggleTax = { viewModel.toggleTax() },
                        onClearCart = { viewModel.clearCart() },
                        onCheckout = { viewModel.openCheckout() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Compact Single Column: Grid with Docked Bottom Summary Panel
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (cartItems.isNotEmpty()) 100.dp else 16.dp)
                ) {
                    ProductCatalogSection(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.setCategory(it) },
                        products = products,
                        cartItems = cartItems,
                        onAddToCart = { viewModel.addToCart(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Docked Bottom Summary Panel
                if (cartItems.isNotEmpty()) {
                    DockedSummaryBottomBar(
                        cartItems = cartItems,
                        subtotal = subtotal,
                        discount = discount,
                        isTaxEnabled = isTaxEnabled,
                        taxAmount = taxAmount,
                        totalAmount = totalAmount,
                        isExpanded = isCartExpanded,
                        onToggleExpand = { isCartExpanded = !isCartExpanded },
                        onIncreaseQty = { viewModel.updateCartQuantity(it, 1) },
                        onDecreaseQty = { viewModel.updateCartQuantity(it, -1) },
                        onRemoveItem = { viewModel.removeFromCart(it) },
                        onOpenDiscount = { showDiscountDialog = true },
                        onToggleTax = { viewModel.toggleTax() },
                        onClearCart = { viewModel.clearCart() },
                        onCheckout = { viewModel.openCheckout() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Custom Discount Dialog
        if (showDiscountDialog) {
            DiscountDialog(
                currentDiscount = discount,
                onApply = {
                    viewModel.setDiscount(it)
                    showDiscountDialog = false
                },
                onDismiss = { showDiscountDialog = false }
            )
        }
    }
}

@Composable
fun ProductCatalogSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cashier_search_input"),
                placeholder = { Text("Cari produk atau barcode...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(text = category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Product Grid
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada produk yang cocok",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    val inCartQty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    MinimalistProductCard(
                        product = product,
                        inCartQty = inCartQty,
                        onAddToCart = { onAddToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalistProductCard(
    product: ProductEntity,
    inCartQty: Int,
    onAddToCart: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock in 1..product.minStockAlert

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isOutOfStock) { onAddToCart() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) Color(0xFFF8FAFC) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (inCartQty > 0) 2.dp else 1.dp,
            color = if (inCartQty > 0) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category tag & In-cart badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = product.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (inCartQty > 0) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inCartQty.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isOutOfStock) Color(0xFF94A3B8) else Color(0xFF0F172A),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price Tag
            Text(
                text = Formatters.toRupiah(product.sellPrice),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stockText = when {
                    isOutOfStock -> "Habis"
                    isLowStock -> "Sisa ${product.stock} ${product.unit}"
                    else -> "${product.stock} ${product.unit}"
                }
                val stockColor = when {
                    isOutOfStock -> DangerRed
                    isLowStock -> WarningAmber
                    else -> SuccessGreen
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = stockText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = stockColor
                    )
                }

                if (!isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Summary Panel Content used in both wide-screen split layout and mobile modal/expandable sheet.
 */
@Composable
fun TransactionSummaryContent(
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    isTaxEnabled: Boolean,
    taxAmount: Double,
    totalAmount: Double,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onOpenDiscount: () -> Unit,
    onToggleTax: () -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalQty = cartItems.sumOf { it.quantity }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        // Panel Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Ringkasan Transaksi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "$totalQty item dalam transaksi aktif",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (cartItems.isNotEmpty()) {
                TextButton(
                    onClick = onClearCart,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Kosongkan", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

        // Empty State or Items List
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum Ada Transaksi Aktif",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih atau cari produk di katalog untuk menambahkan ke struk belanja",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Scrollable Active Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    TransactionItemRow(
                        item = item,
                        onIncrease = { onIncreaseQty(item.product.id) },
                        onDecrease = { onDecreaseQty(item.product.id) },
                        onRemove = { onRemoveItem(item.product.id) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

        // Totals & Discount/Tax Calculation Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Subtotal", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(text = Formatters.toRupiah(subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
            }

            // Discount Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenDiscount() }
                ) {
                    Icon(Icons.Default.Discount, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (discount > 0) "Diskon (Ubah)" else "+ Tambah Diskon",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD97706)
                    )
                }
                Text(
                    text = if (discount > 0) "-${Formatters.toRupiah(discount)}" else "Rp 0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (discount > 0) Color(0xFFD97706) else Color(0xFF94A3B8)
                )
            }

            // Tax (PPN 11%) Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "PPN (11%)", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isTaxEnabled,
                        onCheckedChange = { onToggleTax() },
                        modifier = Modifier.size(28.dp),
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                    )
                }
                Text(
                    text = if (taxAmount > 0) Formatters.toRupiah(taxAmount) else "Rp 0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))

            // Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL BAYAR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = Formatters.toRupiah(totalAmount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Primary Checkout Action Button
        Button(
            onClick = onCheckout,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_checkout"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Proses Pembayaran",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${Formatters.toRupiah(item.product.sellPrice)} / ${item.product.unit}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Stepper (-)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .clickable { onDecrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp), tint = Color(0xFF334155))
                }

                Text(
                    text = item.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color(0xFF0F172A)
                )

                // Stepper (+)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .clickable(enabled = item.quantity < item.product.stock) { onIncrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah",
                        modifier = Modifier.size(14.dp),
                        tint = if (item.quantity < item.product.stock) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = Formatters.toRupiah(item.subtotal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Mobile Docked Summary Bottom Bar with expanding sheet
 */
@Composable
fun DockedSummaryBottomBar(
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    isTaxEnabled: Boolean,
    taxAmount: Double,
    totalAmount: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onOpenDiscount: () -> Unit,
    onToggleTax: () -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalQty = cartItems.sumOf { it.quantity }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = Color.White,
        shadowElevation = 16.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Expandable Items List
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(animationSpec = spring()),
                exit = fadeOut() + shrinkVertically(animationSpec = spring())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rincian Transaksi Aktif ($totalQty item)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        TextButton(onClick = onClearCart) {
                            Text("Kosongkan", color = DangerRed, fontSize = 12.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((cartItems.size * 60).coerceAtMost(220).dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cartItems, key = { it.product.id }) { item ->
                            TransactionItemRow(
                                item = item,
                                onIncrease = { onIncreaseQty(item.product.id) },
                                onDecrease = { onDecreaseQty(item.product.id) },
                                onRemove = { onRemoveItem(item.product.id) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Discount & Tax Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenDiscount,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Discount, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (discount > 0) "Diskon: ${Formatters.toRupiah(discount)}" else "+ Tambah Diskon",
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "PPN 11%", fontSize = 12.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isTaxEnabled,
                                onCheckedChange = { onToggleTax() },
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            // Summary Bar & Checkout Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleExpand() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalQty item",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = Formatters.toRupiah(totalAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("btn_checkout"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Bayar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DiscountDialog(
    currentDiscount: Double,
    onApply: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var discountText by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Potongan Diskon", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                Text("Masukkan nominal diskon (Rp):", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) discountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Nominal Diskon (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = discountText.toDoubleOrNull() ?: 0.0
                onApply(amount)
            }) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
