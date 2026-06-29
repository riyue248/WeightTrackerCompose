package com.example.weighttracker.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.weighttracker.data.WeightRepository
import com.example.weighttracker.viewmodel.WeightRecordViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

@Composable
actual fun rememberViewModel(): WeightRecordViewModel {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember {
        // Run Room -> JSON migration before creating repository
        val dataDir = context.filesDir.absolutePath
        RoomMigration.migrateIfNeeded(context, dataDir)
        WeightRecordViewModel(
            repository = WeightRepository(dataDir),
            settings = SharedPreferencesSettings(
                context.getSharedPreferences("weight_settings", Context.MODE_PRIVATE)
            ),
            scope = scope
        )
    }
}
