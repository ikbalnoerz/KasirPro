package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.KasirDatabase
import com.example.data.PosRepository
import com.example.data.ProductEntity
import com.example.data.SalesSummary
import com.example.data.SupplierPlace
import com.example.data.SupplierService
import com.example.data.TopProductSummary
import com.example.data.TransactionDetail
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class PosTab {
    CASHIER,
    INVENTORY,
    REPORTS,
    SUPPLIERS
}

enum class ReportPeriod {
    TODAY,
    WEEK,
    MONTH,
    ALL
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KasirDatabase.getDatabase(application)
    private val repository = PosRepository(database)
    private val supplierService = SupplierService()

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(PosTab.CASHIER)
    val currentTab: StateFlow<PosTab> = _currentTab.asStateFlow()

    // Products & Inventory
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topProducts: StateFlow<List<TopProductSummary>> = repository.topProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered Products for Cashier Catalog
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true)
            val matchCat = category == "Semua" || product.category == category
            matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Management
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _isTaxEnabled = MutableStateFlow(false)
    val isTaxEnabled: StateFlow<Boolean> = _isTaxEnabled.asStateFlow()

    val subtotal: StateFlow<Double> = _cart.combine(_discount) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val taxAmount: StateFlow<Double> = combine(subtotal, _discount, _isTaxEnabled) { sub, disc, taxOn ->
        if (taxOn) {
            val taxable = (sub - disc).coerceAtLeast(0.0)
            taxable * 0.11 // 11% PPN Indonesia
        } else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalAmount: StateFlow<Double> = combine(subtotal, _discount, taxAmount) { sub, disc, tax ->
        ((sub - disc).coerceAtLeast(0.0) + tax)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Checkout & Payment State
    private val _isCheckoutOpen = MutableStateFlow(false)
    val isCheckoutOpen: StateFlow<Boolean> = _isCheckoutOpen.asStateFlow()

    private val _paymentMethod = MutableStateFlow("QRIS") // "QRIS", "TUNAI", "TRANSFER"
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _cashTenderedInput = MutableStateFlow("")
    val cashTenderedInput: StateFlow<String> = _cashTenderedInput.asStateFlow()

    val changeAmount: StateFlow<Double> = combine(totalAmount, _cashTenderedInput) { total, input ->
        val tendered = input.toDoubleOrNull() ?: 0.0
        if (tendered >= total) tendered - total else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _isCheckingOut = MutableStateFlow(false)
    val isCheckingOut: StateFlow<Boolean> = _isCheckingOut.asStateFlow()

    private val _completedTransaction = MutableStateFlow<TransactionDetail?>(null)
    val completedTransaction: StateFlow<TransactionDetail?> = _completedTransaction.asStateFlow()

    // Product Add/Edit Dialog State
    private val _showProductDialog = MutableStateFlow(false)
    val showProductDialog: StateFlow<Boolean> = _showProductDialog.asStateFlow()

    private val _editingProduct = MutableStateFlow<ProductEntity?>(null)
    val editingProduct: StateFlow<ProductEntity?> = _editingProduct.asStateFlow()

    // Quick Restock Dialog
    private val _showRestockDialog = MutableStateFlow(false)
    val showRestockDialog: StateFlow<Boolean> = _showRestockDialog.asStateFlow()
    private val _restockTargetProduct = MutableStateFlow<ProductEntity?>(null)
    val restockTargetProduct: StateFlow<ProductEntity?> = _restockTargetProduct.asStateFlow()

    // Reports Period
    private val _reportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _salesSummary = MutableStateFlow(
        SalesSummary(0.0, 0, 0.0, 0.0, 0.0, 0.0)
    )
    val salesSummary: StateFlow<SalesSummary> = _salesSummary.asStateFlow()

    // Supplier Maps Grounding Finder State
    private val _supplierQuery = MutableStateFlow("Grosir Sembako")
    val supplierQuery: StateFlow<String> = _supplierQuery.asStateFlow()

    private val _supplierLocation = MutableStateFlow("Jakarta Barat")
    val supplierLocation: StateFlow<String> = _supplierLocation.asStateFlow()

    private val _suppliersList = MutableStateFlow<List<SupplierPlace>>(emptyList())
    val suppliersList: StateFlow<List<SupplierPlace>> = _suppliersList.asStateFlow()

    private val _isSearchingSuppliers = MutableStateFlow(false)
    val isSearchingSuppliers: StateFlow<Boolean> = _isSearchingSuppliers.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
        }
        // Load initial suppliers
        searchSuppliers("Grosir Sembako")

        // Recompute sales summary whenever transactions change
        viewModelScope.launch {
            combine(allTransactions, reportPeriod) { transactions, period ->
                filterTransactionsByPeriod(transactions, period)
            }.collect { filteredTrx ->
                val summary = repository.calculateSalesSummary(filteredTrx)
                _salesSummary.value = summary
            }
        }
    }

    fun setTab(tab: PosTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: ProductEntity) {
        if (product.stock <= 0) return
        val currentItems = _cart.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentItems[index]
            if (existing.quantity < product.stock) {
                currentItems[index] = existing.copy(quantity = existing.quantity + 1)
                _cart.value = currentItems
            }
        } else {
            currentItems.add(CartItem(product, 1))
            _cart.value = currentItems
        }
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentItems = _cart.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val existing = currentItems[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentItems.removeAt(index)
            } else if (newQty <= existing.product.stock) {
                currentItems[index] = existing.copy(quantity = newQty)
            }
            _cart.value = currentItems
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discount.value = 0.0
    }

    fun setDiscount(amount: Double) {
        _discount.value = amount.coerceAtLeast(0.0)
    }

    fun toggleTax() {
        _isTaxEnabled.value = !_isTaxEnabled.value
    }

    fun openCheckout() {
        if (_cart.value.isEmpty()) return
        _paymentMethod.value = "QRIS"
        _cashTenderedInput.value = totalAmount.value.toInt().toString()
        _isCheckoutOpen.value = true
    }

    fun closeCheckout() {
        _isCheckoutOpen.value = false
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
        if (method == "TUNAI") {
            _cashTenderedInput.value = totalAmount.value.toInt().toString()
        }
    }

    fun setCashTendered(amountStr: String) {
        _cashTenderedInput.value = amountStr
    }

    fun completeCheckout() {
        val items = _cart.value
        if (items.isEmpty()) return

        val method = _paymentMethod.value
        val total = totalAmount.value
        val tendered = if (method == "TUNAI") {
            _cashTenderedInput.value.toDoubleOrNull() ?: total
        } else {
            total
        }

        if (method == "TUNAI" && tendered < total) {
            // Cannot checkout if cash tendered is less than total
            return
        }

        val change = (tendered - total).coerceAtLeast(0.0)

        viewModelScope.launch {
            _isCheckingOut.value = true
            try {
                val detail = repository.executeCheckout(
                    items = items,
                    paymentMethod = method,
                    subtotal = subtotal.value,
                    discount = _discount.value,
                    tax = taxAmount.value,
                    totalAmount = total,
                    cashTendered = tendered,
                    changeAmount = change,
                    cashierName = "Kasir 01"
                )
                _completedTransaction.value = detail
                clearCart()
                _isCheckoutOpen.value = false
            } finally {
                _isCheckingOut.value = false
            }
        }
    }

    fun dismissReceipt() {
        _completedTransaction.value = null
    }

    fun viewPastTransactionReceipt(transaction: TransactionEntity) {
        viewModelScope.launch {
            val detail = repository.getTransactionDetail(transaction.id)
            if (detail != null) {
                _completedTransaction.value = detail
            }
        }
    }

    // Inventory operations
    fun openAddProductDialog() {
        _editingProduct.value = null
        _showProductDialog.value = true
    }

    fun openEditProductDialog(product: ProductEntity) {
        _editingProduct.value = product
        _showProductDialog.value = true
    }

    fun closeProductDialog() {
        _showProductDialog.value = false
        _editingProduct.value = null
    }

    fun saveProduct(
        id: Long,
        name: String,
        barcode: String,
        category: String,
        costPrice: Double,
        sellPrice: Double,
        stock: Int,
        minStock: Int,
        unit: String
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                name = name.trim(),
                barcode = barcode.trim(),
                category = category.trim(),
                costPrice = costPrice,
                sellPrice = sellPrice,
                stock = stock,
                minStockAlert = minStock,
                unit = unit.trim()
            )
            if (id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
            closeProductDialog()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun openRestockDialog(product: ProductEntity) {
        _restockTargetProduct.value = product
        _showRestockDialog.value = true
    }

    fun closeRestockDialog() {
        _showRestockDialog.value = false
        _restockTargetProduct.value = null
    }

    fun applyRestock(addedQty: Int) {
        val target = _restockTargetProduct.value ?: return
        viewModelScope.launch {
            repository.addStock(target.id, addedQty)
            closeRestockDialog()
        }
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    private fun filterTransactionsByPeriod(
        transactions: List<TransactionEntity>,
        period: ReportPeriod
    ): List<TransactionEntity> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (period) {
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                transactions.filter { it.timestamp >= startOfDay }
            }
            ReportPeriod.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = cal.timeInMillis
                transactions.filter { it.timestamp >= sevenDaysAgo }
            }
            ReportPeriod.MONTH -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = cal.timeInMillis
                transactions.filter { it.timestamp >= monthAgo }
            }
            ReportPeriod.ALL -> transactions
        }
    }

    // Supplier search
    fun searchSuppliers(query: String, location: String = _supplierLocation.value) {
        _supplierQuery.value = query
        _supplierLocation.value = location
        viewModelScope.launch {
            _isSearchingSuppliers.value = true
            try {
                val results = supplierService.searchSuppliers(query, location)
                _suppliersList.value = results
            } finally {
                _isSearchingSuppliers.value = false
            }
        }
    }
}
