package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.CustomerEntity
import com.example.data.db.InvoiceEntity
import com.example.data.db.LedgerTransactionEntity
import com.example.data.model.InvoiceData
import com.example.data.model.InvoiceLineItem
import com.example.data.model.VendorProfile
import com.example.data.repository.InvoiceRepository
import com.example.utils.PdfGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.data.model.TaxMetrics
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceViewModel(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _invoiceForm = MutableStateFlow(
        InvoiceData(
            invoiceNo = "1001",
            invoiceDate = dateFormat.format(Date()),
            vendorProfile = repository.getVendorProfile(),
            authSign = repository.getVendorProfile().proprietor
        )
    )
    val invoiceForm: StateFlow<InvoiceData> = _invoiceForm.asStateFlow()

    val taxMetrics: StateFlow<TaxMetrics> = _invoiceForm.map { invoice ->
        TaxMetrics(
            taxableValue = invoice.taxableValue,
            gstRatePercent = invoice.gstRatePercent,
            cgstRate = invoice.cgstRatePercent,
            sgstRate = invoice.sgstRatePercent,
            igstRate = invoice.igstRatePercent,
            cgst = invoice.cgstAmt,
            sgst = invoice.sgstAmt,
            igst = invoice.igstAmt,
            totalTax = invoice.totalTax,
            grandTotal = invoice.grandTotal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaxMetrics()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedInvoices: StateFlow<List<InvoiceEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allInvoices
        } else {
            repository.searchInvoices(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _vendorProfile = MutableStateFlow(repository.getVendorProfile())
    val vendorProfile: StateFlow<VendorProfile> = _vendorProfile.asStateFlow()

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<com.example.data.model.AppThemeMode> = _themeMode.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(repository.isDynamicColor())
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    fun setThemeMode(mode: com.example.data.model.AppThemeMode) {
        _themeMode.value = mode
        repository.saveThemeMode(mode)
    }

    fun toggleThemeMode() {
        val nextMode = when (_themeMode.value) {
            com.example.data.model.AppThemeMode.LIGHT -> com.example.data.model.AppThemeMode.DARK
            com.example.data.model.AppThemeMode.DARK -> com.example.data.model.AppThemeMode.SYSTEM
            com.example.data.model.AppThemeMode.SYSTEM -> com.example.data.model.AppThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        _isDynamicColor.value = enabled
        repository.saveDynamicColor(enabled)
    }

    private val _generatedPdf = MutableStateFlow<File?>(null)
    val generatedPdf: StateFlow<File?> = _generatedPdf.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<CustomerEntity>> = _customerSearchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allCustomers
        } else {
            repository.searchCustomers(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTransactions: StateFlow<List<LedgerTransactionEntity>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun addCustomer(name: String, phone: String, address: String, gstin: String, onComplete: () -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveCustomer(
                CustomerEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim(),
                    gstin = gstin.trim()
                )
            )
            onComplete()
        }
    }

    fun recordPayment(
        customerId: Int,
        customerName: String,
        amount: Double,
        paymentMode: String,
        note: String,
        date: String = dateFormat.format(Date()),
        onComplete: () -> Unit = {}
    ) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.saveLedgerTransaction(
                LedgerTransactionEntity(
                    customerId = customerId,
                    customerName = customerName,
                    date = if (date.isBlank()) dateFormat.format(Date()) else date,
                    type = "DEBIT", // Jama / Payment Received
                    amount = amount,
                    paymentMode = paymentMode,
                    note = if (note.isBlank()) "Jama (Payment Received)" else note
                )
            )
            onComplete()
        }
    }

    fun recordCreditEntry(
        customerId: Int,
        customerName: String,
        amount: Double,
        note: String,
        date: String = dateFormat.format(Date()),
        onComplete: () -> Unit = {}
    ) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.saveLedgerTransaction(
                LedgerTransactionEntity(
                    customerId = customerId,
                    customerName = customerName,
                    date = if (date.isBlank()) dateFormat.format(Date()) else date,
                    type = "CREDIT", // Udhar / Bill Entry
                    amount = amount,
                    paymentMode = "BALANCE",
                    note = if (note.isBlank()) "Manual Udhar Entry" else note
                )
            )
            onComplete()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun updateDocumentMeta(
        logoName: String,
        invoiceNo: String,
        invoiceDate: String,
        paymentMode: String,
        upiTransactionId: String = _invoiceForm.value.upiTransactionId,
        invoiceCategory: String = _invoiceForm.value.invoiceCategory
    ) {
        _invoiceForm.value = _invoiceForm.value.copy(
            logoName = logoName,
            invoiceNo = invoiceNo,
            invoiceDate = invoiceDate,
            paymentMode = paymentMode,
            upiTransactionId = upiTransactionId,
            invoiceCategory = invoiceCategory
        )
    }

    fun updateUpiTransactionId(upiTransactionId: String) {
        _invoiceForm.value = _invoiceForm.value.copy(upiTransactionId = upiTransactionId)
    }

    fun updateInvoiceCategory(category: String) {
        _invoiceForm.value = _invoiceForm.value.copy(invoiceCategory = category)
    }

    fun updateTemplate(template: com.example.data.model.InvoiceTemplate) {
        _invoiceForm.value = _invoiceForm.value.copy(templateId = template.id)
        _generatedPdf.value = null
    }

    fun updateTemplate(templateId: String) {
        _invoiceForm.value = _invoiceForm.value.copy(templateId = templateId)
        _generatedPdf.value = null
    }

    fun updateClientDetails(custName: String, custPhone: String, custGstin: String, custAddress: String = "") {
        _invoiceForm.value = _invoiceForm.value.copy(
            custName = custName,
            custPhone = custPhone,
            custGstin = custGstin,
            custAddress = custAddress
        )
    }

    fun updateTaxConfig(taxType: String, gstRatePercent: Double, customTaxRate: Double = 0.18) {
        _invoiceForm.value = _invoiceForm.value.copy(
            taxType = taxType,
            gstRatePercent = gstRatePercent,
            customTaxRate = customTaxRate
        )
    }

    fun updateAuthSign(authSign: String) {
        _invoiceForm.value = _invoiceForm.value.copy(
            authSign = authSign
        )
    }

    fun addLineItem(
        itemName: String = "Digital Signature (Class 3)",
        itemType: String = if (_invoiceForm.value.invoiceCategory == "GOODS") "GOODS" else "SERVICES",
        hsnSacCode: String = if (_invoiceForm.value.invoiceCategory == "GOODS") "8471" else "998313",
        unit: String = "Nos"
    ) {
        val currentItems = _invoiceForm.value.items.toMutableList()
        currentItems.add(
            InvoiceLineItem(
                itemName = itemName,
                itemType = itemType,
                hsnSacCode = hsnSacCode,
                pricePerUnit = 0.0,
                quantity = 1,
                unit = unit
            )
        )
        _invoiceForm.value = _invoiceForm.value.copy(items = currentItems)
    }

    fun removeLineItem(index: Int) {
        val currentItems = _invoiceForm.value.items.toMutableList()
        if (currentItems.size > 1 && index in currentItems.indices) {
            currentItems.removeAt(index)
            _invoiceForm.value = _invoiceForm.value.copy(items = currentItems)
        }
    }

    fun updateLineItem(
        index: Int,
        name: String,
        price: Double,
        qty: Int,
        itemType: String = "SERVICES",
        hsnSacCode: String = "998313",
        unit: String = "Nos"
    ) {
        val currentItems = _invoiceForm.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = currentItems[index].copy(
                itemName = name,
                pricePerUnit = price,
                quantity = qty,
                itemType = itemType,
                hsnSacCode = hsnSacCode,
                unit = unit
            )
            _invoiceForm.value = _invoiceForm.value.copy(items = currentItems)
        }
    }

    fun generateAndSaveInvoicePdf(context: Context, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            val pdfFile = PdfGenerator.generateInvoicePdf(context, _invoiceForm.value)
            if (pdfFile != null) {
                _generatedPdf.value = pdfFile
                repository.saveInvoice(_invoiceForm.value, pdfFile.absolutePath)
            }
            _isGeneratingPdf.value = false
            onComplete(pdfFile)
        }
    }

    fun deleteInvoice(id: Int) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
        }
    }

    fun getInvoiceDataForEntity(entity: InvoiceEntity): InvoiceData {
        return repository.parseInvoiceData(entity)
    }

    fun loadInvoiceToEdit(entity: InvoiceEntity) {
        val parsed = repository.parseInvoiceData(entity)
        _invoiceForm.value = parsed
        if (entity.pdfPath != null) {
            val file = File(entity.pdfPath)
            if (file.exists()) {
                _generatedPdf.value = file
            }
        }
    }

    fun saveVendorProfile(profile: VendorProfile) {
        repository.saveVendorProfile(profile)
        _vendorProfile.value = profile
        _invoiceForm.value = _invoiceForm.value.copy(vendorProfile = profile)
    }

    fun resetForm() {
        val nextNo = ((_invoiceForm.value.invoiceNo.toIntOrNull() ?: 1000) + 1).toString()
        _invoiceForm.value = InvoiceData(
            invoiceNo = nextNo,
            invoiceDate = dateFormat.format(Date()),
            vendorProfile = _vendorProfile.value,
            authSign = _vendorProfile.value.proprietor
        )
        _generatedPdf.value = null
    }
}

class InvoiceViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
