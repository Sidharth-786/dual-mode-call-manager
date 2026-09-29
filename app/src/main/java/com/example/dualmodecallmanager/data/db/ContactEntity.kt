package com.example.dualmodecallmanager.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dualmodecallmanager.data.model.ContactCategory

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val category: ContactCategory,
    val createdAt: Long = System.currentTimeMillis()
)
