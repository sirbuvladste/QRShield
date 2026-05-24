package com.vldsir.qrshield.data.history

import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.analysis.risk.VerdictExplanation
import com.vldsir.qrshield.classifier.PayloadType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScanHistoryRepository(private val dao: ScanDao) {

    suspend fun insert(record: ScanRecord): Long {
        val entity = record.toEntity()
        return dao.insert(entity)
    }

    fun observeAll(): Flow<List<ScanRecord>> =
        dao.observeAll().map { list -> list.map { it.toRecord() } }

    fun observeById(id: Long): Flow<ScanRecord?> =
        dao.observeById(id).map { it?.toRecord() }

    suspend fun clear() = dao.clear()

    private fun ScanRecord.toEntity() = ScanEntity(
        id = id,
        timestamp = timestamp,
        payload = payload,
        payloadType = payloadType.name,
        verdict = verdict.name,
        explanationHeadline = explanation.headline,
        explanationBullets = explanation.bullets.joinToString("\n"),
        onlineLookupUsed = onlineLookupUsed,
    )

    private fun ScanEntity.toRecord() = ScanRecord(
        id = id,
        timestamp = timestamp,
        payload = payload,
        payloadType = PayloadType.valueOf(payloadType),
        verdict = Verdict.valueOf(verdict),
        explanation = VerdictExplanation(
            headline = explanationHeadline,
            bullets = if (explanationBullets.isEmpty()) emptyList()
            else explanationBullets.split("\n"),
        ),
        onlineLookupUsed = onlineLookupUsed,
    )
}
