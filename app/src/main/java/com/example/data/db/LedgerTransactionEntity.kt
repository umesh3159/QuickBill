package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_transactions")
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val date: String,
    val type: String, // "CREDIT" (Bill/Udhar) or "DEBIT" (Payment Received/Jama)
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, ONLINE PAY, CHEQUE, BANK, OTHER
    val note: String = "",
    val invoiceNo: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
