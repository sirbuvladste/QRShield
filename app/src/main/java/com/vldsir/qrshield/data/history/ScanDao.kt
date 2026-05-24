package com.vldsir.qrshield.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Insert
    suspend fun insert(entity: ScanEntity): Long

    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    fun observeById(id: Long): Flow<ScanEntity?>

    @Query("DELETE FROM scans")
    suspend fun clear()
}
