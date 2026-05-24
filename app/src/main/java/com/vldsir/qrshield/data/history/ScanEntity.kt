package com.vldsir.qrshield.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val payload: String,
    val payloadType: String,
    val verdict: String,
    val explanationHeadline: String,
    val explanationBullets: String,
    val onlineLookupUsed: Boolean,
)
