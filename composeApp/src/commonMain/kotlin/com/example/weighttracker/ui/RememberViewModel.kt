package com.example.weighttracker.ui

import androidx.compose.runtime.Composable
import com.example.weighttracker.viewmodel.WeightRecordViewModel

@Composable
expect fun rememberViewModel(): WeightRecordViewModel
