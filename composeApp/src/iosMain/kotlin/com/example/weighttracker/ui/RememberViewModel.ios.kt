package com.example.weighttracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.weighttracker.data.WeightRepository
import com.example.weighttracker.viewmodel.WeightRecordViewModel
import com.russhwolf.settings.Settings
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@Composable
actual fun rememberViewModel(): WeightRecordViewModel {
    val scope = rememberCoroutineScope()
    return remember {
        val dataDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).first() as String
        WeightRecordViewModel(
            repository = WeightRepository(dataDir),
            settings = Settings(),
            scope = scope
        )
    }
}
