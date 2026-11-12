package com.fxrates.feature.rates.detail

import androidx.compose.runtime.Immutable
import com.fxrates.feature.rates.detail.model.RateHistoryRow

@Immutable
internal data class CurrencyDetailUiState(
    val heading: String,
    val content: Content,
) {

    @Immutable
    sealed class Content {

        data object Loading : Content()

        data object Error : Content()

        data object Empty : Content()

        data class History(
            val latest: RateHistoryRow,
            val rows: List<RateHistoryRow>,
            val absoluteChange: String?,
            val percentChange: String?,
        ) : Content()

    }

}
