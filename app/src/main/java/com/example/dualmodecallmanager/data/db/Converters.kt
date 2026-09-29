package com.example.dualmodecallmanager.data.db

import androidx.room.TypeConverter
import com.example.dualmodecallmanager.data.model.AppMode
import com.example.dualmodecallmanager.data.model.ContactCategory

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromAppMode(mode: AppMode): String = mode.name

    @TypeConverter
    @JvmStatic
    fun toAppMode(name: String): AppMode = runCatching { AppMode.valueOf(name) }.getOrDefault(AppMode.WORK)

    @TypeConverter
    @JvmStatic
    fun fromContactCategory(category: ContactCategory): String = category.name

    @TypeConverter
    @JvmStatic
    fun toContactCategory(name: String): ContactCategory = runCatching { ContactCategory.valueOf(name) }.getOrDefault(ContactCategory.WORK)
}
