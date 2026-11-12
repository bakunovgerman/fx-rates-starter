package com.fxrates.data.repository

import com.fxrates.data.remote.FrankfurterApi
import com.fxrates.domain.model.CurrencyRate
import com.fxrates.domain.model.RateObservation
import com.fxrates.domain.repository.RatesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

@Inject
class RatesRepositoryImpl(
    private val api: FrankfurterApi,
    private val converter: RatesConverter,
    private val clock: Clock,
) : RatesRepository {

    override suspend fun getLatestRates(base: String): List<CurrencyRate> = coroutineScope {
        // A week back always holds at least two business days, even around long holidays.
        val start = clock.todayIn(TimeZone.UTC).minus(DatePeriod(days = LOOKBACK_DAYS))
        val currencies = async { api.getCurrencies() }
        val timeSeries = async { api.getTimeSeries(base = base, start = start) }
        converter.toLatestRates(timeSeries = timeSeries.await(), currencies = currencies.await())
    }

    override suspend fun getRateHistory(base: String, code: String): List<RateObservation> {
        val end = clock.todayIn(TimeZone.UTC)
        val start = end.minus(DatePeriod(days = HISTORY_WINDOW_DAYS - 1))
        val timeSeries = api.getTimeSeries(base = base, start = start, end = end)
        return converter.toRateHistory(timeSeries = timeSeries, code = code, start = start, end = end)
    }

    private companion object {

        const val LOOKBACK_DAYS = 7
        const val HISTORY_WINDOW_DAYS = 30

    }

}
