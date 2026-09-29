package com.example.dualmodecallmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dualmodecallmanager.data.model.ContactCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getAllContactsList(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE category = :category OR category = 'BOTH' ORDER BY name ASC")
    fun getContactsByCategory(category: ContactCategory): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE category = :category OR category = 'BOTH' ORDER BY name ASC")
    suspend fun getContactsByCategoryList(category: ContactCategory): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT COUNT(*) FROM contacts WHERE category = :category OR category = 'BOTH'")
    fun getContactCountByCategory(category: ContactCategory): Flow<Int>
}
