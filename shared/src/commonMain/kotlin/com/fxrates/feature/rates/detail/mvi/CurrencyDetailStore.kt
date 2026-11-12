package com.fxrates.feature.rates.detail.mvi

import com.arkivanov.mvikotlin.core.store.Store
import com.fxrates.core.utils.Resource
import com.fxrates.domain.model.RateObservation

internal class CurrencyDetailStore(
    store: Store<Intent, State, Label>,
) : Store<CurrencyDetailStore.Intent, CurrencyDetailStore.State, CurrencyDetailStore.Label> by store {

    sealed class Intent {

        data object OnRetry : Intent()

        data object OnBack : Intent()

    }

    sealed class Action {

        data class LoadHistory(val base: String, val code: String) : Action()

    }

    sealed class Message {

        data class UpdateHistory(val history: Resource<List<RateObservation>>) : Message()

    }

    sealed class Label {

        data object GoBack : Label()

    }

    data class State(
        val base: String,
        val code: String,
        val history: Resource<List<RateObservation>> = Resource.Loading,
    )

}
