package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLEncoder
import java.util.EnumMap

/**
 * Dynamic UPI QR Code generator utilizing Google's ZXing (Zebra Crossing) library.
 * Encodes standard NPCI UPI specification URLs:
 * upi://pay?pa=<Merchant UPI ID>&pn=<Beneficiary Name>&am=<Amount>&cu=INR&tn=<Invoice/Note>&tr=<Invoice Ref>
 */
object QrCodeGenerator {

    /**
     * Generates a dynamic UPI QR Code Bitmap using ZXing based on invoice total amount,
     * merchant UPI ID, beneficiary name, and invoice reference.
     */
    fun generateUpiQrBitmap(
        upiId: String,
        name: String,
        amount: Double,
        note: String = "",
        invoiceRef: String = "",
        width: Int = 400,
        height: Int = 400,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        val cleanUpiId = upiId.trim()
        if (cleanUpiId.isBlank()) return null

        return try {
            val upiUri = buildUpiUriString(
                upiId = cleanUpiId,
                name = name,
                amount = amount,
                note = note,
                invoiceRef = invoiceRef
            )

            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 1 // Minimal quiet zone for crisp rendering
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M // Medium error correction (~15%)

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, width, height, hints)

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) foregroundColor else backgroundColor)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Builds the standard NPCI-compliant UPI payment URI string.
     * Compatible with Google Pay, PhonePe, Paytm, BHIM, Cred, and all Indian banking UPI apps.
     */
    fun buildUpiUriString(
        upiId: String,
        name: String,
        amount: Double,
        note: String = "",
        invoiceRef: String = ""
    ): String {
        val cleanUpiId = upiId.trim()
        if (cleanUpiId.isBlank()) return ""

        val encodedName = URLEncoder.encode(name.ifBlank { "Merchant Store" }, "UTF-8").replace("+", "%20")
        val formattedAmount = String.format(java.util.Locale.US, "%.2f", amount)

        val sb = StringBuilder("upi://pay?pa=$cleanUpiId&pn=$encodedName&am=$formattedAmount&cu=INR")

        if (note.isNotBlank()) {
            val encodedNote = URLEncoder.encode(note.trim(), "UTF-8").replace("+", "%20")
            sb.append("&tn=$encodedNote")
        }

        if (invoiceRef.isNotBlank()) {
            val cleanRef = invoiceRef.trim().replace(" ", "")
            sb.append("&tr=$cleanRef")
        }

        return sb.toString()
    }
}
