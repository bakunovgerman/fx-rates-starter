package com.fxrates.feature.rates.detail.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.fxrates.core.utils.Resource
import com.fxrates.core.utils.resourceFlow
import com.fxrates.domain.repository.RatesRepository
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Action
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Intent
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Label
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Message
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.State
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class CurrencyDetailExecutor(
    private val repository: RatesRepository,
) : CoroutineExecutor<Intent, Action, State, Message, Label>() {

    override fun executeAction(action: Action) = when (action) {
        is Action.LoadHistory -> loadHistory(action)
    }

    override fun executeIntent(intent: Intent) {
        when (intent) {
            is Intent.OnRetry -> {
                val state = state()
                if (state.history is Resource.Error) {
                    dispatch(Message.UpdateHistory(Resource.Loading))
                    forward(Action.LoadHistory(base = state.base, code = state.code))
                }
            }
            is Intent.OnBack -> {
                publish(Label.GoBack)
            }
        }
    }

    private fun loadHistory(action: Action.LoadHistory) {
        resourceFlow { repository.getRateHistory(base = action.base, code = action.code) }
            .onEach { history -> dispatch(Message.UpdateHistory(history)) }
            .launchIn(scope)
    }

}
