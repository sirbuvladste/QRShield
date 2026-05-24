package com.vldsir.qrshield.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanEntity::class], version = 1, exportSchema = true)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}
