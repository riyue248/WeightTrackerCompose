package com.example.weighttracker.data

import kotlinx.serialization.Serializable

@Serializable
data class WeightRecord(
    val id: Long = 0,
    val date: String,
    val currentWeight: Double,
    val targetWeight: Double
)

@Serializable
data class WeightRecordList(
    val records: List<WeightRecord> = emptyList()
)
