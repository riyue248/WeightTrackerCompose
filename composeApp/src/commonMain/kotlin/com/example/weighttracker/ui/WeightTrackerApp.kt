package com.example.weighttracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weighttracker.data.WeightRecord
import com.example.weighttracker.util.formatWeight
import com.example.weighttracker.util.toDisplayWeight
import com.example.weighttracker.util.toLocalDateOrToday
import com.example.weighttracker.viewmodel.WeightRecordInput
import com.example.weighttracker.viewmodel.WeightTrackerUiState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

@Composable
fun WeightTrackerRoot() {
    val viewModel = rememberViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeightTrackerApp(
        uiState = uiState,
        onAddClick = viewModel::openNewRecord,
        onEditClick = viewModel::openEditor,
        onDeleteClick = viewModel::requestDelete,
        onDateChange = viewModel::updateEditorDate,
        onCurrentWeightChange = viewModel::updateCurrentWeight,
        onSaveEditor = viewModel::saveEditor,
        onCancelEditor = viewModel::closeEditor,
        onTargetClick = viewModel::openTargetEditor,
        onTargetChange = viewModel::updateTargetWeight,
        onSaveTarget = viewModel::saveTargetWeight,
        onCancelTarget = viewModel::closeTargetEditor,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
        onToggleUnit = viewModel::toggleUnit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightTrackerApp(
    uiState: WeightTrackerUiState,
    onAddClick: () -> Unit,
    onEditClick: (WeightRecord) -> Unit,
    onDeleteClick: (WeightRecord) -> Unit,
    onDateChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onSaveEditor: () -> Unit,
    onCancelEditor: () -> Unit,
    onTargetClick: () -> Unit,
    onTargetChange: (String) -> Unit,
    onSaveTarget: () -> Unit,
    onCancelTarget: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onToggleUnit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("体重记录") },
                actions = {
                    UnitToggleChip(
                        useJin = uiState.useJin,
                        onToggle = onToggleUnit
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("新增记录") }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            WeightTrackerContent(
                records = uiState.records,
                targetWeight = uiState.targetWeight,
                useJin = uiState.useJin,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onTargetClick = onTargetClick
            )
        }
    }

    uiState.editor?.let { input ->
        WeightRecordEditorDialog(
            input = input,
            errorMessage = uiState.errorMessage,
            useJin = uiState.useJin,
            onDateChange = onDateChange,
            onCurrentWeightChange = onCurrentWeightChange,
            onSave = onSaveEditor,
            onCancel = onCancelEditor
        )
    }

    uiState.targetEditor?.let { targetInput ->
        TargetWeightDialog(
            value = targetInput,
            errorMessage = uiState.errorMessage,
            useJin = uiState.useJin,
            onValueChange = onTargetChange,
            onSave = onSaveTarget,
            onCancel = onCancelTarget
        )
    }

    uiState.recordPendingDelete?.let { record ->
        DeleteConfirmDialog(
            record = record,
            onConfirm = onConfirmDelete,
            onCancel = onCancelDelete
        )
    }
}

@Composable
private fun WeightTrackerContent(
    records: List<WeightRecord>,
    targetWeight: Double?,
    useJin: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (WeightRecord) -> Unit,
    onDeleteClick: (WeightRecord) -> Unit,
    onTargetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TargetWeightCard(
            targetWeight = targetWeight,
            useJin = useJin,
            onClick = onTargetClick
        )

        if (records.isEmpty()) {
            EmptyState(
                onAddClick = onAddClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            WeightRecordList(
                records = records,
                targetWeight = targetWeight,
                useJin = useJin,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TargetWeightCard(
    targetWeight: Double?,
    useJin: Boolean,
    onClick: () -> Unit
) {
    val unitLabel = if (useJin) "斤" else "kg"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Scale,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "目标体重",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = targetWeight?.let { "${it.toDisplayWeight(useJin).formatWeight()} $unitLabel" } ?: "未设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            TextButton(onClick = onClick) {
                Text(if (targetWeight == null) "设置" else "修改")
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Scale,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "还没有体重记录",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("新增第一条记录")
            }
        }
    }
}

@Composable
private fun WeightRecordList(
    records: List<WeightRecord>,
    targetWeight: Double?,
    useJin: Boolean,
    onEditClick: (WeightRecord) -> Unit,
    onDeleteClick: (WeightRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = records,
            key = { _, record -> record.id }
        ) { index, record ->
            val previousWeight = records.getOrNull(index + 1)?.currentWeight
            WeightRecordCard(
                record = record,
                targetWeight = targetWeight,
                useJin = useJin,
                previousWeight = previousWeight,
                onEditClick = { onEditClick(record) },
                onDeleteClick = { onDeleteClick(record) }
            )
        }
    }
}

