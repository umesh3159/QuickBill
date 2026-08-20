package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InvoiceLineItem
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
import com.example.utils.NumberToWords
import com.example.utils.QrCodeGenerator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: InvoiceViewModel,
    onPreviewPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoiceData by viewModel.invoiceForm.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showInstantQrDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "1. Doc & Pay (UPI)",
        "2. Client Info",
        "3. Goods / Services & GST",
        "4. Signature & Bank"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step Indicator Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LogoBlue,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("step_tab_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selectedTab == index) LogoBlue else MutedLine,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    color = if (selectedTab == index) Color.White else CharcoalBody,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) LogoBlue else GreyText
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            when (selectedTab) {
                0 -> item {
                    // STEP 1: Document Metadata, Invoice Category & Online UPI Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "[1/4] DOCUMENT & PAYMENT SETTLEMENT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogoBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Invoice Category Chips (Services / Goods / Both)
                            Text(
                                text = "Supply Type / Category:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalBody
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("SERVICES", "Services", Icons.Default.Business),
                                    Triple("GOODS", "Goods", Icons.Default.ShoppingBag),
                                    Triple("BOTH", "Both (Goods & Services)", Icons.Default.Receipt)
                                ).forEach { (catKey, catLabel, icon) ->
                                    val isSelected = invoiceData.invoiceCategory == catKey
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.updateInvoiceCategory(catKey) }
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) LogoBlue else MutedLine,
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        color = if (isSelected) LogoBlue.copy(alpha = 0.1f) else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) LogoBlue else GreyText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = catLabel,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) LogoBlue else CharcoalBody
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = invoiceData.invoiceNo,
                                    onValueChange = {
                                        viewModel.updateDocumentMeta(
                                            logoName = invoiceData.logoName,
                                            invoiceNo = it,
                                            invoiceDate = invoiceData.invoiceDate,
                                            paymentMode = invoiceData.paymentMode,
                                            upiTransactionId = invoiceData.upiTransactionId,
                                            invoiceCategory = invoiceData.invoiceCategory
                                        )
                                    },
                                    label = { Text("Invoice No") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("invoice_number_input"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = invoiceData.invoiceDate,
                                    onValueChange = {
                                        viewModel.updateDocumentMeta(
                                            logoName = invoiceData.logoName,
                                            invoiceNo = invoiceData.invoiceNo,
                                            invoiceDate = it,
                                            paymentMode = invoiceData.paymentMode,
                                            upiTransactionId = invoiceData.upiTransactionId,
                                            invoiceCategory = invoiceData.invoiceCategory
                                        )
                                    },
                                    label = { Text("Date (DD/MM/YYYY)") },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("invoice_date_input"),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Payment Mode:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBody
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val paymentModes = listOf(
                                Triple("CASH", "CASH / Cash Payment", Icons.Default.Payments),
                                Triple("ONLINE PAY", "ONLINE PAY / UPI Instant", Icons.Default.QrCode),
                                Triple("BALANCE", "BALANCE / Credit Khata", Icons.Default.CreditCard)
                            )

                            paymentModes.forEach { (mode, label, icon) ->
                                val isSelected = invoiceData.paymentMode == mode
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.updateDocumentMeta(
                                                logoName = invoiceData.logoName,
                                                invoiceNo = invoiceData.invoiceNo,
                                                invoiceDate = invoiceData.invoiceDate,
                                                paymentMode = mode,
                                                upiTransactionId = invoiceData.upiTransactionId,
                                                invoiceCategory = invoiceData.invoiceCategory
                                            )
                                        }
                                        .testTag("payment_mode_$mode"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) {
                                        if (mode == "ONLINE PAY") LightGreenTint else LogoBlue.copy(alpha = 0.08f)
                                    } else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) {
                                            if (mode == "ONLINE PAY") LogoGreenBorder else LogoBlue
                                        } else MutedLine
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.updateDocumentMeta(
                                                    logoName = invoiceData.logoName,
                                                    invoiceNo = invoiceData.invoiceNo,
                                                    invoiceDate = invoiceData.invoiceDate,
                                                    paymentMode = mode,
                                                    upiTransactionId = invoiceData.upiTransactionId,
                                                    invoiceCategory = invoiceData.invoiceCategory
                                                )
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = if (mode == "ONLINE PAY") GreenText else LogoBlue
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                if (mode == "ONLINE PAY") GreenText else LogoBlue
                                            } else GreyText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                if (mode == "ONLINE PAY") GreenText else LogoBlue
                                            } else CharcoalBody,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }

                            // If Online Pay / UPI is selected: show rich UPI Paid Details input card
                            AnimatedVisibility(visible = invoiceData.paymentMode in listOf("ONLINE PAY", "UPI")) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .background(LightGreenTint, RoundedCornerShape(12.dp))
                                        .border(1.dp, LogoGreenBorder, RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = GreenText, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PAID UPI DETAILS",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenText
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Payment received on UPI Handle: ${invoiceData.vendorProfile.upiHandle} (${invoiceData.vendorProfile.beneficiaryName})",
                                        fontSize = 11.5.sp,
                                        color = CharcoalBody
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showInstantQrDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenText),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("show_dynamic_upi_qr_step1_btn")
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Show Dynamic UPI QR (₹%,.2f)".format(invoiceData.grandTotal), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = invoiceData.upiTransactionId,
                                        onValueChange = { viewModel.updateUpiTransactionId(it) },
                                        label = { Text("UPI Reference / UTR Number (Optional)") },
                                        placeholder = { Text("e.g. 421589104821") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("upi_utr_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenText,
                                            unfocusedBorderColor = LogoGreenBorder
                                        ),
                                        supportingText = {
                                            Text(
                                                "This UTR No. will print as 'PAID VIA UPI' on the invoice & PDF.",
                                                fontSize = 10.5.sp,
                                                color = GreyText
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Divider(color = MutedLine, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            // INVOICE PDF TEMPLATE SELECTOR
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = LogoBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Invoice Layout & PDF Template",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LogoBlue
                                    )
                                    Text(
                                        text = "Select your preferred professional PDF design template:",
                                        fontSize = 11.sp,
                                        color = GreyText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            InvoiceTemplate.entries.forEach { template ->
                                val isSelected = invoiceData.templateId == template.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.updateTemplate(template) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) template.composePrimaryColor else MutedLine,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .testTag("template_option_${template.id}"),
                                    color = if (isSelected) template.composeBgTint else MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateTemplate(template) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = template.composePrimaryColor
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Color Swatch Bar
                                        Row(
                                            modifier = Modifier
                                                .size(width = 24.dp, height = 24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .background(template.composePrimaryColor)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .background(template.composeSecondaryColor)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = template.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) template.composePrimaryColor else CharcoalBody
                                            )
                                            Text(
                                                text = template.description,
                                                fontSize = 10.5.sp,
                                                color = CharcoalBody.copy(alpha = 0.8f),
                                                lineHeight = 13.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = template.composePrimaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> item {
                    // STEP 2: Client / Trustee Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "[2/4] CLIENT / BUYER DETAILS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogoBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = invoiceData.custName,
                                onValueChange = {
                                    viewModel.updateClientDetails(
                                        custName = it,
                                        custPhone = invoiceData.custPhone,
                                        custGstin = invoiceData.custGstin,
                                        custAddress = invoiceData.custAddress
                                    )
                                },
                                label = { Text("Client / Company Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_name_input"),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = invoiceData.custPhone,
                                onValueChange = {
                                    viewModel.updateClientDetails(
                                        custName = invoiceData.custName,
                                        custPhone = it,
                                        custGstin = invoiceData.custGstin,
                                        custAddress = invoiceData.custAddress
                                    )
                                },
                                label = { Text("Mobile / Contact Number *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_phone_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = invoiceData.custGstin,
                                onValueChange = {
                                    viewModel.updateClientDetails(
                                        custName = invoiceData.custName,
                                        custPhone = invoiceData.custPhone,
                                        custGstin = it,
                                        custAddress = invoiceData.custAddress
                                    )
                                },
                                label = { Text("Client GSTIN (Optional e.g. 27AAAAA0000A1Z5)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_gstin_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = invoiceData.custAddress,
                                onValueChange = {
                                    viewModel.updateClientDetails(
                                        custName = invoiceData.custName,
                                        custPhone = invoiceData.custPhone,
                                        custGstin = invoiceData.custGstin,
                                        custAddress = it
                                    )
                                },
                                label = { Text("Place of Supply / Address (Optional)") },
                                placeholder = { Text("e.g. Maharashtra (27) / Rajura") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                2 -> {
                    // STEP 3: Goods & Services Line Items & Comprehensive GST Slabs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[3/4] GOODS / SERVICES & GST TAX",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogoBlue
                            )
                            Button(
                                onClick = { viewModel.addLineItem() },
                                modifier = Modifier.testTag("add_item_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = LogoBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Line Item", fontSize = 12.sp)
                            }
                        }
                    }

                    itemsIndexed(invoiceData.items) { index, item ->
                        LineItemEditorCard(
                            index = index,
                            item = item,
                            canRemove = invoiceData.items.size > 1,
                            onUpdate = { name, price, qty, itemType, hsnSac, unit ->
                                viewModel.updateLineItem(index, name, price, qty, itemType, hsnSac, unit)
                            },
                            onRemove = {
                                viewModel.removeLineItem(index)
                            }
                        )
                    }

                    // GST Tax Architecture Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "GST Statutory Tax System:",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBody
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Tax Mode Radio Options
                                val taxOptions = listOf(
                                    Pair("1", "Intra-State GST (CGST + SGST) [Within State]"),
                                    Pair("2", "Inter-State IGST (Integrated GST) [Out of State]"),
                                    Pair("3", "Exempt / Non-GST Supply (Tax Free)"),
                                    Pair("4", "Custom Flat Tax Rate")
                                )

                                taxOptions.forEach { (typeKey, labelStr) ->
                                    val isSelected = invoiceData.taxType == typeKey
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateTaxConfig(typeKey, invoiceData.gstRatePercent, invoiceData.customTaxRate)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.updateTaxConfig(typeKey, invoiceData.gstRatePercent, invoiceData.customTaxRate)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = LogoBlue)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = labelStr,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) LogoBlue else CharcoalBody
                                        )
                                    }
                                }

                                if (invoiceData.taxType in listOf("1", "2")) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Select GST Tax Rate Slab:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GreyText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // GST Slabs (0%, 5%, 12%, 18%, 28%)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { rate ->
                                            val isSelectedRate = invoiceData.gstRatePercent == rate
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.updateTaxConfig(
                                                            taxType = invoiceData.taxType,
                                                            gstRatePercent = rate,
                                                            customTaxRate = rate / 100.0
                                                        )
                                                    }
                                                    .border(
                                                        width = if (isSelectedRate) 1.5.dp else 1.dp,
                                                        color = if (isSelectedRate) LogoBlue else MutedLine,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ),
                                                color = if (isSelectedRate) LogoBlue else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (rate == 0.0) "0% (Exempt)" else "${rate.toInt()}% GST",
                                                        color = if (isSelectedRate) Color.White else CharcoalBody,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Live GST Breakdown Badge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            if (invoiceData.taxType == "1") {
                                                val halfRate = invoiceData.gstRatePercent / 2.0
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Central GST (CGST @ ${if (halfRate % 1.0 == 0.0) halfRate.toInt() else halfRate}%):", fontSize = 11.5.sp, color = CharcoalBody)
                                                    Text("₹%,.2f".format(invoiceData.cgstAmt), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = LogoBlue)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("State GST (SGST @ ${if (halfRate % 1.0 == 0.0) halfRate.toInt() else halfRate}%):", fontSize = 11.5.sp, color = CharcoalBody)
                                                    Text("₹%,.2f".format(invoiceData.sgstAmt), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = LogoBlue)
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Integrated GST (IGST @ ${invoiceData.gstRatePercent.toInt()}%):", fontSize = 11.5.sp, color = CharcoalBody)
                                                    Text("₹%,.2f".format(invoiceData.igstAmt), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = LogoBlue)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> item {
                    // STEP 4: Verification, Bank Protocols & Authorized Signature
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "[4/4] AUTHORIZATION & SETTLEMENT PROTOCOLS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogoBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = invoiceData.authSign,
                                onValueChange = { viewModel.updateAuthSign(it) },
                                label = { Text("Authorized Signatory Controller *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_sign_input"),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bank Details Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LightOrangeTint, RoundedCornerShape(12.dp))
                                    .border(1.dp, LogoRedBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "VENDOR BANK & UPI SETTLEMENT DETAILS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreyText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Company: ${invoiceData.vendorProfile.companyName}\n" +
                                                "Beneficiary: ${invoiceData.vendorProfile.beneficiaryName}\n" +
                                                "Bank: ${invoiceData.vendorProfile.bankName} (${invoiceData.vendorProfile.ifscCode})\n" +
                                                "Account No: ${invoiceData.vendorProfile.accountNo}\n" +
                                                "UPI Handle: ${invoiceData.vendorProfile.upiHandle}",
                                        fontSize = 12.sp,
                                        color = CharcoalBody,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = { showInstantQrDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("show_dynamic_upi_qr_step4_btn")
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenText)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Preview Dynamic Payment QR", fontSize = 11.5.sp, color = GreenText, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Bottom Live Summary Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val taxMetrics by viewModel.taxMetrics.collectAsStateWithLifecycle()
                    Column {
                        if (invoiceData.taxType == "1") {
                            Text(
                                text = "CGST: ₹%,.2f | SGST: ₹%,.2f".format(taxMetrics.cgst, taxMetrics.sgst),
                                fontSize = 11.sp,
                                color = LogoBlue,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (invoiceData.taxType == "2") {
                            Text(
                                text = "IGST: ₹%,.2f".format(taxMetrics.igst),
                                fontSize = 11.sp,
                                color = LogoBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Taxable: ₹%,.2f | GST: ₹%,.2f".format(taxMetrics.taxableValue, taxMetrics.totalTax),
                            fontSize = 11.sp,
                            color = GreyText
                        )
                        Text(
                            text = "Rs. %,.2f".format(taxMetrics.grandTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LogoBlue
                        )
                    }

                    Button(
                        onClick = onPreviewPdf,
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("preview_invoice_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Preview & Generate", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = NumberToWords.convertRupees(invoiceData.grandTotal),
                    fontSize = 11.sp,
                    color = LightGolden,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }

    if (showInstantQrDialog) {
        val qrBitmap = remember(invoiceData.grandTotal, invoiceData.vendorProfile.upiHandle, invoiceData.invoiceNo) {
            QrCodeGenerator.generateUpiQrBitmap(
                upiId = invoiceData.vendorProfile.upiHandle,
                name = invoiceData.vendorProfile.beneficiaryName,
                amount = invoiceData.grandTotal,
                note = "Invoice #${invoiceData.invoiceNo}",
                invoiceRef = invoiceData.invoiceNo,
                width = 300,
                height = 300
            )
        }

        val upiUri = remember(invoiceData.grandTotal, invoiceData.vendorProfile.upiHandle, invoiceData.invoiceNo) {
            QrCodeGenerator.buildUpiUriString(
                upiId = invoiceData.vendorProfile.upiHandle,
                name = invoiceData.vendorProfile.beneficiaryName,
                amount = invoiceData.grandTotal,
                note = "Invoice #${invoiceData.invoiceNo}",
                invoiceRef = invoiceData.invoiceNo
            )
        }

        AlertDialog(
            onDismissRequest = { showInstantQrDialog = false },
            title = {
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
                            text = "Dynamic UPI QR Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CharcoalBody
                        )
                        Text(
                            text = "Scan with any UPI app for instant payment",
                            fontSize = 11.sp,
                            color = GreyText
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                text = "EXACT BILL AMOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreyText
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

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LogoGreenBorder),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Instant UPI QR Code",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp)
                                    .testTag("instant_upi_qr_image")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                        text = "Scan with Google Pay, PhonePe, Paytm, BHIM, or any UPI app to transfer exact bill amount directly.",
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
                            context.startActivity(Intent.createChooser(intent, "Open in UPI App"))
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineItemEditorCard(
    index: Int,
    item: InvoiceLineItem,
    canRemove: Boolean,
    onUpdate: (String, Double, Int, String, String, String) -> Unit,
    onRemove: () -> Unit
) {
    var nameText by remember(item.itemName) { mutableStateOf(item.itemName) }
    var priceText by remember(item.pricePerUnit) { mutableStateOf(if (item.pricePerUnit == 0.0) "" else item.pricePerUnit.toString()) }
    var qtyText by remember(item.quantity) { mutableStateOf(item.quantity.toString()) }
    var itemType by remember(item.itemType) { mutableStateOf(item.itemType) }
    var hsnSacText by remember(item.hsnSacCode) { mutableStateOf(item.hsnSacCode) }
    var unitText by remember(item.unit) { mutableStateOf(item.unit) }

    fun triggerUpdate() {
        onUpdate(
            nameText,
            priceText.toDoubleOrNull() ?: 0.0,
            qtyText.toIntOrNull() ?: 1,
            itemType,
            hsnSacText,
            unitText
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Item #${"%02d".format(index + 1)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = LogoBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (itemType == "GOODS") Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (itemType == "GOODS") "Goods (HSN)" else "Service (SAC)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (itemType == "GOODS") AmberText else LogoBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Item", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service vs Goods Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("SERVICES", "Service (SAC)"),
                    Pair("GOODS", "Goods (HSN)")
                ).forEach { (tKey, tLabel) ->
                    val isSel = itemType == tKey
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                itemType = tKey
                                if (tKey == "SERVICES" && hsnSacText in listOf("", "8471", "8523")) {
                                    hsnSacText = "998313"
                                } else if (tKey == "GOODS" && hsnSacText in listOf("", "998313", "998221")) {
                                    hsnSacText = "8471"
                                }
                                triggerUpdate()
                            }
                            .border(
                                width = if (isSel) 1.2.dp else 1.dp,
                                color = if (isSel) LogoBlue else MutedLine,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        color = if (isSel) LogoBlue.copy(alpha = 0.08f) else Color.Transparent
                    ) {
                        Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = tLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) LogoBlue else CharcoalBody
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    triggerUpdate()
                },
                label = { Text("Item / Service Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hsnSacText,
                    onValueChange = {
                        hsnSacText = it
                        triggerUpdate()
                    },
                    label = { Text("HSN / SAC Code") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text(if (itemType == "GOODS") "8471" else "998313") }
                )

                OutlinedTextField(
                    value = unitText,
                    onValueChange = {
                        unitText = it
                        triggerUpdate()
                    },
                    label = { Text("Unit") },
                    placeholder = { Text("Nos/Hrs/Pcs") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Quick HSN / SAC Suggestion chips
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = if (itemType == "GOODS") {
                    listOf(Pair("8471", "USB Dongle/Token"), Pair("8523", "Software Media"))
                } else {
                    listOf(Pair("998313", "DSC / IT Consulting"), Pair("998221", "Tax / Advisory"))
                }
                suggestions.forEach { (code, desc) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                hsnSacText = code
                                triggerUpdate()
                            }
                    ) {
                        Text(
                            text = "$code ($desc)",
                            fontSize = 10.sp,
                            color = LogoBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        triggerUpdate()
                    },
                    label = { Text("Rate / Unit (₹)") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = {
                        qtyText = it
                        triggerUpdate()
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            val subtotal = (priceText.toDoubleOrNull() ?: 0.0) * (qtyText.toIntOrNull() ?: 1)
            Text(
                text = "Item Taxable Subtotal: ₹%,.2f".format(subtotal),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GreenText,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
