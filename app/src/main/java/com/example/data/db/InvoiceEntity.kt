package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNo: String,
    val invoiceDate: String,
    val clientName: String,
    val clientPhone: String,
    val clientGstin: String,
    val paymentMode: String,
    val upiTransactionId: String = "",
    val invoiceCategory: String = "SERVICES",
    val subtotal: Double,
    val totalTax: Double,
    val grandTotal: Double,
    val itemsJson: String,
    val taxType: String,
    val gstRatePercent: Double = 18.0,
    val customTaxRate: Double = 0.18,
    val authSign: String,
    val templateId: String = "classic_corporate",
    val pdfPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

