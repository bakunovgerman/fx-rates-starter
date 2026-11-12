package com.fxrates.feature.rates.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fxrates.core.utils.Resource
import com.fxrates.domain.model.RateObservation
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.LocalDate

internal class CurrencyDetailUiStatePreviewProvider : PreviewParameterProvider<CurrencyDetailUiState> {

    private val converter = CurrencyDetailUiStateConverter()

    override val values: Sequence<CurrencyDetailUiState> = sequenceOf(
        Resource.Loading,
        Resource.Error(IllegalStateException("Preview request failed")),
        Resource.Data(emptyList()),
        Resource.Data(
            listOf(
                RateObservation(date = LocalDate(2026, 11, 12), rate = "0.8916".toBigDecimal()),
                RateObservation(date = LocalDate(2026, 11, 11), rate = "0.8879".toBigDecimal()),
                RateObservation(date = LocalDate(2026, 11, 10), rate = "0.8882".toBigDecimal()),
            ),
        ),
    ).map { history ->
        converter.createUiState(CurrencyDetailStore.State(base = "USD", code = "EUR", history = history))
    }

}
