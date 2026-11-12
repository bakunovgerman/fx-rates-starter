package com.fxrates.feature.rates.detail.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.fxrates.domain.repository.RatesRepository
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Action
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.State
import me.tatarka.inject.annotations.Inject

@Inject
internal class CurrencyDetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RatesRepository,
) {

    fun createStore(base: String, code: String): CurrencyDetailStore {
        val store = storeFactory.create(
            name = "CurrencyDetailStore",
            initialState = State(base = base, code = code),
            bootstrapper = SimpleBootstrapper(Action.LoadHistory(base = base, code = code)),
            executorFactory = { CurrencyDetailExecutor(repository = repository) },
            reducer = { message -> reduce(message) },
        )
        return CurrencyDetailStore(store)
    }

}
