package com.example.utils

import java.text.DecimalFormat

object NumberToWords {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
        "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun convertLessThanOneThousand(number: Int): String {
        var current = number
        var soFar = ""

        if (current % 100 < 20) {
            soFar = units[current % 100]
            current /= 100
        } else {
            soFar = units[current % 10]
            current /= 10
            soFar = tens[current % 10] + (if (soFar.isNotEmpty()) " $soFar" else "")
            current /= 10
        }

        if (current == 0) return soFar
        return units[current] + " Hundred" + (if (soFar.isNotEmpty()) " $soFar" else "")
    }

    fun convertRupees(amount: Double): String {
        if (amount == 0.0) return "Zero Rupees Only"

        val df = DecimalFormat("0.00")
        val formatted = df.format(amount)
        val parts = formatted.split(".")

        var rupees = parts[0].toLongOrNull() ?: 0L
        val paise = parts.getOrNull(1)?.toIntOrNull() ?: 0

        if (rupees == 0L && paise == 0) return "Zero Rupees Only"

        var result = ""

        if (rupees > 0) {
            val crore = (rupees / 10000000).toInt()
            rupees %= 10000000

            val lakh = (rupees / 100000).toInt()
            rupees %= 100000

            val thousand = (rupees / 1000).toInt()
            rupees %= 1000

            val hundred = rupees.toInt()

            if (crore > 0) {
                result += convertLessThanOneThousand(crore) + " Crore "
            }
            if (lakh > 0) {
                result += convertLessThanOneThousand(lakh) + " Lakh "
            }
            if (thousand > 0) {
                result += convertLessThanOneThousand(thousand) + " Thousand "
            }
            if (hundred > 0) {
                result += convertLessThanOneThousand(hundred) + " "
            }

            result = result.trim() + if (rupees == 1L) " Rupee" else " Rupees"
        }

        if (paise > 0) {
            val paiseStr = convertLessThanOneThousand(paise)
            if (result.isNotEmpty()) {
                result += " and $paiseStr Paise"
            } else {
                result = "$paiseStr Paise"
            }
        }

        return "$result Only".trim()
    }
}
