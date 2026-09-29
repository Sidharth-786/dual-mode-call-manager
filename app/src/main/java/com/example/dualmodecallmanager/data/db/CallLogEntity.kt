package com.example.dualmodecallmanager.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dualmodecallmanager.data.model.AppMode

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val callerName: String?,
    val activeMode: AppMode,
    val isAllowed: Boolean,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