@Composable
private fun WeightRecordCard(
    record: WeightRecord,
    targetWeight: Double?,
    useJin: Boolean,
    previousWeight: Double?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val unitLabel = if (useJin) "斤" else "kg"
    val displayWeight = record.currentWeight.toDisplayWeight(useJin)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "当前 ${displayWeight.formatWeight()} $unitLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    previousWeight?.let { prev ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = differenceText(record.currentWeight, prev, useJin),
                            style = MaterialTheme.typography.bodySmall,
                            color = differenceColor(record.currentWeight, prev)
                        )
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            targetWeight?.let {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = distanceText(record.currentWeight, it, useJin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightRecordEditorDialog(
    input: WeightRecordInput,
    errorMessage: String?,
    useJin: Boolean,
    onDateChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val unitLabel = if (useJin) "斤" else "kg"

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(if (input.id == 0L) "新增体重记录" else "编辑体重记录")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = input.date,
                    onValueChange = onDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("日期") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                        }
                    }
                )
                OutlinedTextField(
                    value = input.currentWeight,
                    onValueChange = onCurrentWeightChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("当前体重 $unitLabel") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = input.date.toLocalDateOrToday().toUtcMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChange(it.toDateString())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TargetWeightDialog(
    value: String,
    errorMessage: String?,
    useJin: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val unitLabel = if (useJin) "斤" else "kg"
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("设置目标体重") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("目标体重 $unitLabel") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    record: WeightRecord,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("确认删除") },
        text = { Text("确定要删除 ${record.date} 的体重记录吗？") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}

// --- Helper functions ---

private fun distanceText(currentWeight: Double, targetWeight: Double, useJin: Boolean): String {
    val current = currentWeight.toDisplayWeight(useJin)
    val target = targetWeight.toDisplayWeight(useJin)
    val distance = abs(current - target).formatWeight()
    val unitLabel = if (useJin) "斤" else "kg"
    return when {
        currentWeight > targetWeight -> "距离目标还需减少 $distance $unitLabel"
        currentWeight < targetWeight -> "距离目标还需增加 $distance $unitLabel"
        else -> "已经达到目标体重"
    }
}

private fun differenceText(currentWeight: Double, previousWeight: Double, useJin: Boolean): String {
    val current = currentWeight.toDisplayWeight(useJin)
    val previous = previousWeight.toDisplayWeight(useJin)
    val diff = abs(current - previous).formatWeight()
    val unitLabel = if (useJin) "斤" else "kg"
    return when {
        currentWeight > previousWeight -> "比上次重 $diff $unitLabel"
        currentWeight < previousWeight -> "比上次轻 $diff $unitLabel"
        else -> "与上次相同"
    }
}

@Composable
private fun differenceColor(currentWeight: Double, previousWeight: Double): Color {
    return when {
        currentWeight > previousWeight -> MaterialTheme.colorScheme.error
        currentWeight < previousWeight -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun UnitToggleChip(
    useJin: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = useJin,
        onClick = onToggle,
        label = { Text(if (useJin) "斤" else "kg") },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.padding(end = 8.dp)
    )
}

// --- kotlinx-datetime replacements for java.time ---

private fun LocalDate.toUtcMillis(): Long {
    return Instant.parse("${this}T00:00:00Z").toEpochMilliseconds()
}

private fun Long.toDateString(): String {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC).date.toString()
}
