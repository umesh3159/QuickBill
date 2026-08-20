package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JMD DigiSign", appName)
  }

  @Test
  fun `verify invoice tax calculation and rupees in words`() {
    val items = listOf(
      com.example.data.model.InvoiceLineItem(
        itemName = "Digital Signature Certificate",
        pricePerUnit = 1000.0,
        quantity = 2
      )
    )
    val invoice = com.example.data.model.InvoiceData(
      items = items,
      taxType = "1" // CGST 9% + SGST 9%
    )

    assertEquals(2000.0, invoice.taxableValue, 0.01)
    assertEquals(180.0, invoice.cgstAmt, 0.01)
    assertEquals(180.0, invoice.sgstAmt, 0.01)
    assertEquals(2360.0, invoice.grandTotal, 0.01)

    val words = com.example.utils.NumberToWords.convertRupees(invoice.grandTotal)
    assertEquals("Two Thousand Three Hundred Sixty Rupees Only", words)
  }
}
