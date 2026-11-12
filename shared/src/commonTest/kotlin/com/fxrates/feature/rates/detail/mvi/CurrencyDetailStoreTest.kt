package com.fxrates.feature.rates.detail.mvi

import com.arkivanov.mvikotlin.core.rx.observer
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.fxrates.core.utils.Resource
import com.fxrates.domain.model.RateObservation
import com.fxrates.testing.FakeRatesRepository
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyDetailStoreTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()
    val scheduler = testDispatcher.scheduler
    val history = listOf(RateObservation(LocalDate(2026, 11, 11), "0.88".toBigDecimal()))

    beforeSpec {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    fun createStore(repository: FakeRatesRepository): CurrencyDetailStore {
        val factory = CurrencyDetailStoreFactory(storeFactory = DefaultStoreFactory(), repository = repository)
        return factory.createStore(base = "USD", code = "EUR")
    }

    test("the selected pair loads once on start and shows loading until history arrives") {
        val response = CompletableDeferred<List<RateObservation>>()
        val repository = FakeRatesRepository().apply { historyResponse = { response.await() } }
        val store = createStore(repository)

        try {
            scheduler.runCurrent()
            store.state.history shouldBe Resource.Loading
            repository.historyRequests shouldBe listOf("USD" to "EUR")

            response.complete(history)
            scheduler.runCurrent()

            store.state.history shouldBe Resource.Data(history)
            repository.historyRequests shouldBe listOf("USD" to "EUR")
        } finally {
            store.dispose()
        }
    }

    test("a failed history request can be retried with the same pair") {
        val failure = IllegalStateException("offline")
        val repository = FakeRatesRepository().apply { historyResponse = { throw failure } }
        val store = createStore(repository)

        try {
            scheduler.runCurrent()
            store.state.history shouldBe Resource.Error(failure)

            val response = CompletableDeferred<List<RateObservation>>()
            repository.historyResponse = { response.await() }
            store.accept(CurrencyDetailStore.Intent.OnRetry)
            scheduler.runCurrent()

            store.state.history shouldBe Resource.Loading
            repository.historyRequests shouldBe listOf("USD" to "EUR", "USD" to "EUR")

            response.complete(history)
            scheduler.runCurrent()

            store.state.history shouldBe Resource.Data(history)
        } finally {
            store.dispose()
        }
    }

    test("two retry taps before the request starts still load history only once") {
        val failure = IllegalStateException("offline")
        val repository = FakeRatesRepository().apply { historyResponse = { throw failure } }
        val store = createStore(repository)

        try {
            scheduler.runCurrent()
            store.state.history shouldBe Resource.Error(failure)
            repository.historyResponse = { history }

            store.accept(CurrencyDetailStore.Intent.OnRetry)
            store.accept(CurrencyDetailStore.Intent.OnRetry)
            scheduler.runCurrent()

            repository.historyRequests shouldBe listOf("USD" to "EUR", "USD" to "EUR")
            store.state.history shouldBe Resource.Data(history)
        } finally {
            store.dispose()
        }
    }

    test("an empty history is a successful load") {
        val store = createStore(FakeRatesRepository())

        try {
            scheduler.runCurrent()
            store.state.history shouldBe Resource.Data(emptyList<RateObservation>())
        } finally {
            store.dispose()
        }
    }

    test("back emits a navigation label even while history is loading") {
        val response = CompletableDeferred<List<RateObservation>>()
        val repository = FakeRatesRepository().apply { historyResponse = { response.await() } }
        val store = createStore(repository)
        val labels = mutableListOf<CurrencyDetailStore.Label>()
        val subscription = store.labels(observer { label -> labels.add(label) })

        try {
            scheduler.runCurrent()
            store.state.history shouldBe Resource.Loading
            store.accept(CurrencyDetailStore.Intent.OnBack)

            labels shouldBe listOf(CurrencyDetailStore.Label.GoBack)
        } finally {
            subscription.dispose()
            store.dispose()
            scheduler.runCurrent()
        }
    }

})
