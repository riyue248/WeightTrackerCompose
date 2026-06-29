package com.example.weighttracker

import androidx.compose.ui.window.ComposeUIViewController
import com.example.weighttracker.ui.WeightTrackerRoot
import com.example.weighttracker.ui.theme.WeightTrackerTheme

fun MainViewController() = ComposeUIViewController {
    WeightTrackerTheme {
        WeightTrackerRoot()
    }
}
