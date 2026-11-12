package com.fxrates.domain.repository

import com.fxrates.domain.model.CurrencyRate
import com.fxrates.domain.model.RateObservation

interface RatesRepository {

    /** The latest rates of every supported currency against [base], sorted by code. */
    suspend fun getLatestRates(base: String): List<CurrencyRate>

    /** Available quotes for [code] against [base] from the last 30 UTC dates, newest first. */
    suspend fun getRateHistory(base: String, code: String): List<RateObservation>

}
