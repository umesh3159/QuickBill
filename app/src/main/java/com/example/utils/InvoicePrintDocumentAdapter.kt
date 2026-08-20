package com.example.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.data.model.InvoiceData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Custom PrintDocumentAdapter implementation that renders and exports
 * invoice data to a well-formatted PDF file using the Android Print Document Adapter API.
 */
class InvoicePrintDocumentAdapter(
    private val context: Context,
    private val invoice: InvoiceData,
    private val existingPdfFile: File? = null
) : PrintDocumentAdapter() {

    private var generatedPdfDocument: PdfDocument? = null
    private val totalPages: Int = 1

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        // Configure Document Metadata
        val documentName = "Invoice_${invoice.invoiceNo.ifBlank { "Doc" }}.pdf"
        val info = PrintDocumentInfo.Builder(documentName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(totalPages)
            .build()

        val layoutChanged = newAttributes != oldAttributes
        callback?.onLayoutFinished(info, layoutChanged)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        if (destination == null) {
            callback?.onWriteFailed("Invalid destination descriptor")
            return
        }

        if (cancellationSignal?.isCanceled == true) {
            callback?.onWriteCancelled()
            return
        }

        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(destination.fileDescriptor)

            // If we have an existing PDF file, stream directly to output; otherwise generate fresh
            val sourceFile = existingPdfFile?.takeIf { it.exists() && it.length() > 0 }
                ?: PdfGenerator.generateInvoicePdf(context, invoice)

            if (sourceFile != null && sourceFile.exists()) {
                inputStream = FileInputStream(sourceFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                        return
                    }
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } else {
                callback?.onWriteFailed("Failed to render invoice document")
            }
        } catch (e: Exception) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
            } else {
                callback?.onWriteFailed(e.localizedMessage ?: "Unknown writing error")
            }
        } finally {
            try {
                inputStream?.close()
            } catch (_: IOException) {}
            try {
                outputStream?.close()
            } catch (_: IOException) {}
        }
    }

    override fun onFinish() {
        super.onFinish()
        generatedPdfDocument?.close()
        generatedPdfDocument = null
    }

    companion object {
        fun printInvoice(context: Context, invoice: InvoiceData, existingPdfFile: File? = null) {
            try {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val jobName = "JMD_DigiSign_Invoice_${invoice.invoiceNo}"
                    val adapter = InvoicePrintDocumentAdapter(context, invoice, existingPdfFile)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("res1", "A4 Resolution", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager.print(jobName, adapter, printAttributes)
                } else {
                    Toast.makeText(context, "Printing service unavailable on this device", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Print error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
