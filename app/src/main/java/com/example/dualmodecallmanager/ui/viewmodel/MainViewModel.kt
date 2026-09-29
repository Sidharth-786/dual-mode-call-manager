package com.example.dualmodecallmanager.ui.viewmodel

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dualmodecallmanager.data.db.AppDatabase
import com.example.dualmodecallmanager.data.db.CallLogEntity
import com.example.dualmodecallmanager.data.db.ContactEntity
import com.example.dualmodecallmanager.data.model.AppMode
import com.example.dualmodecallmanager.data.model.ContactCategory
import com.example.dualmodecallmanager.data.preferences.PreferencesManager
import com.example.dualmodecallmanager.util.PhoneNumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SettingsState(
    val activeMode: AppMode = AppMode.WORK,
    val allowUnknownWork: Boolean = false,
    val allowUnknownPersonal: Boolean = false
)

data class MainUiState(
    val activeMode: AppMode = AppMode.WORK,
    val allowUnknownWork: Boolean = false,
    val allowUnknownPersonal: Boolean = false,
    val contacts: List<ContactEntity> = emptyList(),
    val selectedCategoryFilter: ContactCategory? = null,
    val searchQuery: String = "",
    val callLogs: List<CallLogEntity> = emptyList(),
    val workContactCount: Int = 0,
    val personalContactCount: Int = 0,
    val todayScreenedCount: Int = 0,
    val isCallScreeningRoleGranted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val contactDao = db.contactDao()
    private val callLogDao = db.callLogDao()
    private val prefs = PreferencesManager.getInstance(application)

    private val _selectedCategoryFilter = MutableStateFlow<ContactCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isCallScreeningRoleGranted = MutableStateFlow(false)

    private val settingsFlow = combine(
        prefs.activeModeFlow,
        prefs.allowUnknownWorkFlow,
        prefs.allowUnknownPersonalFlow
    ) { mode, allowWork, allowPersonal ->
        SettingsState(mode, allowWork, allowPersonal)
    }

    private val startOfDay: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val uiState: StateFlow<MainUiState> = combine(
        settingsFlow,
        contactDao.getAllContacts(),
        callLogDao.getAllCallLogs(),
        _selectedCategoryFilter,
        combine(_searchQuery, _isCallScreeningRoleGranted) { q, r -> Pair(q, r) }
    ) { settings, contacts, callLogs, categoryFilter: ContactCategory?, (query, roleGranted) ->

        val filteredContacts = contacts.filter { contact ->
            val matchesCategory = categoryFilter == null || contact.category == categoryFilter || contact.category == ContactCategory.BOTH
            val matchesQuery = query.isBlank() || contact.name.contains(query, ignoreCase = true) || contact.phoneNumber.contains(query)
            matchesCategory && matchesQuery
        }

        val workCount = contacts.count { it.category == ContactCategory.WORK || it.category == ContactCategory.BOTH }
        val personalCount = contacts.count { it.category == ContactCategory.PERSONAL || it.category == ContactCategory.BOTH }
        val todayCount = callLogs.count { it.timestamp >= startOfDay }

        MainUiState(
            activeMode = settings.activeMode,
            allowUnknownWork = settings.allowUnknownWork,
            allowUnknownPersonal = settings.allowUnknownPersonal,
            contacts = filteredContacts,
            selectedCategoryFilter = categoryFilter,
            searchQuery = query,
            callLogs = callLogs,
            workContactCount = workCount,
            personalContactCount = personalCount,
            todayScreenedCount = todayCount,
            isCallScreeningRoleGranted = roleGranted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun toggleMode() {
        runCatching { prefs.toggleMode() }
    }

    fun setActiveMode(mode: AppMode) {
        runCatching { prefs.activeMode = mode }
    }

    fun setAllowUnknownWork(allow: Boolean) {
        runCatching { prefs.allowUnknownWork = allow }
    }

    fun setAllowUnknownPersonal(allow: Boolean) {
        runCatching { prefs.allowUnknownPersonal = allow }
    }

    fun setCategoryFilter(category: ContactCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addContact(name: String, phoneNumber: String, category: ContactCategory) {
        viewModelScope.launch {
            runCatching {
                val normalized = PhoneNumberUtils.normalizePhoneNumber(phoneNumber)
                val entity = ContactEntity(
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    normalizedNumber = normalized,
                    category = category
                )
                contactDao.insertContact(entity)
            }
        }
    }

    fun updateContact(contact: ContactEntity) {
        viewModelScope.launch {
            runCatching { contactDao.updateContact(contact) }
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            runCatching { contactDao.deleteContact(contact) }
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            runCatching { callLogDao.clearAllCallLogs() }
        }
    }

    fun checkCallScreeningRoleStatus(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                val isHeld = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
                _isCallScreeningRoleGranted.value = isHeld
            }.onFailure {
                _isCallScreeningRoleGranted.value = false
            }
        } else {
            _isCallScreeningRoleGranted.value = true
        }
    }

    fun importPhoneContacts(context: Context, category: ContactCategory, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val importedList = mutableListOf<ContactEntity>()
            runCatching {
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, null
                )

                cursor?.use { c ->
                    val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (c.moveToNext()) {
                        val name = if (nameIndex >= 0) c.getString(nameIndex) ?: "Unknown" else "Unknown"
                        val number = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                        if (number.isNotBlank()) {
                            val normalized = PhoneNumberUtils.normalizePhoneNumber(number)
                            importedList.add(
                                ContactEntity(
                                    name = name,
                                    phoneNumber = number,
                                    normalizedNumber = normalized,
                                    category = category
                                )
                            )
                        }
                    }
                }

                if (importedList.isNotEmpty()) {
                    contactDao.insertContacts(importedList)
                }
            }
            onComplete(importedList.size)
        }
    }
}
