package com.fxrates.feature.rates.detail.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class RateHistoryRow(
    val date: String,
    val rate: String,
)
