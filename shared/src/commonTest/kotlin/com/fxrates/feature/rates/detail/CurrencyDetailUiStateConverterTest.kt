package com.fxrates.feature.rates.detail

import com.fxrates.core.utils.Resource
import com.fxrates.domain.model.RateObservation
import com.fxrates.feature.rates.detail.CurrencyDetailUiState.Content
import com.fxrates.feature.rates.detail.model.RateHistoryRow
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate

class CurrencyDetailUiStateConverterTest : FunSpec({

    val converter = CurrencyDetailUiStateConverter()
    val initialState = CurrencyDetailStore.State(base = "USD", code = "EUR")

    fun observation(day: Int, rate: String): RateObservation {
        return RateObservation(LocalDate(2026, 11, day), rate.toBigDecimal())
    }

    fun content(vararg observations: RateObservation): Content {
        return converter.createUiState(initialState.copy(history = Resource.Data(observations.toList()))).content
    }

    test("loading keeps the currency pair heading visible") {
        val uiState = converter.createUiState(initialState)

        uiState.heading shouldBe "USD / EUR"
        uiState.content shouldBe Content.Loading
    }

    test("a failed request gives the error content") {
        val state = initialState.copy(history = Resource.Error(IllegalStateException("offline")))

        converter.createUiState(state).content shouldBe Content.Error
    }

    test("a successful request without observations gives the no-rates content") {
        content() shouldBe Content.Empty
    }

    test("a single observation is the latest quote and has no period change") {
        val history = content(observation(11, "0.88123")).shouldBeInstanceOf<Content.History>()
        val expectedRow = RateHistoryRow(date = "2026-11-11", rate = "0.8812")

        history.latest shouldBe expectedRow
        history.rows shouldBe listOf(expectedRow)
        history.absoluteChange shouldBe null
        history.percentChange shouldBe null
    }

    test("an increase compares the earliest quote with the dated latest quote") {
        val history = content(
            observation(12, "125"),
            observation(11, "120"),
            observation(10, "100"),
        ).shouldBeInstanceOf<Content.History>()

        history.latest shouldBe RateHistoryRow(date = "2026-11-12", rate = "125.0000")
        history.rows shouldBe listOf(
            RateHistoryRow(date = "2026-11-12", rate = "125.0000"),
            RateHistoryRow(date = "2026-11-11", rate = "120.0000"),
            RateHistoryRow(date = "2026-11-10", rate = "100.0000"),
        )
        history.absoluteChange shouldBe "+25.0000"
        history.percentChange shouldBe "+25.00%"
    }

    test("a decrease has a negative amount and a negative percentage") {
        val history = content(observation(12, "100"), observation(11, "125")).shouldBeInstanceOf<Content.History>()

        history.absoluteChange shouldBe "-25.0000"
        history.percentChange shouldBe "-20.00%"
    }

    context("unchanged and tiny movements display as unsigned zero when rounded") {
        withTests("100", "100.00001", "99.99999") { current ->
            val history = content(
                observation(12, current),
                observation(11, "100"),
            ).shouldBeInstanceOf<Content.History>()

            history.absoluteChange shouldBe "0.0000"
            history.percentChange shouldBe "0.00%"
        }
    }

    test("a zero earliest quote keeps the absolute change without a percentage") {
        val history = content(observation(12, "1"), observation(11, "0")).shouldBeInstanceOf<Content.History>()

        history.absoluteChange shouldBe "+1.0000"
        history.percentChange shouldBe null
    }

})
