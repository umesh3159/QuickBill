package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.CustomerDao
import com.example.data.db.CustomerEntity
import com.example.data.db.InvoiceDao
import com.example.data.db.InvoiceEntity
import com.example.data.db.LedgerTransactionEntity
import com.example.data.model.InvoiceData
import com.example.data.model.InvoiceLineItem
import com.example.data.model.VendorProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jmd_vendor_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val itemsListAdapter = moshi.adapter<List<InvoiceLineItem>>(
        Types.newParameterizedType(List::class.java, InvoiceLineItem::class.java)
    )

    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    fun searchInvoices(query: String): Flow<List<InvoiceEntity>> = invoiceDao.searchInvoices(query)

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

    val allTransactions: Flow<List<LedgerTransactionEntity>> = customerDao.getAllTransactions()

    fun getTransactionsForCustomer(customerId: Int): Flow<List<LedgerTransactionEntity>> =
        customerDao.getTransactionsForCustomer(customerId)

    suspend fun saveCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteTransactionsByCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }

    suspend fun saveLedgerTransaction(transaction: LedgerTransactionEntity): Long =
        customerDao.insertTransaction(transaction)

    suspend fun saveInvoice(data: InvoiceData, pdfPath: String?): Long {
        val itemsJson = itemsListAdapter.toJson(data.items)
        val entity = InvoiceEntity(
            invoiceNo = data.invoiceNo,
            invoiceDate = data.invoiceDate,
            clientName = data.custName,
            clientPhone = data.custPhone,
            clientGstin = data.custGstin,
            paymentMode = data.paymentMode,
            upiTransactionId = data.upiTransactionId,
            invoiceCategory = data.invoiceCategory,
            subtotal = data.taxableValue,
            totalTax = data.totalTax,
            grandTotal = data.grandTotal,
            itemsJson = itemsJson,
            taxType = data.taxType,
            gstRatePercent = data.gstRatePercent,
            customTaxRate = data.customTaxRate,
            authSign = data.authSign,
            templateId = data.templateId,
            pdfPath = pdfPath
        )
        val invoiceId = invoiceDao.insertInvoice(entity)

        // Automatically log/update Customer Ledger if customer name is provided
        val cName = data.custName.trim()
        if (cName.isNotBlank()) {
            var cust = customerDao.findCustomer(cName, data.custPhone.trim())
            val custId = if (cust != null) {
                // Update phone/gstin if missing
                if (data.custPhone.isNotBlank() && cust.phone.isBlank()) {
                    val updated = cust.copy(phone = data.custPhone.trim(), gstin = if (cust.gstin.isBlank()) data.custGstin.trim() else cust.gstin)
                    customerDao.insertCustomer(updated)
                }
                cust.id
            } else {
                customerDao.insertCustomer(
                    CustomerEntity(
                        name = cName,
                        phone = data.custPhone.trim(),
                        gstin = data.custGstin.trim()
                    )
                ).toInt()
            }

            // Log Credit Transaction for Bill/Invoice
            val txDate = if (data.invoiceDate.isNotBlank()) data.invoiceDate else "Today"
            customerDao.insertTransaction(
                LedgerTransactionEntity(
                    customerId = custId,
                    customerName = cName,
                    date = txDate,
                    type = "CREDIT",
                    amount = data.grandTotal,
                    paymentMode = data.paymentMode,
                    note = "Invoice #${data.invoiceNo} (${data.invoiceCategory})",
                    invoiceNo = data.invoiceNo
                )
            )

            // If Paid immediately (CASH or ONLINE PAY / UPI), log corresponding DEBIT transaction (Jama)
            if (data.paymentMode in listOf("CASH", "ONLINE PAY", "UPI")) {
                val upiNote = if (data.upiTransactionId.isNotBlank()) " (UTR: ${data.upiTransactionId})" else ""
                customerDao.insertTransaction(
                    LedgerTransactionEntity(
                        customerId = custId,
                        customerName = cName,
                        date = txDate,
                        type = "DEBIT",
                        amount = data.grandTotal,
                        paymentMode = data.paymentMode,
                        note = "Received for Invoice #${data.invoiceNo}$upiNote",
                        invoiceNo = data.invoiceNo
                    )
                )
            }
        }

        return invoiceId
    }

    suspend fun deleteInvoice(id: Int) {
        invoiceDao.deleteInvoiceById(id)
    }

    fun parseInvoiceData(entity: InvoiceEntity): InvoiceData {
        val items = try {
            itemsListAdapter.fromJson(entity.itemsJson) ?: listOf(InvoiceLineItem())
        } catch (e: Exception) {
            listOf(InvoiceLineItem())
        }

        return InvoiceData(
            invoiceNo = entity.invoiceNo,
            invoiceDate = entity.invoiceDate,
            paymentMode = entity.paymentMode,
            upiTransactionId = entity.upiTransactionId,
            invoiceCategory = entity.invoiceCategory,
            custName = entity.clientName,
            custPhone = entity.clientPhone,
            custGstin = entity.clientGstin,
            items = items,
            taxType = entity.taxType,
            gstRatePercent = entity.gstRatePercent,
            customTaxRate = entity.customTaxRate,
            authSign = entity.authSign,
            templateId = entity.templateId,
            vendorProfile = getVendorProfile()
        )
    }

    fun getVendorProfile(): VendorProfile {
        return VendorProfile(
            companyName = prefs.getString("company_name", "JMD Digital Signature Certificate Services") ?: "JMD Digital Signature Certificate Services",
            proprietor = prefs.getString("proprietor", "Umesh K Pawade") ?: "Umesh K Pawade",
            address = prefs.getString("address", "Snehdeep Nagar, Chunala Road, Bamanwada, Rajura, District Chandrapur - 442905") ?: "Snehdeep Nagar, Chunala Road, Bamanwada, Rajura, District Chandrapur - 442905",
            phone = prefs.getString("phone", "+91 8668645831") ?: "+91 8668645831",
            email = prefs.getString("email", "Umeshpawade007.com") ?: "Umeshpawade007.com",
            bankName = prefs.getString("bank_name", "Bank of India") ?: "Bank of India",
            accountNo = prefs.getString("account_no", "961510310000289") ?: "961510310000289",
            ifscCode = prefs.getString("ifsc_code", "BKID0009615") ?: "BKID0009615",
            upiHandle = prefs.getString("upi_handle", "8668645831@superyes") ?: "8668645831@superyes",
            beneficiaryName = prefs.getString("beneficiary_name", "UMESH PAWADE") ?: "UMESH PAWADE"
        )
    }

    fun saveVendorProfile(profile: VendorProfile) {
        prefs.edit()
            .putString("company_name", profile.companyName)
            .putString("proprietor", profile.proprietor)
            .putString("address", profile.address)
            .putString("phone", profile.phone)
            .putString("email", profile.email)
            .putString("bank_name", profile.bankName)
            .putString("account_no", profile.accountNo)
            .putString("ifsc_code", profile.ifscCode)
            .putString("upi_handle", profile.upiHandle)
            .putString("beneficiary_name", profile.beneficiaryName)
            .apply()
    }

    fun getThemeMode(): com.example.data.model.AppThemeMode {
        val savedId = prefs.getString("app_theme_mode", com.example.data.model.AppThemeMode.SYSTEM.id)
        return com.example.data.model.AppThemeMode.fromId(savedId)
    }

    fun saveThemeMode(mode: com.example.data.model.AppThemeMode) {
        prefs.edit().putString("app_theme_mode", mode.id).apply()
    }

    fun isDynamicColor(): Boolean {
        return prefs.getBoolean("app_dynamic_color", false)
    }

    fun saveDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean("app_dynamic_color", enabled).apply()
    }
}
