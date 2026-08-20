package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CustomerEntity
import com.example.data.db.LedgerTransactionEntity
import com.example.ui.theme.AmberText
import com.example.ui.theme.CharcoalBody
import com.example.ui.theme.GreenText
import com.example.ui.theme.GreyText
import com.example.ui.theme.LightGreenTint
import com.example.ui.theme.LightOrangeTint
import com.example.ui.theme.LogoBlue
import com.example.ui.theme.MutedLine
import com.example.ui.theme.SurfaceAccentBg
import com.example.ui.theme.SurfaceVariantBg
import com.example.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerForCredit by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedCustomerForStatement by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    // Calculate total summary
    val totalCreditAll = allTransactions.filter { it.type == "CREDIT" }.sumOf { it.amount }
    val totalDebitAll = allTransactions.filter { it.type == "DEBIT" }.sumOf { it.amount }
    val netOutstandingAll = (totalCreditAll - totalDebitAll).coerceAtLeast(0.0)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Card Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Ledger",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Customer Ledger & Khata",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Manage customer credit, debit & payments",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Udhar / Outstanding Balance Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Udhar (Due)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹%,.2f".format(netOutstandingAll),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Total Jama / Received Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Jama (Received)", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹%,.2f".format(totalDebitAll),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search and Add Customer Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateCustomerSearchQuery(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_customer_input"),
                        placeholder = { Text("Search customer by name/phone...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreyText) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateCustomerSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = GreyText)
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LogoBlue,
                            unfocusedBorderColor = MutedLine,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddCustomerDialog = true },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = LogoBlue),
                        modifier = Modifier.testTag("add_customer_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Customer List Items
            if (customers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MutedLine)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GreyText,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Customers Found", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = CharcoalBody)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Add a new customer or generate an invoice to start tracking credit and debit accounts.",
                                fontSize = 12.sp,
                                color = GreyText
                            )
                        }
                    }
                }
            } else {
                items(customers, key = { it.id }) { customer ->
                    val customerTxs = allTransactions.filter { it.customerId == customer.id }
                    val totalCredit = customerTxs.filter { it.type == "CREDIT" }.sumOf { it.amount }
                    val totalDebit = customerTxs.filter { it.type == "DEBIT" }.sumOf { it.amount }
                    val dueBalance = totalCredit - totalDebit

                    CustomerLedgerCard(
                        customer = customer,
                        totalCredit = totalCredit,
                        totalDebit = totalDebit,
                        dueBalance = dueBalance,
                        onPaymentClick = { customerForPayment = customer },
                        onCreditClick = { customerForCredit = customer },
                        onStatementClick = { selectedCustomerForStatement = customer },
                        onDeleteClick = { customerToDelete = customer }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // --- DIALOGS ---

    // 1. Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address, gstin ->
                viewModel.addCustomer(name, phone, address, gstin)
                showAddCustomerDialog = false
            }
        )
    }

    // 2. Record Payment (Jama) Dialog
    if (customerForPayment != null) {
        val cust = customerForPayment!!
        RecordPaymentDialog(
            customerName = cust.name,
            onDismiss = { customerForPayment = null },
            onRecord = { amount, mode, note, date ->
                viewModel.recordPayment(cust.id, cust.name, amount, mode, note, date)
                customerForPayment = null
            }
        )
    }

    // 3. Record Udhar (Credit) Dialog
    if (customerForCredit != null) {
        val cust = customerForCredit!!
        RecordCreditDialog(
            customerName = cust.name,
            onDismiss = { customerForCredit = null },
            onRecord = { amount, note, date ->
                viewModel.recordCreditEntry(cust.id, cust.name, amount, note, date)
                customerForCredit = null
            }
        )
    }

    // 4. Delete Customer Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Account?") },
            text = { Text("Are you sure you want to delete '${customerToDelete?.name}' and all associated ledger records?") },
            confirmButton = {
                TextButton(onClick = {
                    customerToDelete?.let { viewModel.deleteCustomer(it) }
                    customerToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Full Statement Bottom Sheet
    if (selectedCustomerForStatement != null) {
        val cust = selectedCustomerForStatement!!
        val customerTxs = allTransactions.filter { it.customerId == cust.id }

        CustomerStatementBottomSheet(
            customer = cust,
            transactions = customerTxs,
            onDismiss = { selectedCustomerForStatement = null }
        )
    }
}

@Composable
fun CustomerLedgerCard(
    customer: CustomerEntity,
    totalCredit: Double,
    totalDebit: Double,
    dueBalance: Double,
    onPaymentClick: () -> Unit,
    onCreditClick: () -> Unit,
    onStatementClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val hasBalance = dueBalance > 0.01

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onStatementClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = "Phone: ${customer.phone}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isDark = MaterialTheme.colorScheme.background.red < 0.2f
                val badgeBg = if (hasBalance) {
                    if (isDark) Color(0xFF451A03) else LightOrangeTint
                } else {
                    if (isDark) Color(0xFF064E3B) else LightGreenTint
                }
                val badgeText = if (hasBalance) {
                    if (isDark) Color(0xFFFBBF24) else AmberText
                } else {
                    if (isDark) Color(0xFF34D399) else GreenText
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(badgeBg, CircleShape)
                        .border(
                            0.5.dp,
                            badgeText.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasBalance) "Due ₹%,.0f".format(dueBalance) else "Settled",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Totals breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Udhar (Bill)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.2f".format(totalCredit), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Total Jama (Paid)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.2f".format(totalDebit), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Balance Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹%,.2f".format(dueBalance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasBalance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPaymentClick,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jama (Receive)", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onCreditClick,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, LogoBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = LogoBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Udhar (Credit)", fontSize = 12.sp, color = LogoBlue)
                }

                if (customer.phone.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .background(SurfaceVariantBg, CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = LogoBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, gstin: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Customer", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_customer_phone_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Customer Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it },
                    label = { Text("GSTIN (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, address, gstin) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LogoBlue)
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    customerName: String,
    onDismiss: () -> Unit,
    onRecord: (amount: Double, mode: String, note: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Payment (Jama) - $customerName", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Received (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GreyText)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH", "ONLINE PAY", "UPI", "CHEQUE").forEach { mode ->
                        val isSel = paymentMode == mode
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) LogoBlue else SurfaceVariantBg)
                                .clickable { paymentMode = mode }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                mode,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else CharcoalBody,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Remark (e.g. Cash received)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onRecord(amt, paymentMode, note, "")
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GreenText)
            ) {
                Text("Record Jama")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordCreditDialog(
    customerName: String,
    onDismiss: () -> Unit,
    onRecord: (amount: Double, note: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Udhar (Credit) - $customerName", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Udhar Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Items Given (e.g. Goods Udhar)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onRecord(amt, note, "")
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = LogoBlue)
            ) {
                Text("Add Udhar Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatementBottomSheet(
    customer: CustomerEntity,
    transactions: List<LedgerTransactionEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val totalCredit = transactions.filter { it.type == "CREDIT" }.sumOf { it.amount }
    val totalDebit = transactions.filter { it.type == "DEBIT" }.sumOf { it.amount }
    val dueBalance = totalCredit - totalDebit

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Khata Statement: ${customer.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBody
                    )
                    if (customer.phone.isNotBlank()) {
                        Text("Phone: ${customer.phone}", fontSize = 12.sp, color = GreyText)
                    }
                }

                if (customer.phone.isNotBlank() && dueBalance > 0) {
                    Button(
                        onClick = {
                            val msg = "Namaste ${customer.name} ji, aapke khate ka total balance ₹%.2f baki hai. Kripya bhugtan karein. Dhanyawad!".format(dueBalance)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=${customer.phone}&text=${Uri.encode(msg)}")
                            }
                            context.startActivity(intent)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Reminder", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantBg),
                border = BorderStroke(1.dp, MutedLine)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Bill (Credit)", fontSize = 11.sp, color = GreyText)
                        Text("₹%,.2f".format(totalCredit), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBody)
                    }
                    Column {
                        Text("Total Paid (Jama)", fontSize = 11.sp, color = GreyText)
                        Text("₹%,.2f".format(totalDebit), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenText)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Net Due Balance", fontSize = 11.sp, color = GreyText)
                        Text(
                            "₹%,.2f".format(dueBalance),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dueBalance > 0) AmberText else GreenText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Transaction Statement History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalBody)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged yet.", fontSize = 13.sp, color = GreyText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(transactions) { tx ->
                        val isDebit = tx.type == "DEBIT" // Jama
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MutedLine)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDebit) LightGreenTint else LightOrangeTint,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isDebit) Icons.Default.Payments else Icons.AutoMirrored.Filled.ReceiptLong,
                                            contentDescription = null,
                                            tint = if (isDebit) GreenText else AmberText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (tx.note.isNotBlank()) tx.note else (if (isDebit) "Jama Payment" else "Bill Udhar"),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = CharcoalBody
                                    )
                                    Text(
                                        text = "${tx.date} • ${tx.paymentMode}",
                                        fontSize = 11.sp,
                                        color = GreyText
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isDebit) "- " else "+ ") + "₹%,.2f".format(tx.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDebit) GreenText else AmberText
                                    )
                                    Text(
                                        text = if (isDebit) "Jama" else "Udhar",
                                        fontSize = 10.sp,
                                        color = if (isDebit) GreenText else AmberText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
