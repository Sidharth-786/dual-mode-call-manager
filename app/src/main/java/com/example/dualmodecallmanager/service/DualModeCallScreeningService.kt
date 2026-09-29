package com.example.dualmodecallmanager.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.dualmodecallmanager.data.db.AppDatabase
import com.example.dualmodecallmanager.data.db.CallLogEntity
import com.example.dualmodecallmanager.data.db.ContactEntity
import com.example.dualmodecallmanager.data.model.AppMode
import com.example.dualmodecallmanager.data.model.ContactCategory
import com.example.dualmodecallmanager.data.preferences.PreferencesManager
import com.example.dualmodecallmanager.util.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DualModeCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        runCatching {
            if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
                val response = CallResponse.Builder().build()
                respondToCall(callDetails, response)
                return
            }

            val handle = callDetails.handle
            val rawNumber = handle?.schemeSpecificPart ?: ""
            val preferencesManager = PreferencesManager.getInstance(applicationContext)
            val database = AppDatabase.getInstance(applicationContext)

            val activeMode = preferencesManager.activeMode
            val allowUnknownWork = preferencesManager.allowUnknownWork
            val allowUnknownPersonal = preferencesManager.allowUnknownPersonal

            // Synchronously evaluate screening decision
            var isAllowed = false
            var reason = ""
            var callerName: String? = null

            val contacts: List<ContactEntity> = runBlocking(Dispatchers.IO) {
                runCatching { database.contactDao().getAllContactsList() }.getOrDefault(emptyList())
            }

            val matchedContact = contacts.firstOrNull { contact ->
                PhoneNumberUtils.areNumbersMatching(contact.phoneNumber, rawNumber)
            }

            if (matchedContact != null) {
                callerName = matchedContact.name
                when (activeMode) {
                    AppMode.WORK -> {
                        if (matchedContact.category == ContactCategory.WORK || matchedContact.category == ContactCategory.BOTH) {
                            isAllowed = true
                            reason = "Work contact allowed in Work Mode"
                        } else {
                            isAllowed = false
                            reason = "Personal contact rejected in Work Mode"
                        }
                    }
                    AppMode.PERSONAL -> {
                        if (matchedContact.category == ContactCategory.PERSONAL || matchedContact.category == ContactCategory.BOTH) {
                            isAllowed = true
                            reason = "Personal contact allowed in Personal Mode"
                        } else {
                            isAllowed = false
                            reason = "Work contact rejected in Personal Mode"
                        }
                    }
                }
            } else {
                // Unknown number logic
                when (activeMode) {
                    AppMode.WORK -> {
                        if (allowUnknownWork) {
                            isAllowed = true
                            reason = "Unknown caller allowed by Work policy"
                        } else {
                            isAllowed = false
                            reason = "Unknown caller rejected by Work policy"
                        }
                    }
                    AppMode.PERSONAL -> {
                        if (allowUnknownPersonal) {
                            isAllowed = true
                            reason = "Unknown caller allowed by Personal policy"
                        } else {
                            isAllowed = false
                            reason = "Unknown caller rejected by Personal policy"
                        }
                    }
                }
            }

            // Build system screening response
            val response = if (isAllowed) {
                CallResponse.Builder().build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipNotification(false)
                    .setSkipCallLog(false)
                    .build()
            }

            respondToCall(callDetails, response)

            // Log screened call asynchronously
            val displayPhone = if (rawNumber.isNotEmpty()) rawNumber else "Private Number"
            serviceScope.launch {
                runCatching {
                    database.callLogDao().insertCallLog(
                        CallLogEntity(
                            phoneNumber = displayPhone,
                            callerName = callerName,
                            activeMode = activeMode,
                            isAllowed = isAllowed,
                            reason = reason,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }.onFailure {
            // Fallback safety for unexpected system exceptions
            runCatching {
                val fallbackResponse = CallResponse.Builder().build()
                respondToCall(callDetails, fallbackResponse)
            }
        }
    }
}
