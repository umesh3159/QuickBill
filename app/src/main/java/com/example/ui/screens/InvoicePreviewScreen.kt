package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InvoiceData
import com.example.data.model.InvoiceTemplate
import com.example.ui.theme.AmberText
import com.example.ui.theme.CharcoalBody
import com.example.ui.theme.GreenText
import com.example.ui.theme.GreyText
import com.example.ui.theme.LightGolden
import com.example.ui.theme.LightGreenTint
import com.example.ui.theme.LightOrangeTint
import com.example.ui.theme.LogoBlue
import com.example.ui.theme.LogoGreenBorder
import com.example.ui.theme.LogoRedBorder
import com.example.ui.theme.MutedLine
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.utils.InvoicePrintDocumentAdapter
import com.example.utils.NumberToWords
import com.example.utils.PdfGenerator
import com.example.utils.QrCodeGenerator
import com.example.utils.ThermalPrinterHelper
import java.io.File

@Composable
fun InvoicePreviewScreen(
    viewModel: InvoiceViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoiceData by viewModel.invoiceForm.collectAsStateWithLifecycle()
    val generatedPdf by viewModel.generatedPdf.collectAsStateWithLifecycle()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()

    val template = invoiceData.template
    val primaryCol = template.composePrimaryColor
    val secondaryCol = template.composeSecondaryColor
    val bgTintCol = template.composeBgTint
    val borderCol = template.composeBorder

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showThermalDialog by remember { mutableStateOf(false) }
    var thermalWidth by remember { mutableIntStateOf(32) }

    LaunchedEffect(invoiceData) {
        qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
            upiId = invoiceData.vendorProfile.upiHandle,
            name = invoiceData.vendorProfile.beneficiaryName,
            amount = invoiceData.grandTotal,
            note = "Invoice #${invoiceData.invoiceNo}",
            invoiceRef = invoiceData.invoiceNo,
            width = 300,
            height = 300
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // 1. Interactive Template Selector Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = primaryCol,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PDF Layout Template:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = CharcoalBody
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primaryCol.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = template.title,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryCol,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontal Scrollable Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InvoiceTemplate.entries.forEach { tmpl ->
                                val isSel = invoiceData.templateId == tmpl.id
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.updateTemplate(tmpl) }
                                        .border(
                                            width = if (isSel) 2.dp else 1.dp,
                                            color = if (isSel) tmpl.composePrimaryColor else MutedLine,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .testTag("preview_template_chip_${tmpl.id}"),
                                    color = if (isSel) tmpl.composeBgTint else Color(0xFFF8FAFC)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mini color dot
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(tmpl.composePrimaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = tmpl.title,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) tmpl.composePrimaryColor else CharcoalBody
                                            )
                                            Text(
                                                text = tmpl.subtitle,
                                                fontSize = 9.sp,
                                                color = GreyText
                                            )
                                        }
                                        if (isSel) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = tmpl.composePrimaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Printable Invoice Preview Paper Card (Dynamically Rendered Per Template)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        // Top Accent Strip for Modern / Bold Templates
                        if (template == InvoiceTemplate.MODERN_MINIMAL) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(primaryCol)
                            )
                        } else if (template == InvoiceTemplate.BOLD_COMPACT) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primaryCol)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = invoiceData.vendorProfile.companyName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Prop: ${invoiceData.vendorProfile.proprietor} | Rajura - 442905",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = secondaryCol
                                    ) {
                                        Text(
                                            text = "TAX INVOICE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(18.dp)) {

                            // Header Section (if not already drawn in Bold Compact)
                            if (template != InvoiceTemplate.BOLD_COMPACT) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(46.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = primaryCol
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = "JMD",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = invoiceData.vendorProfile.companyName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryCol
                                            )
                                            Text(
                                                text = "Prop: ${invoiceData.vendorProfile.proprietor}",
                                                fontSize = 10.5.sp,
                                                color = CharcoalBody
                                            )
                                            Text(
                                                text = "GST Tax Invoice • ${if (invoiceData.invoiceCategory == "GOODS") "Goods Supply" else if (invoiceData.invoiceCategory == "BOTH") "Goods & Services" else "Services Supply"}",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryCol
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "TAX INVOICE",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryCol
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Inv No: #${invoiceData.invoiceNo}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryCol
                                        )
                                        Text(
                                            text = "Date: ${if (invoiceData.invoiceDate.isBlank()) "24/07/2026" else invoiceData.invoiceDate}",
                                            fontSize = 10.sp,
                                            color = CharcoalBody
                                        )
                                        val isOnline = invoiceData.paymentMode in listOf("ONLINE PAY", "UPI")
                                        Text(
                                            text = if (invoiceData.paymentMode == "BALANCE") "DUE / BALANCE" else if (isOnline) "PAID (UPI)" else "PAID (CASH)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (invoiceData.paymentMode == "BALANCE") AmberText else GreenText
                                        )
                                    }
                                }
                            }

                            // UPI UTR Badge if online pay
                            val isOnlinePay = invoiceData.paymentMode in listOf("ONLINE PAY", "UPI")
                            if (isOnlinePay && invoiceData.upiTransactionId.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LightGreenTint,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LogoGreenBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = GreenText, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PAID VIA UPI | UTR Ref: ${invoiceData.upiTransactionId}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenText
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = borderCol, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Vendor & Client Address Boxes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Vendor Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(bgTintCol, RoundedCornerShape(8.dp))
                                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(text = "SELLER / SUPPLIER", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                        Text(text = invoiceData.vendorProfile.companyName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBody)
                                        Text(text = "Rajura, Dist Chandrapur - 442905", fontSize = 8.5.sp, color = GreyText)
                                        Text(text = "Ph: ${invoiceData.vendorProfile.phone}", fontSize = 8.5.sp, color = GreyText)
                                        Text(text = "UPI: ${invoiceData.vendorProfile.upiHandle}", fontSize = 8.5.sp, color = GreyText)
                                    }
                                }

                                // Client Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(bgTintCol, RoundedCornerShape(8.dp))
                                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(text = "BILL TO (CLIENT)", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                        Text(text = if (invoiceData.custName.isNotBlank()) invoiceData.custName else "Valued Client", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBody)
                                        Text(text = "Phone: ${if (invoiceData.custPhone.isNotBlank()) invoiceData.custPhone else "-"}", fontSize = 8.5.sp, color = GreyText)
                                        Text(text = "GSTIN: ${if (invoiceData.custGstin.isNotBlank()) invoiceData.custGstin else "Unregistered"}", fontSize = 8.5.sp, color = GreyText)
                                        if (invoiceData.custAddress.isNotBlank()) {
                                            Text(text = invoiceData.custAddress, fontSize = 8.5.sp, color = GreyText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Line Items Table Header
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = primaryCol,
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "SNo.", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp))
                                    Text(text = "Item & HSN/SAC", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(text = "Qty", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                                    Text(text = "Amount (₹)", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Line Items Rows
                            invoiceData.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 0) Color.White else bgTintCol)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "%02d".format(idx + 1), fontSize = 9.sp, color = CharcoalBody, modifier = Modifier.width(26.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (item.itemName.isNotBlank()) item.itemName else "Services Rendered",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CharcoalBody
                                        )
                                        val hsnCode = if (item.hsnSacCode.isNotBlank()) item.hsnSacCode else if (item.itemType == "GOODS") "8471" else "998313"
                                        Text(
                                            text = "Code: $hsnCode (${if (item.itemType == "GOODS") "Goods" else "Service"})",
                                            fontSize = 8.sp,
                                            color = GreyText
                                        )
                                    }
                                    Text(text = "${item.quantity} ${item.unit}", fontSize = 9.sp, color = CharcoalBody, modifier = Modifier.width(36.dp))
                                    Text(text = "₹%,.2f".format(item.subtotal), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBody)
                                }
                                Divider(color = borderCol, thickness = 0.5.dp)
                            }

                            // Tax & Totals Summary Rows
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgTintCol)
                                    .padding(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Gross Taxable Value", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                    Text(text = "₹%,.2f".format(invoiceData.taxableValue), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                }

                                when (invoiceData.taxType) {
                                    "1" -> {
                                        val cgstRate = invoiceData.cgstRatePercent
                                        val sgstRate = invoiceData.sgstRatePercent
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Central GST (CGST) @ ${if (cgstRate % 1.0 == 0.0) cgstRate.toInt() else cgstRate}%", fontSize = 9.sp, color = CharcoalBody)
                                            Text(text = "₹%,.2f".format(invoiceData.cgstAmt), fontSize = 9.sp, color = CharcoalBody)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "State GST (SGST) @ ${if (sgstRate % 1.0 == 0.0) sgstRate.toInt() else sgstRate}%", fontSize = 9.sp, color = CharcoalBody)
                                            Text(text = "₹%,.2f".format(invoiceData.sgstAmt), fontSize = 9.sp, color = CharcoalBody)
                                        }
                                    }
                                    "2" -> {
                                        val igstRate = invoiceData.igstRatePercent
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Integrated GST (IGST) @ ${if (igstRate % 1.0 == 0.0) igstRate.toInt() else igstRate}%", fontSize = 9.sp, color = CharcoalBody)
                                            Text(text = "₹%,.2f".format(invoiceData.igstAmt), fontSize = 9.sp, color = CharcoalBody)
                                        }
                                    }
                                    "4" -> {
                                        val pct = (invoiceData.customTaxRate * 100).toInt()
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Custom Tax @ $pct%", fontSize = 9.sp, color = CharcoalBody)
                                            Text(text = "₹%,.2f".format(invoiceData.customTaxAmt), fontSize = 9.sp, color = CharcoalBody)
                                        }
                                    }
                                    else -> {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "Tax (Exempted 0%)", fontSize = 9.sp, color = CharcoalBody)
                                            Text(text = "₹0.00", fontSize = 9.sp, color = CharcoalBody)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = primaryCol, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "GRAND TOTAL (Taxable + GST)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                    Text(text = "Rs. %,.2f".format(invoiceData.grandTotal), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Amount in Words
                            Text(
                                text = "Amount in Words: ${NumberToWords.convertRupees(invoiceData.grandTotal)}",
                                fontSize = 9.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = LightGolden
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Payment Settlement & Live UPI Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isOnlinePay) {
                                    // Online UPI Paid Confirmed Box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(LightGreenTint, RoundedCornerShape(8.dp))
                                            .border(1.dp, LogoGreenBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(text = "PAID VIA ONLINE UPI", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GreenText)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "UTR: ${if (invoiceData.upiTransactionId.isNotBlank()) invoiceData.upiTransactionId else "Online Settlement"}\n" +
                                                        "Payee: ${invoiceData.vendorProfile.beneficiaryName}\n" +
                                                        "UPI ID: ${invoiceData.vendorProfile.upiHandle}\n" +
                                                        "Bank: ${invoiceData.vendorProfile.bankName}",
                                                fontSize = 8.sp,
                                                color = CharcoalBody,
                                                lineHeight = 10.5.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(LightGreenTint, RoundedCornerShape(8.dp))
                                            .border(1.dp, LogoGreenBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = GreenText, modifier = Modifier.size(24.dp))
                                            Text(text = "VERIFIED PAYMENT", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GreenText)
                                            Text(text = "PAID VIA UPI", fontSize = 7.5.sp, color = GreenText)
                                        }
                                    }
                                } else {
                                    // Bank Details & Scan to pay Box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(LightOrangeTint, RoundedCornerShape(8.dp))
                                            .border(1.dp, LogoRedBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(text = "SETTLEMENT DETAILS", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GreyText)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Beneficiary: ${invoiceData.vendorProfile.beneficiaryName}\n" +
                                                        "Bank: ${invoiceData.vendorProfile.bankName}\n" +
                                                        "A/C: ${invoiceData.vendorProfile.accountNo}\n" +
                                                        "IFSC: ${invoiceData.vendorProfile.ifscCode}\n" +
                                                        "UPI: ${invoiceData.vendorProfile.upiHandle}",
                                                fontSize = 8.sp,
                                                color = CharcoalBody,
                                                lineHeight = 10.5.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightGreenTint)
                                            .border(1.dp, LogoGreenBorder, RoundedCornerShape(8.dp))
                                            .clickable { showQrDialog = true }
                                            .padding(8.dp)
                                            .testTag("interactive_upi_qr_box")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.QrCode,
                                                        contentDescription = null,
                                                        tint = GreenText,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "SCAN & PAY",
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GreenText
                                                    )
                                                }
                                                Text(
                                                    text = "Tap to enlarge QR",
                                                    fontSize = 7.5.sp,
                                                    color = GreyText
                                                )
                                            }
                                            if (qrBitmap != null) {
                                                Image(
                                                    bitmap = qrBitmap!!.asImageBitmap(),
                                                    contentDescription = "UPI Payment QR Code",
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .testTag("upi_qr_code_image")
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Authorized Signatory
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(text = "Statutory Declarations:", fontSize = 7.5.sp, color = GreyText)
                                    Text(text = "1. Computer generated tax invoice.", fontSize = 7.sp, color = GreyText)
                                    Text(text = "2. GST ITC eligible as per rules.", fontSize = 7.sp, color = GreyText)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "For ${invoiceData.vendorProfile.companyName}", fontSize = 8.sp, color = CharcoalBody)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = invoiceData.authSign, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primaryCol)
                                    Text(text = "Authorized Signatory Controller", fontSize = 7.sp, color = GreyText)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 24.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Export & Print Manager", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = CharcoalBody)
                    Text(
                        text = "Template: ${template.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryCol
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // System Print Button
                    Button(
                        onClick = {
                            printPdfFile(context, invoiceData)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("sys_print_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCol)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Print PDF", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Share Button
                    Button(
                        onClick = {
                            viewModel.generateAndSaveInvoicePdf(context) { file ->
                                if (file != null) sharePdfFile(context, file)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("share_pdf_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryCol)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Share PDF", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Thermal Receipt Button
                    Button(
                        onClick = { showThermalDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("thermal_print_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Thermal POS", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Generate & Save PDF Button
                Button(
                    onClick = {
                        viewModel.generateAndSaveInvoicePdf(context) { file ->
                            if (file != null) {
                                Toast.makeText(context, "Invoice Saved Successfully! (${file.name})", Toast.LENGTH_LONG).show()
                                onDone()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_save_pdf_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryCol),
                    enabled = !isGeneratingPdf
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Generating PDF...")
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Finalize Invoice", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }
    }

    // Dynamic UPI QR Payment Dialog
    if (showQrDialog) {
        val upiUri = QrCodeGenerator.buildUpiUriString(
            upiId = invoiceData.vendorProfile.upiHandle,
            name = invoiceData.vendorProfile.beneficiaryName,
            amount = invoiceData.grandTotal,
            note = "Invoice #${invoiceData.invoiceNo}",
            invoiceRef = invoiceData.invoiceNo
        )

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = GreenText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dynamic UPI QR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CharcoalBody
                            )
                            Text(
                                text = "Instant UPI payment QR code",
                                fontSize = 11.sp,
                                color = GreyText
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Amount Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LightGreenTint,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LogoGreenBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "INVOICE TOTAL AMOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreyText,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "₹%,.2f".format(invoiceData.grandTotal),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big High-Res ZXing QR Code
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LogoGreenBorder),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Dynamic UPI QR Code",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp)
                                    .testTag("dialog_upi_qr_image")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Merchant & UPI ID Details
                    Text(
                        text = invoiceData.vendorProfile.beneficiaryName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CharcoalBody
                    )
                    Text(
                        text = "UPI ID: ${invoiceData.vendorProfile.upiHandle}",
                        fontSize = 12.sp,
                        color = LogoBlue,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Scan with Google Pay, PhonePe, Paytm, BHIM, or any UPI app to pay exact bill amount directly.",
                        fontSize = 10.5.sp,
                        color = GreyText,
                        lineHeight = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                            context.startActivity(Intent.createChooser(intent, "Pay via UPI App"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open UPI App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("UPI ID", invoiceData.vendorProfile.upiHandle)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "UPI ID copied: ${invoiceData.vendorProfile.upiHandle}", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy UPI ID", fontSize = 12.sp)
                }
            }
        )
    }

    // Thermal Printer Dialog
    if (showThermalDialog) {
        AlertDialog(
            onDismissRequest = { showThermalDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = LogoBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thermal POS Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose thermal paper roll width:",
                        fontSize = 13.sp,
                        color = CharcoalBody
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = thermalWidth == 32,
                            onClick = { thermalWidth = 32 },
                            label = { Text("58mm (2-Inch / 32-col)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = thermalWidth == 48,
                            onClick = { thermalWidth = 48 },
                            label = { Text("80mm (3-Inch / 48-col)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val receiptText = remember(thermalWidth, invoiceData) {
                        ThermalPrinterHelper.generateFormattedReceiptText(invoiceData, thermalWidth)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MutedLine)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = receiptText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp,
                                    color = CharcoalBody
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = ThermalPrinterHelper.generateFormattedReceiptText(invoiceData, thermalWidth)
                        ThermalPrinterHelper.shareReceipt(context, text, "JMD Invoice #${invoiceData.invoiceNo}")
                        showThermalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LogoBlue)
                ) {
                    Text("Print / Share Text")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val text = ThermalPrinterHelper.generateFormattedReceiptText(invoiceData, thermalWidth)
                        ThermalPrinterHelper.copyToClipboard(context, text)
                        showThermalDialog = false
                    }
                ) {
                    Text("Copy Receipt")
                }
            }
        )
    }
}

private fun printPdfFile(context: Context, invoiceData: InvoiceData) {
    val fileToPrint = PdfGenerator.generateInvoicePdf(context, invoiceData)
    InvoicePrintDocumentAdapter.printInvoice(context, invoiceData, fileToPrint)
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice - ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Invoice PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
