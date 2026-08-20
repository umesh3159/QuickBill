package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.InvoiceData
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 * Thermal Printer Integration Helper
 * Provides text receipt formatting (2-inch 32-col & 3-inch 48-col),
 * ESC/POS binary command generation for Bluetooth POS printers,
 * and Android Intent integration for printing & sharing.
 */
object ThermalPrinterHelper {

    /**
     * Generates a formatted plain text receipt formatted for standard thermal printers.
     * @param invoice The InvoiceData model
     * @param width Column width (32 chars for 58mm / 2-inch, 48 chars for 80mm / 3-inch)
     */
    fun generateFormattedReceiptText(invoice: InvoiceData, width: Int = 32): String {
        val sb = StringBuilder()
        val lineDivider = "-".repeat(width)
        val doubleDivider = "=".repeat(width)

        // Store Header
        val storeName = invoice.vendorProfile.companyName.ifBlank { "JMD DIGITAL SERVICES" }
        sb.appendLine(centerText(storeName.uppercase(Locale.getDefault()), width))
        if (invoice.vendorProfile.proprietor.isNotBlank()) {
            sb.appendLine(centerText("Prop: ${invoice.vendorProfile.proprietor}", width))
        }
        if (invoice.vendorProfile.address.isNotBlank()) {
            sb.appendLine(centerText(invoice.vendorProfile.address, width))
        }
        if (invoice.vendorProfile.phone.isNotBlank()) {
            sb.appendLine(centerText("Ph: ${invoice.vendorProfile.phone}", width))
        }

        sb.appendLine(lineDivider)

        // Invoice Header Details
        sb.appendLine(twoColumn("Inv No: #${invoice.invoiceNo}", "Date: ${invoice.invoiceDate}", width))
        val isOnlinePay = invoice.paymentMode in listOf("ONLINE PAY", "UPI")
        val payStatus = if (invoice.paymentMode == "BALANCE") "DUE (BALANCE)" else if (isOnlinePay) "PAID (UPI)" else "PAID (CASH)"
        sb.appendLine(twoColumn("Pay Mode: ${invoice.paymentMode}", "Status: $payStatus", width))
        if (isOnlinePay && invoice.upiTransactionId.isNotBlank()) {
            sb.appendLine("UPI UTR: ${invoice.upiTransactionId}")
        }

        if (invoice.custName.isNotBlank()) {
            sb.appendLine(lineDivider)
            sb.appendLine("CLIENT DETAILS:")
            sb.appendLine("Name : ${invoice.custName}")
            if (invoice.custPhone.isNotBlank()) sb.appendLine("Phone: ${invoice.custPhone}")
            if (invoice.custGstin.isNotBlank()) sb.appendLine("GSTIN: ${invoice.custGstin}")
        }

        sb.appendLine(doubleDivider)

        // Item Table Header
        if (width >= 48) {
            sb.appendLine(threeColumn("Item & HSN/SAC", "Qty x Rate", "Amount", width))
        } else {
            sb.appendLine("Item & HSN/SAC")
            sb.appendLine(twoColumn(" Qty x Rate", "Amount", width))
        }
        sb.appendLine(lineDivider)

        // Line Items
        for (item in invoice.items) {
            val total = item.subtotal
            val unitStr = if (item.unit.isNotBlank()) item.unit else "Nos"
            val qtyRate = "${item.quantity} $unitStr x Rs.${"%.2f".format(item.pricePerUnit)}"
            val amountStr = "Rs.${"%.2f".format(total)}"
            val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
            val itemLine = "${item.itemName} [${hsnCode}]"

            if (width >= 48) {
                val name = if (itemLine.length > 20) itemLine.take(18) + ".." else itemLine
                sb.appendLine(threeColumn(name, qtyRate, amountStr, width))
            } else {
                sb.appendLine(itemLine)
                sb.appendLine(twoColumn(" $qtyRate", amountStr, width))
            }
        }

        sb.appendLine(doubleDivider)

        // Totals & Tax
        sb.appendLine(twoColumn("Gross Subtotal:", "Rs.${"%.2f".format(invoice.taxableValue)}", width))

        when (invoice.taxType) {
            "1" -> {
                val cgstPct = invoice.cgstRatePercent
                val sgstPct = invoice.sgstRatePercent
                sb.appendLine(twoColumn("CGST (${if (cgstPct % 1.0 == 0.0) cgstPct.toInt() else cgstPct}%):", "Rs.${"%.2f".format(invoice.cgstAmt)}", width))
                sb.appendLine(twoColumn("SGST (${if (sgstPct % 1.0 == 0.0) sgstPct.toInt() else sgstPct}%):", "Rs.${"%.2f".format(invoice.sgstAmt)}", width))
            }
            "2" -> {
                val igstPct = invoice.igstRatePercent
                sb.appendLine(twoColumn("IGST (${if (igstPct % 1.0 == 0.0) igstPct.toInt() else igstPct}%):", "Rs.${"%.2f".format(invoice.igstAmt)}", width))
            }
            "4" -> {
                val taxPct = (invoice.customTaxRate * 100).toInt()
                sb.appendLine(twoColumn("Tax ($taxPct%):", "Rs.${"%.2f".format(invoice.customTaxAmt)}", width))
            }
        }

        sb.appendLine(lineDivider)
        sb.appendLine(twoColumn("GRAND TOTAL:", "Rs.${"%.2f".format(invoice.grandTotal)}", width))
        sb.appendLine(lineDivider)

        // Payment / UPI Info
        if (isOnlinePay) {
            sb.appendLine(centerText("*** PAID VIA ONLINE UPI ***", width))
            if (invoice.upiTransactionId.isNotBlank()) {
                sb.appendLine(centerText("UTR: ${invoice.upiTransactionId}", width))
            }
            sb.appendLine(centerText("UPI ID: ${invoice.vendorProfile.upiHandle}", width))
            sb.appendLine(lineDivider)
        } else if (invoice.vendorProfile.upiHandle.isNotBlank()) {
            sb.appendLine(centerText("PAY VIA UPI / QR", width))
            sb.appendLine(centerText("UPI ID: ${invoice.vendorProfile.upiHandle}", width))
            sb.appendLine(lineDivider)
        }

        // Footer
        sb.appendLine(centerText("Thank you for your business!", width))
        sb.appendLine(centerText("Please visit again!", width))
        sb.appendLine("\n\n")

        return sb.toString()
    }

