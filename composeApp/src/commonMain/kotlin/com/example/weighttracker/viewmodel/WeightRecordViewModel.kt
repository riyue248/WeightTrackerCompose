package com.example.weighttracker.viewmodel

import com.example.weighttracker.data.WeightRecord
import com.example.weighttracker.data.WeightRepository
import com.example.weighttracker.util.normalizeNumber
import com.example.weighttracker.util.toCleanString
import com.example.weighttracker.util.todayDateString
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class WeightRecordInput(
    val id: Long = 0,
    val date: String = todayDateString(),
    val currentWeight: String = ""
)

data class WeightTrackerUiState(
    val records: List<WeightRecord> = emptyList(),
    val targetWeight: Double? = null,
    val editor: WeightRecordInput? = null,
    val targetEditor: String? = null,
    val recordPendingDelete: WeightRecord? = null,
    val errorMessage: String? = null,
    val useJin: Boolean = false
)

class WeightRecordViewModel(
    private val repository: WeightRepository,
    private val settings: Settings,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(
        WeightTrackerUiState(
            targetWeight = loadTargetWeight(),
            useJin = loadUseJin()
        )
    )
    val uiState: StateFlow<WeightTrackerUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.records.collect { records ->
                val currentTarget = _uiState.value.targetWeight
                val legacyTarget = records.firstOrNull { it.targetWeight > 0.0 }?.targetWeight
                if (currentTarget == null && legacyTarget != null) {
                    saveTargetWeightValue(legacyTarget)
                }
                _uiState.update {
                    it.copy(
                        records = records,
                        targetWeight = it.targetWeight ?: legacyTarget
                    )
                }
            }
        }
    }

    fun openNewRecord() {
        _uiState.update {
            it.copy(
                editor = WeightRecordInput(),
                errorMessage = null
            )
        }
    }

    fun openEditor(record: WeightRecord) {
        _uiState.update { state ->
            val displayWeight = if (state.useJin) record.currentWeight * 2.0 else record.currentWeight
            state.copy(
                editor = WeightRecordInput(
                    id = record.id,
                    date = record.date,
                    currentWeight = displayWeight.toCleanString()
                ),
                errorMessage = null
            )
        }
    }

    fun updateEditorDate(date: String) {
        updateEditor { it.copy(date = date) }
    }

    fun updateCurrentWeight(value: String) {
        updateEditor { it.copy(currentWeight = value) }
    }

    fun closeEditor() {
        _uiState.update { it.copy(editor = null, errorMessage = null) }
    }

    fun saveEditor() {
        val input = _uiState.value.editor ?: return
        val date = runCatching { LocalDate.parse(input.date) }.getOrNull()
        val rawWeight = input.currentWeight.normalizeNumber().toDoubleOrNull()
        val useJin = _uiState.value.useJin
        val currentWeight = if (useJin) rawWeight?.div(2.0) else rawWeight

        when {
            date == null -> showError("请输入正确日期，格式为 YYYY-MM-DD")
            currentWeight == null || currentWeight <= 0.0 -> showError("请输入有效的当前体重")
            else -> {
                scope.launch {
                    repository.save(
                        WeightRecord(
                            id = input.id,
                            date = date.toString(),
                            currentWeight = currentWeight,
                            targetWeight = _uiState.value.targetWeight ?: 0.0
                        )
                    )
                    _uiState.update { it.copy(editor = null, errorMessage = null) }
                }
            }
        }
    }

    fun openTargetEditor() {
        _uiState.update { state ->
            val displayWeight = if (state.useJin) (state.targetWeight?.times(2.0)) else state.targetWeight
            state.copy(
                targetEditor = displayWeight?.toCleanString() ?: "",
                errorMessage = null
            )
        }
    }

    fun updateTargetWeight(value: String) {
        _uiState.update { it.copy(targetEditor = value, errorMessage = null) }
    }

    fun closeTargetEditor() {
        _uiState.update { it.copy(targetEditor = null, errorMessage = null) }
    }

    fun saveTargetWeight() {
        val rawWeight = _uiState.value.targetEditor
            ?.normalizeNumber()
            ?.toDoubleOrNull()
        val useJin = _uiState.value.useJin
        val targetWeight = if (useJin) rawWeight?.div(2.0) else rawWeight

        if (targetWeight == null || targetWeight <= 0.0) {
            showError("请输入有效的目标体重")
            return
        }

        saveTargetWeightValue(targetWeight)
        _uiState.update {
            it.copy(
                targetWeight = targetWeight,
                targetEditor = null,
                errorMessage = null
            )
        }
    }

    fun requestDelete(record: WeightRecord) {
        _uiState.update { it.copy(recordPendingDelete = record) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(recordPendingDelete = null) }
    }

    fun toggleUnit() {
        _uiState.update { state ->
            val newUseJin = !state.useJin
            settings.putBoolean(KEY_USE_JIN, newUseJin)
            state.copy(useJin = newUseJin)
        }
    }

    fun confirmDelete() {
        val record = _uiState.value.recordPendingDelete ?: return
        scope.launch {
            repository.delete(record)
            _uiState.update { it.copy(recordPendingDelete = null) }
        }
    }

    private fun updateEditor(block: (WeightRecordInput) -> WeightRecordInput) {
        _uiState.update { state ->
            state.editor?.let { state.copy(editor = block(it), errorMessage = null) } ?: state
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun loadTargetWeight(): Double? {
        val str = settings.getStringOrNull(KEY_TARGET_WEIGHT) ?: return null
        return str.toDoubleOrNull()
    }

    private fun saveTargetWeightValue(value: Double) {
        settings.putString(KEY_TARGET_WEIGHT, value.toString())
    }

    private fun loadUseJin(): Boolean {
        return settings.getBoolean(KEY_USE_JIN, false)
    }

    companion object {
        const val KEY_TARGET_WEIGHT = "target_weight"
        const val KEY_USE_JIN = "use_jin"
    }
}
