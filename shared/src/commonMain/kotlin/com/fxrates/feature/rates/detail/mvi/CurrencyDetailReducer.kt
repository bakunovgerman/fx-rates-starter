package com.fxrates.feature.rates.detail.mvi

import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.Message
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore.State

internal fun State.reduce(message: Message): State = when (message) {
    is Message.UpdateHistory -> copy(history = message.history)
}
