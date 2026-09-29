package com.example.dualmodecallmanager.util

object PhoneNumberUtils {

    /**
     * Normalizes a raw phone number string by removing all non-digit characters.
     */
    fun normalizePhoneNumber(rawNumber: String): String {
        return rawNumber.replace(Regex("[^0-9]"), "")
    }

    /**
     * Compares two raw phone numbers to check if they represent the same contact number.
     * Uses suffix matching on normalized digits to handle international prefixes, spaces, and country codes.
     */
    fun areNumbersMatching(number1: String, number2: String): Boolean {
        val norm1 = normalizePhoneNumber(number1)
        val norm2 = normalizePhoneNumber(number2)

        if (norm1.isEmpty() || norm2.isEmpty()) return false
        if (norm1 == norm2) return true

        // Compare last 10 digits (or 7 digits if shorter)
        val minLen = minOf(norm1.length, norm2.length)
        val compareLen = minOf(minLen, 10)

        if (compareLen < 7) {
            return norm1 == norm2
        }

        val suffix1 = norm1.takeLast(compareLen)
        val suffix2 = norm2.takeLast(compareLen)
        return suffix1 == suffix2
    }

    /**
     * Formats a normalized or raw phone number for visual presentation.
     */
    fun formatForDisplay(rawNumber: String): String {
        val digits = normalizePhoneNumber(rawNumber)
        return when {
            digits.length == 10 -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
            digits.length == 11 && digits.startsWith("1") -> "+1 (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7)}"
            else -> rawNumber.ifEmpty { "Unknown" }
        }
    }
}
