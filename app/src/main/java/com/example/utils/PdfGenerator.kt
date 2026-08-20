package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.InvoiceData
import com.example.data.model.InvoiceTemplate
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateInvoicePdf(context: Context, invoice: InvoiceData): File? {
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        when (invoice.template) {
            InvoiceTemplate.CLASSIC_CORPORATE -> drawClassicCorporate(canvas, invoice, pageWidth, pageHeight)
            InvoiceTemplate.MODERN_MINIMAL -> drawModernMinimal(canvas, invoice, pageWidth, pageHeight)
            InvoiceTemplate.ELEGANT_EMERALD -> drawElegantEmerald(canvas, invoice, pageWidth, pageHeight)
            InvoiceTemplate.BOLD_COMPACT -> drawBoldCompact(canvas, invoice, pageWidth, pageHeight)
        }

        document.finishPage(page)

        // Save PDF to App External/Internal Storage Cache directory
        return try {
            val cleanCustName = if (invoice.custName.isNotBlank()) invoice.custName.replace(" ", "_") else "Customer"
            val cleanDate = if (invoice.invoiceDate.isNotBlank()) invoice.invoiceDate.replace("/", "-").replace(" ", "_") else "No_Date"
            val templateSuffix = invoice.template.id.take(4).uppercase()
            val fileName = "JMD_DigiSign_${cleanCustName}(${cleanDate})_inv${invoice.invoiceNo}_${templateSuffix}.pdf"

            val pdfDir = File(context.getExternalFilesDir(null), "Invoices")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    // =========================================================================
    // 1. CLASSIC CORPORATE (Executive Navy & Cerulean)
    // =========================================================================
    private fun drawClassicCorporate(canvas: Canvas, invoice: InvoiceData, pageWidth: Int, pageHeight: Int) {
        val colorPrimary = Color.parseColor("#0F3854")
        val colorSecondary = Color.parseColor("#0284C7")
        val colorCharcoal = Color.parseColor("#1E293B")
        val colorMutedLine = Color.parseColor("#CBD5E1")
        val colorLightBg = Color.parseColor("#F8FAFC")
        val colorLightContainer = Color.parseColor("#F1F5F9")
        val colorGreenTint = Color.parseColor("#ECFDF5")
        val colorGreenBorder = Color.parseColor("#10B981")
        val colorGreenText = Color.parseColor("#059669")
        val colorAmberTint = Color.parseColor("#FFFBEB")
        val colorAmberBorder = Color.parseColor("#F59E0B")
        val colorAmberText = Color.parseColor("#D97706")
        val colorGreyText = Color.parseColor("#64748B")
        val colorGoldText = Color.parseColor("#B45309")

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply { isAntiAlias = true }

        val margin = 26f
        var currentY = margin

        // --- 1. TOP BRAND BANNER / HEADER ---
        val logoBoxSize = 38f
        val logoRect = RectF(margin, currentY + 2f, margin + logoBoxSize, currentY + 2f + logoBoxSize)
        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRoundRect(logoRect, 8f, 8f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        textPaint.color = Color.WHITE
        val logoTextWidth = textPaint.measureText("JMD")
        canvas.drawText("JMD", margin + (logoBoxSize - logoTextWidth) / 2f, currentY + 24f, textPaint)

        // Company Name & Category
        val compStartX = margin + logoBoxSize + 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 14.5f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.vendorProfile.companyName, compStartX, currentY + 18f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        textPaint.color = colorGreyText
        canvas.drawText("Proprietor: ${invoice.vendorProfile.proprietor} | Ph: ${invoice.vendorProfile.phone}", compStartX, currentY + 31f, textPaint)

        val catBadgeText = "GST TAX INVOICE • ${if (invoice.invoiceCategory == "GOODS") "SUPPLY OF GOODS" else if (invoice.invoiceCategory == "BOTH") "GOODS & SERVICES" else "SUPPLY OF SERVICES"}"
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorSecondary
        canvas.drawText(catBadgeText, compStartX, currentY + 43f, textPaint)

        // Right side: TAX INVOICE title & Meta Box
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 18f
        textPaint.color = colorPrimary
        val invTitleWidth = textPaint.measureText("TAX INVOICE")
        canvas.drawText("TAX INVOICE", pageWidth - margin - invTitleWidth, currentY + 18f, textPaint)

        val metaStartX = pageWidth - margin - 170f
        var metaY = currentY + 28f

        textPaint.textSize = 8.5f
        fun drawMetaRow(label: String, value: String, valColor: Int = colorPrimary) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = colorGreyText
            canvas.drawText(label, metaStartX, metaY, textPaint)
            canvas.drawText(":", metaStartX + 60f, metaY, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = valColor
            canvas.drawText(value, metaStartX + 68f, metaY, textPaint)
            metaY += 11.5f
        }

        drawMetaRow("Invoice No", "#${invoice.invoiceNo}")
        val dateDisplay = if (invoice.invoiceDate.isBlank()) "24/07/2026" else invoice.invoiceDate
        drawMetaRow("Invoice Date", dateDisplay)

        val isOnlinePay = invoice.paymentMode in listOf("ONLINE PAY", "UPI")
        val payModeColor = if (isOnlinePay) colorGreenText else if (invoice.paymentMode == "CASH") colorPrimary else colorAmberText
        drawMetaRow("Payment Mode", invoice.paymentMode, payModeColor)

        if (isOnlinePay && invoice.upiTransactionId.isNotBlank()) {
            drawMetaRow("UPI UTR No.", invoice.upiTransactionId, colorGreenText)
        }

        val statusText = if (invoice.paymentMode == "BALANCE") "PAYMENT DUE / BALANCE" else if (isOnlinePay) "PAID VIA UPI" else "PAID (CASH)"
        val statusColor = if (invoice.paymentMode == "BALANCE") colorAmberText else colorGreenText
        drawMetaRow("Status", statusText, statusColor)

        currentY += 66f

        // Thin Divider
        paint.color = colorMutedLine
        paint.strokeWidth = 1f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 8f

        // --- 2. ADDRESS BLOCKS ---
        val boxWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val boxHeight = 84f

        // Vendor Box (Left)
        paint.style = Paint.Style.FILL
        paint.color = colorLightBg
        canvas.drawRoundRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        canvas.drawRoundRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), 4f, 4f, paint)

        var vY = currentY + 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("DETAILS OF SUPPLIER / SELLER:", margin + 8f, vY, textPaint)
        vY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorCharcoal
        canvas.drawText(invoice.vendorProfile.companyName, margin + 8f, vY, textPaint)
        vY += 10.5f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        canvas.drawText("Chunala Road, Bamanwada, Rajura, Dist Chandrapur - 442905", margin + 8f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("Phone: ${invoice.vendorProfile.phone} | Email: ${invoice.vendorProfile.email}", margin + 8f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("UPI ID: ${invoice.vendorProfile.upiHandle}", margin + 8f, vY, textPaint)

        // Client Box (Right)
        val clientX = margin + boxWidth + 10f
        paint.style = Paint.Style.FILL
        paint.color = colorLightBg
        canvas.drawRoundRect(RectF(clientX, currentY, clientX + boxWidth, currentY + boxHeight), 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        canvas.drawRoundRect(RectF(clientX, currentY, clientX + boxWidth, currentY + boxHeight), 4f, 4f, paint)

        var cY = currentY + 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("BILL TO & RECIPIENT DETAILS:", clientX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorCharcoal
        val cName = if (invoice.custName.isNotBlank()) invoice.custName else "Valued Client"
        canvas.drawText(cName, clientX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        val cPhone = if (invoice.custPhone.isNotBlank()) invoice.custPhone else "-"
        canvas.drawText("Phone / Mobile: $cPhone", clientX + 8f, cY, textPaint)
        cY += 10f

        val gstinStr = if (invoice.custGstin.isNotBlank()) invoice.custGstin else "Unregistered Consumer"
        canvas.drawText("Client GSTIN: $gstinStr", clientX + 8f, cY, textPaint)
        cY += 10f

        val cAddr = if (invoice.custAddress.isNotBlank()) invoice.custAddress else "Place of Supply: Maharashtra (27)"
        canvas.drawText(cAddr, clientX + 8f, cY, textPaint)

        currentY += boxHeight + 12f

        // --- 3. LINE ITEMS TABLE ---
        val tableWidth = pageWidth - (margin * 2)
        val colWidths = floatArrayOf(26f, 200f, 75f, 80f, 55f, 107f)

        // Header Row
        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = Color.WHITE

        var colX = margin
        val headers = arrayOf("SNo.", "Item / Service Description", "HSN/SAC", "Rate (₹)", "Qty", "Taxable Amt (₹)")
        for (i in headers.indices) {
            val alignOffset = if (i == 3 || i == 5) colWidths[i] - 8f else if (i == 4) colWidths[i] / 2f else 6f
            if (i == 3 || i == 5) textPaint.textAlign = Paint.Align.RIGHT
            else if (i == 4) textPaint.textAlign = Paint.Align.CENTER
            else textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(headers[i], colX + alignOffset, currentY + 13.5f, textPaint)
            colX += colWidths[i]
        }
        textPaint.textAlign = Paint.Align.LEFT
        currentY += 20f

        // Item Rows
        for ((idx, item) in invoice.items.withIndex()) {
            paint.style = Paint.Style.FILL
            paint.color = if (idx % 2 == 0) Color.WHITE else colorLightBg
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = colorMutedLine
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            textPaint.color = colorCharcoal

            var x = margin
            canvas.drawText("%02d".format(idx + 1), x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[0]

            val itemDesc = if (item.itemName.isNotBlank()) item.itemName else "Services Rendered"
            canvas.drawText(itemDesc, x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[1]

            val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
            val typeTag = if (item.itemType == "GOODS") "G" else "S"
            canvas.drawText("$hsnCode ($typeTag)", x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[2]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.pricePerUnit), x + colWidths[3] - 8f, currentY + 13.5f, textPaint)
            x += colWidths[3]

            textPaint.textAlign = Paint.Align.CENTER
            val unitStr = if (item.unit.isNotBlank()) item.unit else "Nos"
            canvas.drawText("${item.quantity} $unitStr", x + (colWidths[4] / 2f), currentY + 13.5f, textPaint)
            x += colWidths[4]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.subtotal), x + colWidths[5] - 8f, currentY + 13.5f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            currentY += 20f
        }

        // Summary Rows
        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, textColor: Int = colorCharcoal, isTotalRow: Boolean = false) {
            paint.style = Paint.Style.FILL
            paint.color = if (isTotalRow) colorLightContainer else colorLightBg
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = colorMutedLine
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textPaint.textSize = if (isTotalRow) 9f else 8f
            textPaint.color = textColor

            canvas.drawText(label, margin + 8f, currentY + 12.5f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, margin + tableWidth - 8f, currentY + 12.5f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += 18f
        }

        drawSummaryRow("Gross Taxable Value (A)", "₹%,.2f".format(invoice.taxableValue), isBold = true, textColor = colorPrimary)

        when (invoice.taxType) {
            "1" -> {
                val cgstPct = invoice.cgstRatePercent
                val sgstPct = invoice.sgstRatePercent
                drawSummaryRow("Central Tax (CGST) @ ${if (cgstPct % 1.0 == 0.0) cgstPct.toInt() else cgstPct}%", "₹%,.2f".format(invoice.cgstAmt))
                drawSummaryRow("State Tax (SGST) @ ${if (sgstPct % 1.0 == 0.0) sgstPct.toInt() else sgstPct}%", "₹%,.2f".format(invoice.sgstAmt))
                drawSummaryRow("Total Tax Amount (GST)", "₹%,.2f".format(invoice.totalTax), isBold = true, textColor = colorSecondary)
                drawSummaryRow("GRAND TOTAL (Taxable + GST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, textColor = colorPrimary, isTotalRow = true)
            }
            "2" -> {
                val igstPct = invoice.igstRatePercent
                drawSummaryRow("Integrated Tax (IGST) @ ${if (igstPct % 1.0 == 0.0) igstPct.toInt() else igstPct}%", "₹%,.2f".format(invoice.igstAmt))
                drawSummaryRow("Total Tax Amount (IGST)", "₹%,.2f".format(invoice.totalTax), isBold = true, textColor = colorSecondary)
                drawSummaryRow("GRAND TOTAL (Taxable + IGST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, textColor = colorPrimary, isTotalRow = true)
            }
            "4" -> {
                val taxPct = (invoice.customTaxRate * 100).toInt()
                drawSummaryRow("Service / Other Tax @ $taxPct%", "₹%,.2f".format(invoice.customTaxAmt))
                drawSummaryRow("GRAND TOTAL (Incl. Tax)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, textColor = colorPrimary, isTotalRow = true)
            }
            else -> {
                drawSummaryRow("Tax: Nil / Exempted Supply (0%)", "₹0.00")
                drawSummaryRow("GRAND TOTAL (Exempt Supply)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, textColor = colorPrimary, isTotalRow = true)
            }
        }

        currentY += 10f

        // Amount in Words
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.5f
        textPaint.color = colorGoldText
        val amtWords = NumberToWords.convertRupees(invoice.grandTotal)
        canvas.drawText("Amount in Words: $amtWords", margin, currentY, textPaint)

        currentY += 16f

        // Settlement & QR Section
        val payBoxWidth = (tableWidth - 10f) / 2f
        val payBoxHeight = 110f

        if (isOnlinePay) {
            paint.style = Paint.Style.FILL
            paint.color = colorGreenTint
            canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = colorGreenBorder
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)

            var pY = currentY + 14f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 9f
            textPaint.color = colorGreenText
            canvas.drawText("ONLINE UPI PAYMENT VERIFIED", margin + 8f, pY, textPaint)
            pY += 12f

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7.5f
            textPaint.color = colorCharcoal

            val upiLines = listOf(
                "Payment Mode: Online Digital Transfer (UPI)",
                "UPI Ref / UTR No: ${if (invoice.upiTransactionId.isNotBlank()) invoice.upiTransactionId else "Verified Electronic Transfer"}",
                "Paid To Beneficiary: ${invoice.vendorProfile.beneficiaryName}",
                "Receiving UPI ID: ${invoice.vendorProfile.upiHandle}",
                "Bank: ${invoice.vendorProfile.bankName} (A/C: ${invoice.vendorProfile.accountNo})",
                "Transaction Status: SUCCESSFUL & CREDITED"
            )
            for (line in upiLines) {
                canvas.drawText(line, margin + 8f, pY, textPaint)
                pY += 10.5f
            }

            // Right Box: Verified Paid Seal Stamp
            val qrBoxX = margin + payBoxWidth + 10f
            paint.style = Paint.Style.FILL
            paint.color = colorGreenTint
            canvas.drawRoundRect(RectF(qrBoxX, currentY, qrBoxX + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = colorGreenBorder
            canvas.drawRoundRect(RectF(qrBoxX, currentY, qrBoxX + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)

            val stampCenterX = qrBoxX + payBoxWidth / 2f
            val stampCenterY = currentY + payBoxHeight / 2f

            paint.style = Paint.Style.STROKE
            paint.color = colorGreenText
            paint.strokeWidth = 2f
            canvas.drawCircle(stampCenterX, stampCenterY, 38f, paint)

            paint.strokeWidth = 0.8f
            canvas.drawCircle(stampCenterX, stampCenterY, 34f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10.5f
            textPaint.color = colorGreenText
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAID VIA UPI", stampCenterX, stampCenterY - 6f, textPaint)

            textPaint.textSize = 7.5f
            val utrShort = if (invoice.upiTransactionId.isNotBlank()) "UTR: ${invoice.upiTransactionId}" else "ONLINE SETTLED"
            canvas.drawText(utrShort, stampCenterX, stampCenterY + 8f, textPaint)

            textPaint.textSize = 6.5f
            canvas.drawText("JMD DIGITAL SERVICES", stampCenterX, stampCenterY + 18f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        } else {
            paint.style = Paint.Style.FILL
            paint.color = colorAmberTint
            canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = colorAmberBorder
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)

            var pY = currentY + 14f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f
            textPaint.color = colorAmberText
            canvas.drawText("BANK & UPI SETTLEMENT DETAILS", margin + 8f, pY, textPaint)
            pY += 12f

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7.5f
            textPaint.color = colorCharcoal
            val bankLines = listOf(
                "Beneficiary: ${invoice.vendorProfile.beneficiaryName}",
                "Bank: ${invoice.vendorProfile.bankName}",
                "Account No: ${invoice.vendorProfile.accountNo}",
                "IFSC Code: ${invoice.vendorProfile.ifscCode}",
                "UPI Handle: ${invoice.vendorProfile.upiHandle}"
            )
            for (line in bankLines) {
                canvas.drawText(line, margin + 8f, pY, textPaint)
                pY += 11f
            }

            val qrBoxX = margin + payBoxWidth + 10f
            paint.style = Paint.Style.FILL
            paint.color = colorGreenTint
            canvas.drawRoundRect(RectF(qrBoxX, currentY, qrBoxX + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = colorGreenBorder
            canvas.drawRoundRect(RectF(qrBoxX, currentY, qrBoxX + payBoxWidth, currentY + payBoxHeight), 6f, 6f, paint)

            var qY = currentY + 14f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f
            textPaint.color = colorGreenText
            canvas.drawText("SCAN & PAY VIA UPI", qrBoxX + 8f, qY, textPaint)
            qY += 12f

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7f
            textPaint.color = colorGreyText
            val qrDescLines = listOf("Scan with GPay, PhonePe,", "Paytm or any BHIM UPI app", "to settle instantly.")
            for (line in qrDescLines) {
                canvas.drawText(line, qrBoxX + 8f, qY, textPaint)
                qY += 10f
            }

            val qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
                upiId = invoice.vendorProfile.upiHandle,
                name = invoice.vendorProfile.beneficiaryName,
                amount = invoice.grandTotal,
                note = "Invoice #${invoice.invoiceNo}",
                width = 160,
                height = 160
            )
            if (qrBitmap != null) {
                val qrSize = 72f
                val qrX = qrBoxX + payBoxWidth - qrSize - 8f
                val qrYPos = currentY + 16f
                canvas.drawBitmap(qrBitmap, null, RectF(qrX, qrYPos, qrX + qrSize, qrYPos + qrSize), null)
            }
        }

        currentY += payBoxHeight + 18f

        // Terms & Signatory
        var fY = currentY
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        val termsLines = listOf(
            "Terms & Statutory Declarations:",
            "1. Goods/Services once registered and activated cannot be refunded or transferred.",
            "2. GST input tax credit (ITC) is subject to conditions under the GST Act, 2017.",
            "3. This is an authorized digital tax invoice."
        )
        for (line in termsLines) {
            canvas.drawText(line, margin, fY, textPaint)
            fY += 9f
        }

        var sigY = currentY
        val sigX = pageWidth - margin - 150f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorCharcoal
        canvas.drawText("For ${invoice.vendorProfile.companyName}", sigX, sigY, textPaint)
        sigY += 22f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9.5f
        textPaint.color = colorPrimary
        val sigName = if (invoice.authSign.isNotBlank()) invoice.authSign else "Umesh K Pawade"
        canvas.drawText(sigName, sigX, sigY, textPaint)
        sigY += 10f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        canvas.drawText("Authorized Signatory Controller", sigX, sigY, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("- Computer Generated Legal Tax Invoice • Classic Corporate -", pageWidth / 2f, pageHeight - 16f, textPaint)
    }

    // =========================================================================
    // 2. MODERN MINIMAL (Tech Indigo & Clean Underlines)
    // =========================================================================
    private fun drawModernMinimal(canvas: Canvas, invoice: InvoiceData, pageWidth: Int, pageHeight: Int) {
        val colorPrimary = Color.parseColor("#4338CA") // Indigo
        val colorSecondary = Color.parseColor("#6366F1") // Light Indigo
        val colorCharcoal = Color.parseColor("#0F172A") // Deep Slate
        val colorMutedLine = Color.parseColor("#E2E8F0") // Soft Line
        val colorLightBg = Color.parseColor("#F8FAFC")
        val colorIndigoTint = Color.parseColor("#EEF2FF")
        val colorIndigoBorder = Color.parseColor("#C7D2FE")
        val colorGreenText = Color.parseColor("#059669")
        val colorGreenTint = Color.parseColor("#ECFDF5")
        val colorAmberText = Color.parseColor("#D97706")
        val colorGreyText = Color.parseColor("#64748B")

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply { isAntiAlias = true }

        val margin = 28f
        var currentY = 0f

        // Top Accent Stripe (Full bleed across top edge)
        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 7f, paint)

        currentY = margin + 4f

        // Top Header
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 17f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.vendorProfile.companyName, margin, currentY + 16f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        textPaint.color = colorGreyText
        canvas.drawText("Prop: ${invoice.vendorProfile.proprietor}  |  Ph: ${invoice.vendorProfile.phone}  |  ${invoice.vendorProfile.email}", margin, currentY + 30f, textPaint)

        // Right side: Modern TAX INVOICE Title and Pill
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 20f
        textPaint.color = colorCharcoal
        val titleText = "INVOICE"
        val titleW = textPaint.measureText(titleText)
        canvas.drawText(titleText, pageWidth - margin - titleW, currentY + 16f, textPaint)

        // Pill badge for category & mode
        val badgeX = pageWidth - margin - 150f
        paint.style = Paint.Style.FILL
        paint.color = colorIndigoTint
        canvas.drawRoundRect(RectF(badgeX, currentY + 22f, pageWidth - margin, currentY + 38f), 8f, 8f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 7.5f
        textPaint.color = colorPrimary
        textPaint.textAlign = Paint.Align.CENTER
        val catText = "GST TAX INVOICE • #${invoice.invoiceNo}"
        canvas.drawText(catText, badgeX + (pageWidth - margin - badgeX) / 2f, currentY + 33f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        currentY += 46f

        // Thin Indigo Line Divider
        paint.color = colorIndigoBorder
        paint.strokeWidth = 1f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 12f

        // Metadata Ribbon (Date, Payment Mode, Status)
        val isOnlinePay = invoice.paymentMode in listOf("ONLINE PAY", "UPI")
        val dateDisplay = if (invoice.invoiceDate.isBlank()) "24/07/2026" else invoice.invoiceDate

        paint.style = Paint.Style.FILL
        paint.color = colorLightBg
        canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + 24f), 6f, 6f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8f
        textPaint.color = colorGreyText
        canvas.drawText("DATE: ", margin + 10f, currentY + 15.5f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = colorCharcoal
        canvas.drawText(dateDisplay, margin + 40f, currentY + 15.5f, textPaint)

        val ribbonMid = margin + 180f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = colorGreyText
        canvas.drawText("PAYMENT: ", ribbonMid, currentY + 15.5f, textPaint)

        val payColor = if (isOnlinePay) colorGreenText else if (invoice.paymentMode == "CASH") colorPrimary else colorAmberText
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = payColor
        val payLabel = if (isOnlinePay && invoice.upiTransactionId.isNotBlank()) "UPI (UTR: ${invoice.upiTransactionId})" else invoice.paymentMode
        canvas.drawText(payLabel, ribbonMid + 50f, currentY + 15.5f, textPaint)

        val statusText = if (invoice.paymentMode == "BALANCE") "DUE / BALANCE" else "SETTLED / PAID"
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = if (invoice.paymentMode == "BALANCE") colorAmberText else colorGreenText
        canvas.drawText("● $statusText", pageWidth - margin - 10f, currentY + 15.5f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        currentY += 34f

        // Modern Clean Address Blocks with Vertical Indigo Accent Bar
        val boxWidth = (pageWidth - (margin * 2) - 16f) / 2f
        val boxHeight = 78f

        // Vendor (Left)
        paint.color = colorPrimary
        paint.strokeWidth = 3f
        canvas.drawLine(margin, currentY, margin, currentY + boxHeight, paint)

        var vY = currentY + 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorSecondary
        canvas.drawText("ISSUED BY / SELLER", margin + 8f, vY, textPaint)
        vY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorCharcoal
        canvas.drawText(invoice.vendorProfile.companyName, margin + 8f, vY, textPaint)
        vY += 10.5f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        canvas.drawText("Rajura, Dist Chandrapur - 442905", margin + 8f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("Ph: ${invoice.vendorProfile.phone}  |  UPI: ${invoice.vendorProfile.upiHandle}", margin + 8f, vY, textPaint)

        // Client (Right)
        val clientX = margin + boxWidth + 16f
        paint.color = colorSecondary
        paint.strokeWidth = 3f
        canvas.drawLine(clientX, currentY, clientX, currentY + boxHeight, paint)

        var cY = currentY + 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorSecondary
        canvas.drawText("BILLED TO / RECIPIENT", clientX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9.5f
        textPaint.color = colorCharcoal
        val cName = if (invoice.custName.isNotBlank()) invoice.custName else "Valued Client"
        canvas.drawText(cName, clientX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        val cPhone = if (invoice.custPhone.isNotBlank()) invoice.custPhone else "-"
        canvas.drawText("Phone: $cPhone", clientX + 8f, cY, textPaint)
        cY += 9.5f

        val gstinStr = if (invoice.custGstin.isNotBlank()) invoice.custGstin else "Unregistered Consumer"
        canvas.drawText("GSTIN: $gstinStr  |  POS: Maharashtra (27)", clientX + 8f, cY, textPaint)

        currentY += boxHeight + 14f

        // Table with Clean Horizontal Lines
        val tableWidth = pageWidth - (margin * 2)
        val colWidths = floatArrayOf(24f, 210f, 75f, 75f, 50f, 105f)

        // Table Header
        paint.style = Paint.Style.FILL
        paint.color = colorIndigoTint
        canvas.drawRoundRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), 4f, 4f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorPrimary

        var colX = margin
        val headers = arrayOf("#", "DESCRIPTION", "HSN/SAC", "RATE (₹)", "QTY", "TOTAL (₹)")
        for (i in headers.indices) {
            val alignOffset = if (i == 3 || i == 5) colWidths[i] - 6f else if (i == 4) colWidths[i] / 2f else 6f
            if (i == 3 || i == 5) textPaint.textAlign = Paint.Align.RIGHT
            else if (i == 4) textPaint.textAlign = Paint.Align.CENTER
            else textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(headers[i], colX + alignOffset, currentY + 13.5f, textPaint)
            colX += colWidths[i]
        }
        textPaint.textAlign = Paint.Align.LEFT
        currentY += 22f

        // Rows
        for ((idx, item) in invoice.items.withIndex()) {
            paint.color = colorMutedLine
            paint.strokeWidth = 0.6f
            canvas.drawLine(margin, currentY + 18f, margin + tableWidth, currentY + 18f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            textPaint.color = colorCharcoal

            var x = margin
            canvas.drawText("%02d".format(idx + 1), x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[0]

            val itemDesc = if (item.itemName.isNotBlank()) item.itemName else "Services Rendered"
            canvas.drawText(itemDesc, x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[1]

            val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
            canvas.drawText("$hsnCode (${if (item.itemType == "GOODS") "Goods" else "Service"})", x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[2]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.pricePerUnit), x + colWidths[3] - 6f, currentY + 12.5f, textPaint)
            x += colWidths[3]

            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("${item.quantity} ${item.unit}", x + (colWidths[4] / 2f), currentY + 12.5f, textPaint)
            x += colWidths[4]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.subtotal), x + colWidths[5] - 6f, currentY + 12.5f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            currentY += 19f
        }

        // Summary Rows (Modern Pill Box on Right)
        val sumBoxWidth = 230f
        val sumBoxX = pageWidth - margin - sumBoxWidth
        var sY = currentY + 6f

        fun drawSumLine(label: String, valStr: String, isBold: Boolean = false, isFinal: Boolean = false) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textPaint.textSize = if (isFinal) 9.5f else 8f
            textPaint.color = if (isFinal) colorPrimary else colorCharcoal
            canvas.drawText(label, sumBoxX, sY, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(valStr, pageWidth - margin, sY, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            sY += if (isFinal) 14f else 11.5f
        }

        drawSumLine("Taxable Amount:", "₹%,.2f".format(invoice.taxableValue), isBold = true)
        when (invoice.taxType) {
            "1" -> {
                drawSumLine("CGST @ ${invoice.cgstRatePercent}%:", "₹%,.2f".format(invoice.cgstAmt))
                drawSumLine("SGST @ ${invoice.sgstRatePercent}%:", "₹%,.2f".format(invoice.sgstAmt))
            }
            "2" -> drawSumLine("IGST @ ${invoice.igstRatePercent}%:", "₹%,.2f".format(invoice.igstAmt))
            "4" -> drawSumLine("Custom Tax:", "₹%,.2f".format(invoice.customTaxAmt))
            else -> drawSumLine("Tax (0% Exempt):", "₹0.00")
        }

        paint.color = colorIndigoBorder
        paint.strokeWidth = 1f
        canvas.drawLine(sumBoxX, sY - 4f, pageWidth - margin, sY - 4f, paint)

        // Modern Final Total Box
        paint.style = Paint.Style.FILL
        paint.color = colorIndigoTint
        canvas.drawRoundRect(RectF(sumBoxX - 4f, sY - 2f, pageWidth - margin, sY + 16f), 4f, 4f, paint)
        sY += 10f
        drawSumLine("GRAND TOTAL:", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isFinal = true)

        currentY = sY + 12f

        // Amount in Words
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textSize = 8f
        textPaint.color = colorSecondary
        canvas.drawText("Amount in Words: ${NumberToWords.convertRupees(invoice.grandTotal)}", margin, currentY, textPaint)

        currentY += 14f

        // Modern Payment Card & QR Box
        val pCardW = (tableWidth - 12f) / 2f
        val pCardH = 95f

        paint.style = Paint.Style.FILL
        paint.color = if (isOnlinePay) colorGreenTint else colorLightBg
        canvas.drawRoundRect(RectF(margin, currentY, margin + pCardW, currentY + pCardH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = if (isOnlinePay) colorGreenText else colorIndigoBorder
        canvas.drawRoundRect(RectF(margin, currentY, margin + pCardW, currentY + pCardH), 6f, 6f, paint)

        var payY = currentY + 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = if (isOnlinePay) colorGreenText else colorPrimary
        canvas.drawText(if (isOnlinePay) "✔ ONLINE SETTLED (UPI)" else "SETTLEMENT & BANK", margin + 8f, payY, textPaint)
        payY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorCharcoal
        val pLines = if (isOnlinePay) listOf(
            "UTR Ref: ${if (invoice.upiTransactionId.isNotBlank()) invoice.upiTransactionId else "Verified Online"}",
            "Payee: ${invoice.vendorProfile.beneficiaryName}",
            "UPI: ${invoice.vendorProfile.upiHandle}",
            "Bank: ${invoice.vendorProfile.bankName}"
        ) else listOf(
            "Bank: ${invoice.vendorProfile.bankName}",
            "A/C: ${invoice.vendorProfile.accountNo}",
            "IFSC: ${invoice.vendorProfile.ifscCode}",
            "UPI: ${invoice.vendorProfile.upiHandle}"
        )
        for (l in pLines) {
            canvas.drawText(l, margin + 8f, payY, textPaint)
            payY += 9.5f
        }

        // Right side: QR Box or Verified Paid Badge
        val rightBoxX = margin + pCardW + 12f
        paint.style = Paint.Style.FILL
        paint.color = colorLightBg
        canvas.drawRoundRect(RectF(rightBoxX, currentY, rightBoxX + pCardW, currentY + pCardH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorIndigoBorder
        canvas.drawRoundRect(RectF(rightBoxX, currentY, rightBoxX + pCardW, currentY + pCardH), 6f, 6f, paint)

        if (isOnlinePay) {
            val cX = rightBoxX + pCardW / 2f
            val cY = currentY + pCardH / 2f
            paint.style = Paint.Style.STROKE
            paint.color = colorGreenText
            paint.strokeWidth = 1.5f
            canvas.drawRoundRect(RectF(cX - 55f, cY - 22f, cX + 55f, cY + 22f), 8f, 8f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 9.5f
            textPaint.color = colorGreenText
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAID VIA UPI", cX, cY - 4f, textPaint)
            textPaint.textSize = 7f
            canvas.drawText("VERIFIED TRANSFER", cX, cY + 8f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        } else {
            val qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
                upiId = invoice.vendorProfile.upiHandle,
                name = invoice.vendorProfile.beneficiaryName,
                amount = invoice.grandTotal,
                note = "Invoice #${invoice.invoiceNo}",
                width = 140,
                height = 140
            )
            if (qrBitmap != null) {
                val qSize = 65f
                val qX = rightBoxX + pCardW - qSize - 8f
                val qY = currentY + 15f
                canvas.drawBitmap(qrBitmap, null, RectF(qX, qY, qX + qSize, qY + qSize), null)
            }
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8f
            textPaint.color = colorPrimary
            canvas.drawText("INSTANT UPI QR", rightBoxX + 8f, currentY + 16f, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 6.5f
            textPaint.color = colorGreyText
            canvas.drawText("Scan with GPay/PhonePe", rightBoxX + 8f, currentY + 28f, textPaint)
        }

        currentY += pCardH + 16f

        // Bottom Signature
        val sigX = pageWidth - margin - 150f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorCharcoal
        canvas.drawText("For ${invoice.vendorProfile.companyName}", sigX, currentY, textPaint)
        currentY += 20f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.authSign, sigX, currentY, textPaint)
        currentY += 9f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        canvas.drawText("Authorized Signatory Controller", sigX, currentY, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("- Electronic GST Tax Invoice • Modern Minimal Template -", pageWidth / 2f, pageHeight - 16f, textPaint)
    }

    // =========================================================================
    // 3. ELEGANT EMERALD (Prestige Emerald & Forest Green)
    // =========================================================================
    private fun drawElegantEmerald(canvas: Canvas, invoice: InvoiceData, pageWidth: Int, pageHeight: Int) {
        val colorPrimary = Color.parseColor("#065F46") // Deep Forest Emerald
        val colorSecondary = Color.parseColor("#059669") // Vibrant Emerald
        val colorCharcoal = Color.parseColor("#1F2937")
        val colorMutedLine = Color.parseColor("#A7F3D0") // Mint border
        val colorEmeraldTint = Color.parseColor("#ECFDF5") // Mint tint
        val colorGoldAccent = Color.parseColor("#D97706") // Warm Gold
        val colorDarkGold = Color.parseColor("#B45309")
        val colorGreyText = Color.parseColor("#4B5563")

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply { isAntiAlias = true }

        val margin = 26f
        var currentY = margin

        // Double Gold Decorative Border at Top
        paint.color = colorGoldAccent
        paint.strokeWidth = 2f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        paint.strokeWidth = 0.6f
        canvas.drawLine(margin, currentY + 3f, pageWidth - margin, currentY + 3f, paint)

        currentY += 12f

        // Header: Stylized Crest Badge
        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        val crestRect = RectF(margin, currentY, margin + 42f, currentY + 42f)
        canvas.drawRoundRect(crestRect, 10f, 10f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        textPaint.color = Color.WHITE
        val emblemW = textPaint.measureText("JMD")
        canvas.drawText("JMD", margin + (42f - emblemW) / 2f, currentY + 25f, textPaint)

        // Company Title
        val compX = margin + 50f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textPaint.textSize = 15f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.vendorProfile.companyName, compX, currentY + 18f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        textPaint.color = colorGreyText
        canvas.drawText("Proprietor: ${invoice.vendorProfile.proprietor}  •  Ph: ${invoice.vendorProfile.phone}", compX, currentY + 31f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorDarkGold
        val suppType = if (invoice.invoiceCategory == "GOODS") "SUPPLY OF GOODS" else if (invoice.invoiceCategory == "BOTH") "GOODS & SERVICES" else "SUPPLY OF SERVICES"
        canvas.drawText("✦ GST TAX INVOICE  |  $suppType ✦", compX, currentY + 42f, textPaint)

        // Right side: Gold Framed Invoice No & Date
        val rightW = 160f
        val rightX = pageWidth - margin - rightW
        paint.style = Paint.Style.FILL
        paint.color = colorEmeraldTint
        canvas.drawRoundRect(RectF(rightX, currentY, pageWidth - margin, currentY + 44f), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        canvas.drawRoundRect(RectF(rightX, currentY, pageWidth - margin, currentY + 44f), 6f, 6f, paint)

        var rY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        textPaint.color = colorPrimary
        canvas.drawText("TAX INVOICE", rightX + 8f, rY, textPaint)
        rY += 12f

        textPaint.textSize = 8f
        textPaint.color = colorCharcoal
        canvas.drawText("Inv No: #${invoice.invoiceNo}", rightX + 8f, rY, textPaint)
        rY += 10.5f

        val dStr = if (invoice.invoiceDate.isBlank()) "24/07/2026" else invoice.invoiceDate
        canvas.drawText("Date: $dStr", rightX + 8f, rY, textPaint)

        currentY += 54f

        // Vendor & Client Cards with Emerald Borders
        val boxWidth = (pageWidth - (margin * 2) - 12f) / 2f
        val boxHeight = 82f

        paint.style = Paint.Style.FILL
        paint.color = colorEmeraldTint
        canvas.drawRoundRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        canvas.drawRoundRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), 6f, 6f, paint)

        var vY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("SUPPLIER / SERVICE PROVIDER", margin + 8f, vY, textPaint)
        vY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorCharcoal
        canvas.drawText(invoice.vendorProfile.companyName, margin + 8f, vY, textPaint)
        vY += 10.5f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        canvas.drawText("Rajura, Dist Chandrapur - 442905", margin + 8f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("UPI Handle: ${invoice.vendorProfile.upiHandle}", margin + 8f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("Bank: ${invoice.vendorProfile.bankName} (${invoice.vendorProfile.ifscCode})", margin + 8f, vY, textPaint)

        // Client Box
        val cX = margin + boxWidth + 12f
        paint.style = Paint.Style.FILL
        paint.color = colorEmeraldTint
        canvas.drawRoundRect(RectF(cX, currentY, cX + boxWidth, currentY + boxHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        canvas.drawRoundRect(RectF(cX, currentY, cX + boxWidth, currentY + boxHeight), 6f, 6f, paint)

        var cY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("CLIENT / RECIPIENT", cX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorCharcoal
        val cName = if (invoice.custName.isNotBlank()) invoice.custName else "Valued Client"
        canvas.drawText(cName, cX + 8f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        val cPhone = if (invoice.custPhone.isNotBlank()) invoice.custPhone else "-"
        canvas.drawText("Phone: $cPhone", cX + 8f, cY, textPaint)
        cY += 9.5f

        val gstinStr = if (invoice.custGstin.isNotBlank()) invoice.custGstin else "Unregistered Consumer"
        canvas.drawText("GSTIN: $gstinStr", cX + 8f, cY, textPaint)
        cY += 9.5f
        canvas.drawText("Place of Supply: Maharashtra (27)", cX + 8f, cY, textPaint)

        currentY += boxHeight + 12f

        // Table with Forest Emerald Header & Gold Accent
        val tableWidth = pageWidth - (margin * 2)
        val colWidths = floatArrayOf(26f, 200f, 75f, 80f, 55f, 107f)

        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRoundRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), 4f, 4f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = Color.WHITE

        var colX = margin
        val headers = arrayOf("SNo.", "Item / Service Particulars", "HSN/SAC", "Price (₹)", "Qty", "Amount (₹)")
        for (i in headers.indices) {
            val alignOffset = if (i == 3 || i == 5) colWidths[i] - 8f else if (i == 4) colWidths[i] / 2f else 6f
            if (i == 3 || i == 5) textPaint.textAlign = Paint.Align.RIGHT
            else if (i == 4) textPaint.textAlign = Paint.Align.CENTER
            else textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(headers[i], colX + alignOffset, currentY + 13.5f, textPaint)
            colX += colWidths[i]
        }
        textPaint.textAlign = Paint.Align.LEFT
        currentY += 20f

        // Item Rows
        for ((idx, item) in invoice.items.withIndex()) {
            paint.style = Paint.Style.FILL
            paint.color = if (idx % 2 == 0) Color.WHITE else colorEmeraldTint
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = colorMutedLine
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 20f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            textPaint.color = colorCharcoal

            var x = margin
            canvas.drawText("%02d".format(idx + 1), x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[0]

            val itemDesc = if (item.itemName.isNotBlank()) item.itemName else "Services Rendered"
            canvas.drawText(itemDesc, x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[1]

            val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
            canvas.drawText("$hsnCode (${if (item.itemType == "GOODS") "Goods" else "Service"})", x + 6f, currentY + 13.5f, textPaint)
            x += colWidths[2]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.pricePerUnit), x + colWidths[3] - 8f, currentY + 13.5f, textPaint)
            x += colWidths[3]

            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("${item.quantity} ${item.unit}", x + (colWidths[4] / 2f), currentY + 13.5f, textPaint)
            x += colWidths[4]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.subtotal), x + colWidths[5] - 8f, currentY + 13.5f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            currentY += 20f
        }

        // Summary Rows
        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, isTotalRow: Boolean = false) {
            paint.style = Paint.Style.FILL
            paint.color = if (isTotalRow) colorEmeraldTint else Color.WHITE
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = if (isTotalRow) colorPrimary else colorMutedLine
            paint.strokeWidth = if (isTotalRow) 1.5f else 1f
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textPaint.textSize = if (isTotalRow) 9.5f else 8f
            textPaint.color = if (isTotalRow) colorPrimary else colorCharcoal

            canvas.drawText(label, margin + 8f, currentY + 12.5f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, margin + tableWidth - 8f, currentY + 12.5f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += 18f
        }

        drawSummaryRow("Taxable Value", "₹%,.2f".format(invoice.taxableValue), isBold = true)
        when (invoice.taxType) {
            "1" -> {
                drawSummaryRow("CGST @ ${invoice.cgstRatePercent}%", "₹%,.2f".format(invoice.cgstAmt))
                drawSummaryRow("SGST @ ${invoice.sgstRatePercent}%", "₹%,.2f".format(invoice.sgstAmt))
                drawSummaryRow("GRAND TOTAL (Taxable + GST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
            }
            "2" -> {
                drawSummaryRow("IGST @ ${invoice.igstRatePercent}%", "₹%,.2f".format(invoice.igstAmt))
                drawSummaryRow("GRAND TOTAL (Taxable + IGST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
            }
            else -> drawSummaryRow("GRAND TOTAL (Exempt)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
        }

        currentY += 10f

        // Amount in Words
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.5f
        textPaint.color = colorDarkGold
        canvas.drawText("Amount in Words: ${NumberToWords.convertRupees(invoice.grandTotal)}", margin, currentY, textPaint)

        currentY += 16f

        // Payment Settlement Section with Emerald Stamp
        val isOnlinePay = invoice.paymentMode in listOf("ONLINE PAY", "UPI")
        val payBoxW = (tableWidth - 10f) / 2f
        val payBoxH = 100f

        paint.style = Paint.Style.FILL
        paint.color = colorEmeraldTint
        canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxW, currentY + payBoxH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorSecondary
        canvas.drawRoundRect(RectF(margin, currentY, margin + payBoxW, currentY + payBoxH), 6f, 6f, paint)

        var payY = currentY + 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("SETTLEMENT & PAYMENT DETAILS", margin + 8f, payY, textPaint)
        payY += 11.5f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorCharcoal
        val infoLines = if (isOnlinePay) listOf(
            "Mode: Online UPI Transfer (Settled)",
            "UTR No: ${if (invoice.upiTransactionId.isNotBlank()) invoice.upiTransactionId else "Electronic Transfer"}",
            "Payee: ${invoice.vendorProfile.beneficiaryName}",
            "UPI: ${invoice.vendorProfile.upiHandle}",
            "Bank: ${invoice.vendorProfile.bankName}"
        ) else listOf(
            "Beneficiary: ${invoice.vendorProfile.beneficiaryName}",
            "Bank: ${invoice.vendorProfile.bankName}",
            "Account No: ${invoice.vendorProfile.accountNo}",
            "IFSC Code: ${invoice.vendorProfile.ifscCode}",
            "UPI Handle: ${invoice.vendorProfile.upiHandle}"
        )
        for (l in infoLines) {
            canvas.drawText(l, margin + 8f, payY, textPaint)
            payY += 10.5f
        }

        // Right box: Starburst Verified Stamp or QR
        val rX = margin + payBoxW + 10f
        paint.style = Paint.Style.FILL
        paint.color = colorEmeraldTint
        canvas.drawRoundRect(RectF(rX, currentY, rX + payBoxW, currentY + payBoxH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorSecondary
        canvas.drawRoundRect(RectF(rX, currentY, rX + payBoxW, currentY + payBoxH), 6f, 6f, paint)

        if (isOnlinePay) {
            val sX = rX + payBoxW / 2f
            val sY = currentY + payBoxH / 2f
            paint.style = Paint.Style.STROKE
            paint.color = colorSecondary
            paint.strokeWidth = 2f
            canvas.drawCircle(sX, sY, 36f, paint)
            paint.strokeWidth = 0.8f
            canvas.drawCircle(sX, sY, 32f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10f
            textPaint.color = colorPrimary
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("✦ PAID VIA UPI ✦", sX, sY - 5f, textPaint)
            textPaint.textSize = 7.5f
            val utrShort = if (invoice.upiTransactionId.isNotBlank()) "UTR: ${invoice.upiTransactionId}" else "VERIFIED"
            canvas.drawText(utrShort, sX, sY + 8f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        } else {
            val qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
                upiId = invoice.vendorProfile.upiHandle,
                name = invoice.vendorProfile.beneficiaryName,
                amount = invoice.grandTotal,
                note = "Invoice #${invoice.invoiceNo}",
                width = 160,
                height = 160
            )
            if (qrBitmap != null) {
                val qSize = 68f
                val qX = rX + payBoxW - qSize - 8f
                val qY = currentY + 16f
                canvas.drawBitmap(qrBitmap, null, RectF(qX, qY, qX + qSize, qY + qSize), null)
            }
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f
            textPaint.color = colorPrimary
            canvas.drawText("INSTANT UPI SCAN", rX + 8f, currentY + 16f, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7f
            textPaint.color = colorGreyText
            canvas.drawText("Scan to settle directly", rX + 8f, currentY + 28f, textPaint)
        }

        currentY += payBoxH + 16f

        // Bottom Signature
        val sigX = pageWidth - margin - 150f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorCharcoal
        canvas.drawText("For ${invoice.vendorProfile.companyName}", sigX, currentY, textPaint)
        currentY += 20f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.authSign, sigX, currentY, textPaint)
        currentY += 9f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        canvas.drawText("Authorized Signatory Controller", sigX, currentY, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("- Computer Generated Legal Tax Invoice • Emerald Prestige Template -", pageWidth / 2f, pageHeight - 16f, textPaint)
    }

    // =========================================================================
    // 4. BOLD COMPACT (High-Contrast Charcoal & Crimson Header)
    // =========================================================================
    private fun drawBoldCompact(canvas: Canvas, invoice: InvoiceData, pageWidth: Int, pageHeight: Int) {
        val colorPrimary = Color.parseColor("#1E293B") // Dark Slate Charcoal
        val colorSecondary = Color.parseColor("#BE123C") // Vivid Crimson
        val colorCharcoal = Color.parseColor("#0F172A")
        val colorMutedLine = Color.parseColor("#94A3B8")
        val colorLightBg = Color.parseColor("#F1F5F9")
        val colorGreenText = Color.parseColor("#059669")
        val colorGreenTint = Color.parseColor("#ECFDF5")
        val colorCrimsonTint = Color.parseColor("#FFE4E6")
        val colorGreyText = Color.parseColor("#475569")

        val paint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply { isAntiAlias = true }

        val margin = 24f
        var currentY = 0f

        // 1. FULL SOLID CHARCOAL TOP BANNER
        val bannerHeight = 56f
        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), bannerHeight, paint)

        // Company Name & Subtitle in White
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 15f
        textPaint.color = Color.WHITE
        canvas.drawText(invoice.vendorProfile.companyName, margin, 24f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8f
        textPaint.color = Color.parseColor("#CBD5E1")
        canvas.drawText("Prop: ${invoice.vendorProfile.proprietor}  |  Ph: ${invoice.vendorProfile.phone}  |  Rajura - 442905", margin, 38f, textPaint)

        // Right side: Crimson TAX INVOICE Badge
        val badgeW = 120f
        val badgeH = 22f
        val badgeX = pageWidth - margin - badgeW
        paint.style = Paint.Style.FILL
        paint.color = colorSecondary
        canvas.drawRoundRect(RectF(badgeX, 16f, badgeX + badgeW, 16f + badgeH), 4f, 4f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9.5f
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("TAX INVOICE", badgeX + badgeW / 2f, 30.5f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        currentY = bannerHeight + 10f

        // High-Contrast Metadata Bar
        paint.style = Paint.Style.FILL
        paint.color = colorLightBg
        canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + 22f), 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorMutedLine
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + 22f), 4f, 4f, paint)

        val dStr = if (invoice.invoiceDate.isBlank()) "24/07/2026" else invoice.invoiceDate
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorCharcoal
        canvas.drawText("INVOICE NO: #${invoice.invoiceNo}", margin + 8f, currentY + 14.5f, textPaint)
        canvas.drawText("DATE: $dStr", margin + 140f, currentY + 14.5f, textPaint)

        val isOnlinePay = invoice.paymentMode in listOf("ONLINE PAY", "UPI")
        val payModeStr = if (isOnlinePay) "PAID VIA UPI" else if (invoice.paymentMode == "CASH") "PAID (CASH)" else "BALANCE (DUE)"
        textPaint.color = if (invoice.paymentMode == "BALANCE") colorSecondary else colorGreenText
        canvas.drawText("STATUS: $payModeStr", margin + 260f, currentY + 14.5f, textPaint)

        if (isOnlinePay && invoice.upiTransactionId.isNotBlank()) {
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = colorGreenText
            canvas.drawText("UTR: ${invoice.upiTransactionId}", pageWidth - margin - 8f, currentY + 14.5f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        currentY += 32f

        // Client & Supplier High-Contrast Boxes
        val boxWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val boxHeight = 76f

        // Supplier Box
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorPrimary
        paint.strokeWidth = 1f
        canvas.drawRect(RectF(margin, currentY, margin + boxWidth, currentY + boxHeight), paint)

        var vY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorPrimary
        canvas.drawText("SELLER / SUPPLIER", margin + 6f, vY, textPaint)
        vY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorCharcoal
        canvas.drawText(invoice.vendorProfile.companyName, margin + 6f, vY, textPaint)
        vY += 10.5f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        canvas.drawText("Ph: ${invoice.vendorProfile.phone}  |  UPI: ${invoice.vendorProfile.upiHandle}", margin + 6f, vY, textPaint)
        vY += 9.5f
        canvas.drawText("Bank: ${invoice.vendorProfile.bankName} (A/C: ${invoice.vendorProfile.accountNo})", margin + 6f, vY, textPaint)

        // Client Box
        val cX = margin + boxWidth + 10f
        paint.style = Paint.Style.FILL
        paint.color = colorCrimsonTint
        canvas.drawRect(RectF(cX, currentY, cX + boxWidth, currentY + boxHeight), paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorSecondary
        paint.strokeWidth = 1f
        canvas.drawRect(RectF(cX, currentY, cX + boxWidth, currentY + boxHeight), paint)

        var cY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = colorSecondary
        canvas.drawText("BILLED TO (CLIENT)", cX + 6f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorCharcoal
        val cName = if (invoice.custName.isNotBlank()) invoice.custName else "Valued Client"
        canvas.drawText(cName, cX + 6f, cY, textPaint)
        cY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorGreyText
        val cPhone = if (invoice.custPhone.isNotBlank()) invoice.custPhone else "-"
        canvas.drawText("Phone: $cPhone", cX + 6f, cY, textPaint)
        cY += 9.5f

        val gstinStr = if (invoice.custGstin.isNotBlank()) invoice.custGstin else "Unregistered Consumer"
        canvas.drawText("GSTIN: $gstinStr", cX + 6f, cY, textPaint)

        currentY += boxHeight + 12f

        // Table with Solid Charcoal Header
        val tableWidth = pageWidth - (margin * 2)
        val colWidths = floatArrayOf(24f, 204f, 75f, 80f, 55f, 109f)

        paint.style = Paint.Style.FILL
        paint.color = colorPrimary
        canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = Color.WHITE

        var colX = margin
        val headers = arrayOf("#", "ITEM / SERVICE NAME", "HSN/SAC", "PRICE (₹)", "QTY", "SUBTOTAL (₹)")
        for (i in headers.indices) {
            val alignOffset = if (i == 3 || i == 5) colWidths[i] - 6f else if (i == 4) colWidths[i] / 2f else 6f
            if (i == 3 || i == 5) textPaint.textAlign = Paint.Align.RIGHT
            else if (i == 4) textPaint.textAlign = Paint.Align.CENTER
            else textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(headers[i], colX + alignOffset, currentY + 12.5f, textPaint)
            colX += colWidths[i]
        }
        textPaint.textAlign = Paint.Align.LEFT
        currentY += 18f

        // Rows
        for ((idx, item) in invoice.items.withIndex()) {
            paint.style = Paint.Style.FILL
            paint.color = if (idx % 2 == 0) Color.WHITE else colorLightBg
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = colorMutedLine
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            textPaint.color = colorCharcoal

            var x = margin
            canvas.drawText("%02d".format(idx + 1), x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[0]

            val itemDesc = if (item.itemName.isNotBlank()) item.itemName else "Services Rendered"
            canvas.drawText(itemDesc, x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[1]

            val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
            canvas.drawText("$hsnCode (${if (item.itemType == "GOODS") "G" else "S"})", x + 6f, currentY + 12.5f, textPaint)
            x += colWidths[2]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.pricePerUnit), x + colWidths[3] - 6f, currentY + 12.5f, textPaint)
            x += colWidths[3]

            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("${item.quantity} ${item.unit}", x + (colWidths[4] / 2f), currentY + 12.5f, textPaint)
            x += colWidths[4]

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹%,.2f".format(item.subtotal), x + colWidths[5] - 6f, currentY + 12.5f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        // Summary Rows
        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, isTotalRow: Boolean = false) {
            paint.style = Paint.Style.FILL
            paint.color = if (isTotalRow) colorSecondary else colorLightBg
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            paint.style = Paint.Style.STROKE
            paint.color = if (isTotalRow) colorSecondary else colorMutedLine
            canvas.drawRect(RectF(margin, currentY, margin + tableWidth, currentY + 18f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textPaint.textSize = if (isTotalRow) 9.5f else 8f
            textPaint.color = if (isTotalRow) Color.WHITE else colorCharcoal

            canvas.drawText(label, margin + 8f, currentY + 12.5f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, margin + tableWidth - 8f, currentY + 12.5f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += 18f
        }

        drawSummaryRow("TAXABLE VALUE", "₹%,.2f".format(invoice.taxableValue), isBold = true)
        when (invoice.taxType) {
            "1" -> {
                drawSummaryRow("CGST @ ${invoice.cgstRatePercent}%", "₹%,.2f".format(invoice.cgstAmt))
                drawSummaryRow("SGST @ ${invoice.sgstRatePercent}%", "₹%,.2f".format(invoice.sgstAmt))
                drawSummaryRow("GRAND TOTAL (Taxable + GST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
            }
            "2" -> {
                drawSummaryRow("IGST @ ${invoice.igstRatePercent}%", "₹%,.2f".format(invoice.igstAmt))
                drawSummaryRow("GRAND TOTAL (Taxable + IGST)", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
            }
            else -> drawSummaryRow("GRAND TOTAL", "Rs. %,.2f".format(invoice.grandTotal), isBold = true, isTotalRow = true)
        }

        currentY += 8f

        // Amount in Words
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8f
        textPaint.color = colorSecondary
        canvas.drawText("Amount in Words: ${NumberToWords.convertRupees(invoice.grandTotal)}", margin, currentY, textPaint)

        currentY += 14f

        // Spotlight Settlement & QR Card
        val payBoxW = (tableWidth - 10f) / 2f
        val payBoxH = 95f

        paint.style = Paint.Style.FILL
        paint.color = if (isOnlinePay) colorGreenTint else colorLightBg
        canvas.drawRect(RectF(margin, currentY, margin + payBoxW, currentY + payBoxH), paint)
        paint.style = Paint.Style.STROKE
        paint.color = if (isOnlinePay) colorGreenText else colorPrimary
        paint.strokeWidth = 1f
        canvas.drawRect(RectF(margin, currentY, margin + payBoxW, currentY + payBoxH), paint)

        var payY = currentY + 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.color = if (isOnlinePay) colorGreenText else colorPrimary
        canvas.drawText("PAYMENT SETTLEMENT", margin + 8f, payY, textPaint)
        payY += 11f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorCharcoal
        val lines = if (isOnlinePay) listOf(
            "Status: PAID VIA UPI (Settled)",
            "UTR / Ref No: ${if (invoice.upiTransactionId.isNotBlank()) invoice.upiTransactionId else "Online Transfer"}",
            "Payee: ${invoice.vendorProfile.beneficiaryName}",
            "UPI: ${invoice.vendorProfile.upiHandle}"
        ) else listOf(
            "Payee: ${invoice.vendorProfile.beneficiaryName}",
            "Bank: ${invoice.vendorProfile.bankName}",
            "A/C: ${invoice.vendorProfile.accountNo}",
            "IFSC: ${invoice.vendorProfile.ifscCode}"
        )
        for (l in lines) {
            canvas.drawText(l, margin + 8f, payY, textPaint)
            payY += 10f
        }

        // Right box: QR or Stamp
        val rX = margin + payBoxW + 10f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRect(RectF(rX, currentY, rX + payBoxW, currentY + payBoxH), paint)
        paint.style = Paint.Style.STROKE
        paint.color = colorPrimary
        canvas.drawRect(RectF(rX, currentY, rX + payBoxW, currentY + payBoxH), paint)

        if (isOnlinePay) {
            val sX = rX + payBoxW / 2f
            val sY = currentY + payBoxH / 2f
            paint.style = Paint.Style.STROKE
            paint.color = colorGreenText
            paint.strokeWidth = 2f
            canvas.drawRect(RectF(sX - 50f, sY - 20f, sX + 50f, sY + 20f), paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10f
            textPaint.color = colorGreenText
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAID VIA UPI", sX, sY - 4f, textPaint)
            textPaint.textSize = 7f
            canvas.drawText("SETTLED", sX, sY + 8f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        } else {
            val qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
                upiId = invoice.vendorProfile.upiHandle,
                name = invoice.vendorProfile.beneficiaryName,
                amount = invoice.grandTotal,
                note = "Invoice #${invoice.invoiceNo}",
                width = 160,
                height = 160
            )
            if (qrBitmap != null) {
                val qSize = 65f
                val qX = rX + payBoxW - qSize - 8f
                val qY = currentY + 15f
                canvas.drawBitmap(qrBitmap, null, RectF(qX, qY, qX + qSize, qY + qSize), null)
            }
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f
            textPaint.color = colorPrimary
            canvas.drawText("SCAN & PAY (UPI)", rX + 8f, currentY + 16f, textPaint)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7f
            textPaint.color = colorGreyText
            canvas.drawText("Instant QR settlement", rX + 8f, currentY + 28f, textPaint)
        }

        currentY += payBoxH + 16f

        // Bottom Signature
        val sigX = pageWidth - margin - 150f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7.5f
        textPaint.color = colorCharcoal
        canvas.drawText("For ${invoice.vendorProfile.companyName}", sigX, currentY, textPaint)
        currentY += 18f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        textPaint.color = colorPrimary
        canvas.drawText(invoice.authSign, sigX, currentY, textPaint)
        currentY += 9f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        canvas.drawText("Authorized Signatory Controller", sigX, currentY, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.textSize = 7f
        textPaint.color = colorGreyText
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("- Computer Generated Legal Tax Invoice • Bold High-Contrast Template -", pageWidth / 2f, pageHeight - 16f, textPaint)
    }
}
