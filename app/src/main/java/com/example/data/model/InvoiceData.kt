package com.example.data.model

data class VendorProfile(
    val companyName: String = "JMD Digital Signature Certificate Services",
    val proprietor: String = "Umesh K Pawade",
    val address: String = "Snehdeep Nagar, Chunala Road, Bamanwada, Rajura, District Chandrapur - 442905",
    val phone: String = "+91 8668645831",
    val email: String = "Umeshpawade007.com",
    val bankName: String = "Bank of India",
    val accountNo: String = "961510310000289",
    val ifscCode: String = "BKID0009615",
    val upiHandle: String = "8668645831@superyes",
    val beneficiaryName: String = "UMESH PAWADE"
)

data class InvoiceLineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val itemName: String = "Digital Signature Certificate (Class 3)",
    val itemType: String = "SERVICES", // SERVICES or GOODS
    val hsnSacCode: String = "998313", // SAC for services, HSN for goods
    val pricePerUnit: Double = 0.0,
    val quantity: Int = 1,
    val unit: String = "Nos" // Nos, Units, Pcs, Hrs, Set, Job
) {
    val subtotal: Double
        get() = pricePerUnit * quantity
}

data class InvoiceData(
    val logoName: String = "mylogo.png",
    val invoiceNo: String = "1001",
    val invoiceDate: String = "",
    val paymentMode: String = "CASH", // CASH, BALANCE, ONLINE PAY
    val upiTransactionId: String = "", // UTR / Transaction reference no
    val upiPaymentTime: String = "", // Payment timestamp
    val invoiceCategory: String = "SERVICES", // SERVICES, GOODS, BOTH
    val custName: String = "",
    val custPhone: String = "",
    val custGstin: String = "",
    val custAddress: String = "",
    val items: List<InvoiceLineItem> = listOf(InvoiceLineItem()),
    val taxType: String = "1", // 1: CGST+SGST, 2: IGST, 3: Exempt, 4: Custom / Other
    val gstRatePercent: Double = 18.0, // 0%, 5%, 12%, 18%, 28%
    val customTaxRate: Double = 0.18, // rate multiplier e.g. 0.18
    val authSign: String = "Umesh K Pawade",
    val vendorProfile: VendorProfile = VendorProfile(),
    val templateId: String = InvoiceTemplate.CLASSIC_CORPORATE.id
) {
    val template: InvoiceTemplate
        get() = InvoiceTemplate.fromId(templateId)

    val taxableValue: Double
        get() = items.sumOf { it.subtotal }

    // Effective GST rate multiplier
    val effectiveGstRate: Double
        get() = if (taxType == "3") 0.0 else (gstRatePercent / 100.0)

    val cgstRatePercent: Double
        get() = if (taxType == "1") (gstRatePercent / 2.0) else 0.0

    val sgstRatePercent: Double
        get() = if (taxType == "1") (gstRatePercent / 2.0) else 0.0

    val igstRatePercent: Double
        get() = if (taxType == "2") gstRatePercent else 0.0

    val cgstAmt: Double
        get() = if (taxType == "1") taxableValue * (cgstRatePercent / 100.0) else 0.0

    val sgstAmt: Double
        get() = if (taxType == "1") taxableValue * (sgstRatePercent / 100.0) else 0.0

    val igstAmt: Double
        get() = if (taxType == "2") taxableValue * (igstRatePercent / 100.0) else 0.0

    val customTaxAmt: Double
        get() = if (taxType == "4") taxableValue * customTaxRate else 0.0

    val totalTax: Double
        get() = cgstAmt + sgstAmt + igstAmt + customTaxAmt

    val grandTotal: Double
        get() = taxableValue + totalTax
}
