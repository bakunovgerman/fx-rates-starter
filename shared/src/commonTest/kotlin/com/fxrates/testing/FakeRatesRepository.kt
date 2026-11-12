package com.fxrates.testing

import com.fxrates.domain.model.CurrencyRate
import com.fxrates.domain.model.RateObservation
import com.fxrates.domain.repository.RatesRepository

class FakeRatesRepository : RatesRepository {

    var latestRates: Result<List<CurrencyRate>> = Result.success(emptyList())
    var historyResponse: suspend () -> List<RateObservation> = { emptyList() }

    val requestedBases = mutableListOf<String>()
    val historyRequests = mutableListOf<Pair<String, String>>()

    override suspend fun getLatestRates(base: String): List<CurrencyRate> {
        requestedBases += base
        return latestRates.getOrThrow()
    }

    override suspend fun getRateHistory(base: String, code: String): List<RateObservation> {
        historyRequests += base to code
        return historyResponse.invoke()
    }

}
