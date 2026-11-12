package com.fxrates.feature.rates.detail

import com.fxrates.core.format.NumberFormatter
import com.fxrates.core.money.RateChangeCalculator
import com.fxrates.core.utils.Resource
import com.fxrates.domain.model.RateObservation
import com.fxrates.feature.rates.detail.CurrencyDetailUiState.Content
import com.fxrates.feature.rates.detail.model.RateHistoryRow
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore
import com.fxrates.ui.Strings
import me.tatarka.inject.annotations.Inject

@Inject
internal class CurrencyDetailUiStateConverter {

    fun createUiState(state: CurrencyDetailStore.State): CurrencyDetailUiState {
        val content = when (val history = state.history) {
            is Resource.Loading -> Content.Loading
            is Resource.Error -> Content.Error
            is Resource.Data -> createHistory(history.value)
        }
        return CurrencyDetailUiState(
            heading = Strings.currencyPair(base = state.base, code = state.code),
            content = content,
        )
    }

    private fun createHistory(observations: List<RateObservation>): Content {
        val latest = observations.firstOrNull() ?: return Content.Empty
        val change = if (observations.size > 1) {
            RateChangeCalculator.calculate(previous = observations.last().rate, current = latest.rate)
        } else {
            null
        }
        val rows = observations.map { observation ->
            RateHistoryRow(
                date = observation.date.toString(),
                rate = NumberFormatter.formatRate(observation.rate),
            )
        }
        return Content.History(
            latest = rows.first(),
            rows = rows,
            absoluteChange = change?.absolute?.let(NumberFormatter::formatSignedRate),
            percentChange = change?.percent?.let(NumberFormatter::formatSignedPercent),
        )
    }

}
