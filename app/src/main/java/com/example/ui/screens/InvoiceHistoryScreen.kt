package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.InvoiceEntity
import com.example.ui.theme.AmberText
import com.example.ui.theme.CharcoalBody
import com.example.ui.theme.GreenText
import com.example.ui.theme.GreyText
import com.example.ui.theme.LightGreenTint
import com.example.ui.theme.LightOrangeTint
import com.example.ui.theme.LogoBlue
import com.example.ui.theme.MutedLine
import com.example.ui.theme.RoyalBlue
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.utils.InvoicePrintDocumentAdapter
import java.io.File

@Composable
fun InvoiceHistoryScreen(
    viewModel: InvoiceViewModel,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val savedInvoices by viewModel.savedInvoices.collectAsStateWithLifecycle()

    var selectedFilterMode by remember { mutableStateOf("ALL") }
    var invoiceToDelete by remember { mutableStateOf<InvoiceEntity?>(null) }

    val filteredInvoices = savedInvoices.filter {
        if (selectedFilterMode == "ALL") true else it.paymentMode == selectedFilterMode
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_invoices_input"),
                    placeholder = { Text("Search by Client Name or Invoice #...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreyText) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
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

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf("ALL", "CASH", "BALANCE", "ONLINE PAY")
                    items(filters) { mode ->
                        val isSelected = selectedFilterMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterMode = mode },
                            label = { Text(if (mode == "ALL") "All Payments" else mode, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LogoBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = CircleShape
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (filteredInvoices.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MutedLine,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = GreyText)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching invoice records found", fontWeight = FontWeight.Bold, color = CharcoalBody)
                        Text("Try adjusting your search or filter options.", fontSize = 12.sp, color = GreyText)
                    }
                }
            } else {
                items(filteredInvoices) { invoice ->
                    InvoiceHistoryCard(
                        invoice = invoice,
                        onClick = {
                            viewModel.loadInvoiceToEdit(invoice)
                            onSelectInvoice(invoice)
                        },
                        onPrint = {
                            val invoiceData = viewModel.getInvoiceDataForEntity(invoice)
                            val file = invoice.pdfPath?.let { File(it) }
                            InvoicePrintDocumentAdapter.printInvoice(context, invoiceData, file)
                        },
                        onShare = {
                            if (invoice.pdfPath != null) {
                                val file = File(invoice.pdfPath)
                                if (file.exists()) {
                                    sharePdf(context, file)
                                } else {
                                    Toast.makeText(context, "PDF file missing. Open invoice to re-generate.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDelete = {
                            invoiceToDelete = invoice
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Delete Confirmation Dialog
    if (invoiceToDelete != null) {
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Delete Invoice Record?") },
            text = { Text("Are you sure you want to delete Invoice #${invoiceToDelete?.invoiceNo} for '${invoiceToDelete?.clientName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInvoice(invoiceToDelete!!.id)
                        invoiceToDelete = null
                        Toast.makeText(context, "Invoice record deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InvoiceHistoryCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val (statusColor, statusBg) = when (invoice.paymentMode) {
        "BALANCE" -> Pair(
            if (isDark) Color(0xFFFBBF24) else AmberText,
            if (isDark) Color(0xFF451A03) else LightOrangeTint
        )
        "ONLINE PAY" -> Pair(
            if (isDark) Color(0xFF60A5FA) else RoyalBlue,
            if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
        )
        else -> Pair(
            if (isDark) Color(0xFF34D399) else GreenText,
            if (isDark) Color(0xFF064E3B) else LightGreenTint
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("history_card_${invoice.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Inv #${invoice.invoiceNo}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(6.dp))
                            .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = invoice.paymentMode,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = "Rs. %,.2f".format(invoice.grandTotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (invoice.clientName.isNotBlank()) invoice.clientName else "Valued Client",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (invoice.invoiceCategory == "GOODS") "Goods" else if (invoice.invoiceCategory == "BOTH") "Both" else "Services",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            if (invoice.upiTransactionId.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "UPI UTR: ${invoice.upiTransactionId}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${invoice.invoiceDate} | Phone: ${if (invoice.clientPhone.isNotBlank()) invoice.clientPhone else "-"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onPrint, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Print, contentDescription = "Print / Export PDF", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    if (invoice.pdfPath != null) {
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
