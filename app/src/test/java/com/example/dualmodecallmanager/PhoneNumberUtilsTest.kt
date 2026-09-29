package com.example.dualmodecallmanager

import com.example.dualmodecallmanager.util.PhoneNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun testNormalizePhoneNumber() {
        assertEquals("15551234567", PhoneNumberUtils.normalizePhoneNumber("+1 (555) 123-4567"))
        assertEquals("05551234567", PhoneNumberUtils.normalizePhoneNumber("0555-123-4567"))
        assertEquals("9876543210", PhoneNumberUtils.normalizePhoneNumber("98765 43210"))
    }

    @Test
    fun testAreNumbersMatching_exactMatch() {
        assertTrue(PhoneNumberUtils.areNumbersMatching("5551234567", "5551234567"))
    }

    @Test
    fun testAreNumbersMatching_formattedWithCountryCode() {
        assertTrue(PhoneNumberUtils.areNumbersMatching("+1 (555) 123-4567", "5551234567"))
        assertTrue(PhoneNumberUtils.areNumbersMatching("+15551234567", "(555) 123-4567"))
    }

    @Test
    fun testAreNumbersMatching_differentNumbers() {
        assertFalse(PhoneNumberUtils.areNumbersMatching("5551234567", "5559876543"))
    }

    @Test
    fun testAreNumbersMatching_emptyString() {
        assertFalse(PhoneNumberUtils.areNumbersMatching("", "5551234567"))
    }
}
