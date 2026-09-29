package com.example.dualmodecallmanager

import com.example.dualmodecallmanager.data.db.ContactEntity
import com.example.dualmodecallmanager.data.model.AppMode
import com.example.dualmodecallmanager.data.model.ContactCategory
import com.example.dualmodecallmanager.util.PhoneNumberUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallScreeningLogicTest {

    private val workContact = ContactEntity(
        id = 1,
        name = "Boss",
        phoneNumber = "555-0001",
        normalizedNumber = "5550001",
        category = ContactCategory.WORK
    )

    private val personalContact = ContactEntity(
        id = 2,
        name = "Mom",
        phoneNumber = "555-0002",
        normalizedNumber = "5550002",
        category = ContactCategory.PERSONAL
    )

    private val dualContact = ContactEntity(
        id = 3,
        name = "Doctor",
        phoneNumber = "555-0003",
        normalizedNumber = "5550003",
        category = ContactCategory.BOTH
    )

    private val contactsList = listOf(workContact, personalContact, dualContact)

    private fun evaluateCall(
        rawNumber: String,
        activeMode: AppMode,
        allowUnknown: Boolean
    ): Boolean {
        val matched = contactsList.firstOrNull { contact ->
            PhoneNumberUtils.areNumbersMatching(contact.phoneNumber, rawNumber)
        }

        return if (matched != null) {
            when (activeMode) {
                AppMode.WORK -> matched.category == ContactCategory.WORK || matched.category == ContactCategory.BOTH
                AppMode.PERSONAL -> matched.category == ContactCategory.PERSONAL || matched.category == ContactCategory.BOTH
            }
        } else {
            allowUnknown
        }
    }

    @Test
    fun testWorkMode_workContactAllowed() {
        assertTrue(evaluateCall("555-0001", AppMode.WORK, allowUnknown = false))
    }

    @Test
    fun testWorkMode_personalContactRejected() {
        assertFalse(evaluateCall("555-0002", AppMode.WORK, allowUnknown = false))
    }

    @Test
    fun testWorkMode_dualContactAllowed() {
        assertTrue(evaluateCall("555-0003", AppMode.WORK, allowUnknown = false))
    }

    @Test
    fun testWorkMode_unknownCallerRejectedByDefault() {
        assertFalse(evaluateCall("555-9999", AppMode.WORK, allowUnknown = false))
    }

    @Test
    fun testWorkMode_unknownCallerAllowedWhenConfigured() {
        assertTrue(evaluateCall("555-9999", AppMode.WORK, allowUnknown = true))
    }

    @Test
    fun testPersonalMode_personalContactAllowed() {
        assertTrue(evaluateCall("555-0002", AppMode.PERSONAL, allowUnknown = false))
    }

    @Test
    fun testPersonalMode_workContactRejected() {
        assertFalse(evaluateCall("555-0001", AppMode.PERSONAL, allowUnknown = false))
    }

    @Test
    fun testPersonalMode_dualContactAllowed() {
        assertTrue(evaluateCall("555-0003", AppMode.PERSONAL, allowUnknown = false))
    }

    @Test
    fun testPersonalMode_unknownCallerRejectedByDefault() {
        assertFalse(evaluateCall("555-9999", AppMode.PERSONAL, allowUnknown = false))
    }

    @Test
    fun testPersonalMode_unknownCallerAllowedWhenConfigured() {
        assertTrue(evaluateCall("555-9999", AppMode.PERSONAL, allowUnknown = true))
    }
}
