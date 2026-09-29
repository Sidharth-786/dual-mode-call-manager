package com.example.dualmodecallmanager.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.dualmodecallmanager.data.model.AppMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeModeFlow = MutableStateFlow(getStoredMode())
    val activeModeFlow: StateFlow<AppMode> = _activeModeFlow.asStateFlow()

    private val _allowUnknownWorkFlow = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_UNKNOWN_WORK, false))
    val allowUnknownWorkFlow: StateFlow<Boolean> = _allowUnknownWorkFlow.asStateFlow()

    private val _allowUnknownPersonalFlow = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_UNKNOWN_PERSONAL, false))
    val allowUnknownPersonalFlow: StateFlow<Boolean> = _allowUnknownPersonalFlow.asStateFlow()

    private fun getStoredMode(): AppMode {
        val modeName = prefs.getString(KEY_ACTIVE_MODE, AppMode.WORK.name)
        return runCatching { AppMode.valueOf(modeName ?: AppMode.WORK.name) }.getOrDefault(AppMode.WORK)
    }

    var activeMode: AppMode
        get() = _activeModeFlow.value
        set(value) {
            runCatching {
                prefs.edit().putString(KEY_ACTIVE_MODE, value.name).apply()
                _activeModeFlow.value = value
            }
        }

    var allowUnknownWork: Boolean
        get() = _allowUnknownWorkFlow.value
        set(value) {
            runCatching {
                prefs.edit().putBoolean(KEY_ALLOW_UNKNOWN_WORK, value).apply()
                _allowUnknownWorkFlow.value = value
            }
        }

    var allowUnknownPersonal: Boolean
        get() = _allowUnknownPersonalFlow.value
        set(value) {
            runCatching {
                prefs.edit().putBoolean(KEY_ALLOW_UNKNOWN_PERSONAL, value).apply()
                _allowUnknownPersonalFlow.value = value
            }
        }

    fun toggleMode(): AppMode {
        val newMode = if (activeMode == AppMode.WORK) AppMode.PERSONAL else AppMode.WORK
        activeMode = newMode
        return newMode
    }

    companion object {
        private const val PREFS_NAME = "dual_mode_preferences"
        private const val KEY_ACTIVE_MODE = "active_mode"
        private const val KEY_ALLOW_UNKNOWN_WORK = "allow_unknown_work"
        private const val KEY_ALLOW_UNKNOWN_PERSONAL = "allow_unknown_personal"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
