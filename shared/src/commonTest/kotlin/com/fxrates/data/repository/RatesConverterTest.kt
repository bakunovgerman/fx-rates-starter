package com.fxrates.data.repository

import com.fxrates.data.remote.dto.TimeSeriesDto
import com.fxrates.domain.model.CurrencyRate
import com.fxrates.domain.model.RateObservation
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class RatesConverterTest : FunSpec({

    val converter = RatesConverter()
    val historyStart = LocalDate(2026, 10, 14)
    val historyEnd = LocalDate(2026, 11, 12)

    fun history(rates: Map<String, Map<String, Double>>): List<RateObservation> {
        val timeSeries = TimeSeriesDto(
            base = "USD",
            startDate = historyStart.toString(),
            endDate = historyEnd.toString(),
            rates = rates,
        )
        return converter.toRateHistory(timeSeries, code = "EUR", start = historyStart, end = historyEnd)
    }

    test("the last date is the latest rate and the one before it is the previous rate") {
        val timeSeries = TimeSeriesDto(
            base = "USD",
            startDate = "2026-10-29",
            endDate = "2026-11-02",
            rates = mapOf(
                "2026-11-02" to mapOf("EUR" to 0.89158),
                "2026-10-29" to mapOf("EUR" to 0.89847),
                "2026-10-30" to mapOf("EUR" to 0.89509),
            ),
        )

        val rates = converter.toLatestRates(timeSeries, currencies = mapOf("EUR" to "Euro"))

        rates shouldHaveSize 1
        with(rates.single()) {
            code shouldBe "EUR"
            name shouldBe "Euro"
            date shouldBe LocalDate(2026, 11, 2)
            rate shouldBe "0.89158".toBigDecimal()
            previousRate shouldBe "0.89509".toBigDecimal()
        }
    }

    test("a single date gives no previous rate and an unknown code keeps its code as the name") {
        val timeSeries = TimeSeriesDto(
            base = "USD",
            startDate = "2026-11-02",
            endDate = "2026-11-02",
            rates = mapOf("2026-11-02" to mapOf("XAU" to 0.0003)),
        )

        val rate = converter.toLatestRates(timeSeries, currencies = emptyMap()).single()

        rate.name shouldBe "XAU"
        rate.previousRate shouldBe null
    }

    test("the rates are sorted by code") {
        val timeSeries = TimeSeriesDto(
            base = "USD",
            startDate = "2026-11-02",
            endDate = "2026-11-02",
            rates = mapOf("2026-11-02" to mapOf("JPY" to 157.9, "CHF" to 0.8, "EUR" to 0.89)),
        )

        val codes = converter.toLatestRates(timeSeries, currencies = emptyMap()).map(CurrencyRate::code)

        codes shouldBe listOf("CHF", "EUR", "JPY")
    }

    test("an empty time series gives no rates") {
        val timeSeries = TimeSeriesDto(base = "USD", startDate = "", endDate = "", rates = emptyMap())

        converter.toLatestRates(timeSeries, currencies = emptyMap()).shouldBeEmpty()
    }

    test("history keeps both inclusive boundaries and orders available quotes newest first") {
        val observations = history(
            mapOf(
                "2026-11-06" to mapOf("EUR" to 0.87412),
                "2026-10-13" to mapOf("EUR" to 0.89345),
                "2026-11-12" to mapOf("EUR" to 0.88123),
                "2026-11-13" to mapOf("EUR" to 0.89456),
                "2026-10-14" to mapOf("EUR" to 0.86897),
                "2026-11-09" to mapOf("EUR" to 0.87765),
                "2026-11-10" to mapOf("JPY" to 157.9),
            ),
        )

        observations shouldBe listOf(
            RateObservation(historyEnd, "0.88123".toBigDecimal()),
            RateObservation(LocalDate(2026, 11, 9), "0.87765".toBigDecimal()),
            RateObservation(LocalDate(2026, 11, 6), "0.87412".toBigDecimal()),
            RateObservation(historyStart, "0.86897".toBigDecimal()),
        )
    }

    test("the only retained quote can come from before the newest response date") {
        val observations = history(
            mapOf(
                "2026-11-12" to mapOf("JPY" to 157.9),
                "2026-11-11" to mapOf("EUR" to 0.88123),
            ),
        )

        observations shouldBe listOf(RateObservation(LocalDate(2026, 11, 11), "0.88123".toBigDecimal()))
    }

    test("history is empty if the selected currency appears only outside the window") {
        history(
            mapOf(
                "2026-10-13" to mapOf("EUR" to 0.89),
                "2026-11-12" to mapOf("JPY" to 157.9),
            ),
        ).shouldBeEmpty()
    }

})
