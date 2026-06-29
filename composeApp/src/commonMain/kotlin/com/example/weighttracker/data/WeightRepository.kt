package com.example.weighttracker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

class WeightRepository(private val storageDir: String) {
    private val filePath = "$storageDir/weight_records.json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _records = MutableStateFlow<List<WeightRecord>>(emptyList())
    val records: StateFlow<List<WeightRecord>> = _records.asStateFlow()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        val path = filePath.toPath()
        val fs = FileSystem.SYSTEM
        if (!fs.exists(path)) return
        try {
            val text = fs.read(path) { readUtf8() }
            val list = json.decodeFromString<WeightRecordList>(text)
            _records.value = list.records.sortedByDescending { "${it.date}${it.id}" }
        } catch (_: Exception) {
            _records.value = emptyList()
        }
    }

    suspend fun save(record: WeightRecord) {
        withContext(Dispatchers.IO) {
            val current = _records.value.toMutableList()
            val existing = current.indexOfFirst { it.id == record.id }
            val saved = if (record.id == 0L) {
                record.copy(id = nextId())
            } else {
                record
            }
            if (existing >= 0) {
                current[existing] = saved
            } else {
                current.add(saved)
            }
            writeToDisk(current)
        }
    }

    suspend fun delete(record: WeightRecord) {
        withContext(Dispatchers.IO) {
            val current = _records.value.toMutableList()
            current.removeAll { it.id == record.id }
            writeToDisk(current)
        }
    }

    private fun writeToDisk(records: List<WeightRecord>) {
        val path = filePath.toPath()
        val fs = FileSystem.SYSTEM
        val parent = path.parent ?: return
        if (!fs.exists(parent)) fs.createDirectories(parent)
        val text = json.encodeToString(WeightRecordList.serializer(), WeightRecordList(records))
        fs.write(path) { writeUtf8(text) }
        _records.value = records.sortedByDescending { "${it.date}${it.id}" }
    }

    private fun nextId(): Long = (_records.value.maxOfOrNull { it.id } ?: 0) + 1

    /** For migration: bulk import records from old Room DB */
    suspend fun importRecords(records: List<WeightRecord>) {
        withContext(Dispatchers.IO) {
            val merged = (_records.value + records).distinctBy { it.id }
            writeToDisk(merged)
        }
    }
}
