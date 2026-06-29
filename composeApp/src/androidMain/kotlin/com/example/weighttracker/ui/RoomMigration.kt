package com.example.weighttracker.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.weighttracker.data.WeightRecord
import com.example.weighttracker.data.WeightRecordList
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

object RoomMigration {
    private const val MIGRATION_DONE_KEY = "room_migration_done"
    private const val SETTINGS_NAME = "weight_settings"

    fun migrateIfNeeded(context: Context, dataDir: String) {
        val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MIGRATION_DONE_KEY, false)) return

        val dbFile = context.getDatabasePath("weight_records.db")
        if (!dbFile.exists()) {
            prefs.edit().putBoolean(MIGRATION_DONE_KEY, true).apply()
            return
        }

        val records = try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
            )
            val result = mutableListOf<WeightRecord>()
            db.rawQuery(
                "SELECT id, date, currentWeight, targetWeight FROM weight_records", null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(
                        WeightRecord(
                            id = cursor.getLong(0),
                            date = cursor.getString(1),
                            currentWeight = cursor.getDouble(2),
                            targetWeight = cursor.getDouble(3)
                        )
                    )
                }
            }
            db.close()
            result
        } catch (_: Exception) {
            emptyList()
        }

        if (records.isNotEmpty()) {
            val path = "$dataDir/weight_records.json".toPath()
            val json = Json { prettyPrint = false }
            val text = json.encodeToString(
                WeightRecordList.serializer(), WeightRecordList(records)
            )
            val fs = FileSystem.SYSTEM
            val parent = path.parent!!
            if (!fs.exists(parent)) fs.createDirectories(parent)
            fs.write(path) { writeUtf8(text) }
        }

        prefs.edit().putBoolean(MIGRATION_DONE_KEY, true).apply()
    }
}
