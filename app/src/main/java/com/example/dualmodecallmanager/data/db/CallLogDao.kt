package com.example.dualmodecallmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllCallLogs()

    @Query("SELECT COUNT(*) FROM call_logs WHERE timestamp >= :startOfDayTimestamp")
    fun getTodayCallCount(startOfDayTimestamp: Long): Flow<Int>
}
