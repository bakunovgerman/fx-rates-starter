package com.fxrates.feature.rates.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStore
import com.fxrates.feature.rates.detail.mvi.CurrencyDetailStoreFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.AssistedFactory
import me.tatarka.inject.annotations.Inject

abstract class CurrencyDetailUiComponent {

    internal abstract val state: StateFlow<CurrencyDetailUiState>

    internal abstract fun onRetry()

    internal abstract fun onBack()

}

@Inject
internal class CurrencyDetailUiComponentDefault(
    @Assisted componentContext: ComponentContext,
    @Assisted base: String,
    @Assisted code: String,
    @Assisted private val callbacks: Callbacks,
    storeFactory: CurrencyDetailStoreFactory,
    uiStateConverter: CurrencyDetailUiStateConverter,
) : CurrencyDetailUiComponent(), ComponentContext by componentContext {

    @AssistedFactory
    interface Factory {

        fun create(
            componentContext: ComponentContext,
            base: String,
            code: String,
            callbacks: Callbacks,
        ): CurrencyDetailUiComponentDefault

    }

    interface Callbacks {

        fun onBack()

    }

    private val scope = coroutineScope()

    private val store = storeFactory.createStore(base = base, code = code)

    override val state: StateFlow<CurrencyDetailUiState> = store.states
        .map(uiStateConverter::createUiState)
        .stateIn(scope, SharingStarted.Eagerly, uiStateConverter.createUiState(store.state))

    init {
        lifecycle.doOnDestroy(store::dispose)
        store.labels
            .onEach(::handleLabel)
            .launchIn(scope)
    }

    private fun handleLabel(label: CurrencyDetailStore.Label) {
        when (label) {
            is CurrencyDetailStore.Label.GoBack -> callbacks.onBack()
        }
    }

    override fun onRetry() {
        store.accept(CurrencyDetailStore.Intent.OnRetry)
    }

    override fun onBack() {
        store.accept(CurrencyDetailStore.Intent.OnBack)
    }

}