    /**
     * Generates raw ESC/POS binary command bytes for Bluetooth ESC/POS printers.
     */
    fun generateEscPosBytes(invoice: InvoiceData): ByteArray {
        val stream = ByteArrayOutputStream()

        try {
            // ESC @ - Initialize printer
            stream.write(byteArrayOf(0x1B, 0x40))

            // ESC a 1 - Center align
            stream.write(byteArrayOf(0x1B, 0x61, 0x01))

            // GS ! 0x11 - Double width & double height for Header
            stream.write(byteArrayOf(0x1D, 0x21, 0x11))
            val storeName = invoice.vendorProfile.companyName.ifBlank { "JMD DIGITAL SERVICES" }
            stream.write("$storeName\n".toByteArray(Charsets.UTF_8))

            // GS ! 0x00 - Normal size
            stream.write(byteArrayOf(0x1D, 0x21, 0x00))

            if (invoice.vendorProfile.proprietor.isNotBlank()) {
                stream.write("Prop: ${invoice.vendorProfile.proprietor}\n".toByteArray(Charsets.UTF_8))
            }
            if (invoice.vendorProfile.address.isNotBlank()) {
                stream.write("${invoice.vendorProfile.address}\n".toByteArray(Charsets.UTF_8))
            }
            if (invoice.vendorProfile.phone.isNotBlank()) {
                stream.write("Ph: ${invoice.vendorProfile.phone}\n".toByteArray(Charsets.UTF_8))
            }

            // ESC a 0 - Left align
            stream.write(byteArrayOf(0x1B, 0x61, 0x00))

            val receiptText = generateFormattedReceiptText(invoice, width = 32)
            val lines = receiptText.lines()
            val textBody = lines.drop(4).joinToString("\n")
            stream.write(textBody.toByteArray(Charsets.UTF_8))

            // Feed 4 lines
            stream.write(byteArrayOf(0x1B, 0x64, 0x04))

            // GS V 66 0 - Paper Cut
            stream.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stream.toByteArray()
    }

    /**
     * Copies receipt text to clipboard and shows Toast notification.
     */
    fun copyToClipboard(context: Context, receiptText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Thermal Receipt Text", receiptText)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Thermal Receipt copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Sends formatted receipt text to external Bluetooth Print app or any sharing target.
     */
    fun shareReceipt(context: Context, receiptText: String, title: String = "Print / Share Receipt") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, receiptText)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing receipt text: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Alignment helper functions
    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun twoColumn(left: String, right: String, width: Int): String {
        val spaceNeeded = width - left.length - right.length
        return if (spaceNeeded > 0) {
            left + " ".repeat(spaceNeeded) + right
        } else {
            (left + " " + right).take(width)
        }
    }

    private fun threeColumn(col1: String, col2: String, col3: String, width: Int): String {
        val col1Width = (width * 0.45).toInt()
        val col2Width = (width * 0.30).toInt()
        val col3Width = width - col1Width - col2Width

        val c1 = col1.padEnd(col1Width).take(col1Width)
        val c2 = col2.padStart(col2Width).take(col2Width)
        val c3 = col3.padStart(col3Width).take(col3Width)
        return c1 + c2 + c3
    }
}
