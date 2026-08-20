package com.example.data.model

data class TaxMetrics(
    val taxableValue: Double = 0.0,
    val gstRatePercent: Double = 18.0,
    val cgstRate: Double = 9.0,
    val sgstRate: Double = 9.0,
    val igstRate: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val totalTax: Double = 0.0,
    val grandTotal: Double = 0.0
)

