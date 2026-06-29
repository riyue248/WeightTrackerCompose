package com.example.weighttracker.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.normalizeNumber(): String = replace(',', '.').trim()

fun Double.toCleanString(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

fun Double.toDisplayWeight(useJin: Boolean): Double {
    return if (useJin) this * 2.0 else this
}

fun Double.formatWeight(): String {
    if (this % 1.0 == 0.0) {
        return toInt().toString()
    }
    // Manual rounding to 1 decimal place
    val rounded = (this * 10).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

fun todayDateString(): String = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

fun String.toLocalDateOrToday(): LocalDate {
    return runCatching { LocalDate.parse(this) }.getOrDefault(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
}
