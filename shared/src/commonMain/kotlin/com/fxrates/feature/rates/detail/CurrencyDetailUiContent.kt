package com.fxrates.feature.rates.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.fxrates.feature.rates.detail.CurrencyDetailUiState.Content
import com.fxrates.feature.rates.detail.model.RateHistoryRow
import com.fxrates.ui.Strings
import com.fxrates.ui.theme.AppTheme

@Composable
fun CurrencyDetailUiContent(
    component: CurrencyDetailUiComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    CurrencyDetailUiContent(
        state = state,
        onBack = component::onBack,
        onRetry = component::onRetry,
        modifier = modifier,
    )
}

@Composable
private fun CurrencyDetailUiContent(
    state: CurrencyDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier
        .fillMaxSize()
        .safeDrawingPadding(),
) {
    Row(
        modifier = Modifier.padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        TextButton(onClick = onBack) {
            Text(Strings.BACK)
        }
        Text(state.heading, style = MaterialTheme.typography.headlineMedium)
    }
    when (val content = state.content) {
        is Content.Loading -> {
            Centered { CircularProgressIndicator() }
        }
        is Content.Error -> {
            ErrorState(onRetry = onRetry)
        }
        is Content.Empty -> {
            Centered { Text(Strings.CURRENCY_DETAIL_NO_RATES, style = MaterialTheme.typography.bodyLarge) }
        }
        is Content.History -> {
            RateHistory(content)
        }
    }
}

@Composable
private fun RateHistory(history: Content.History) = LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
) {
    item {
        RateSummary(history)
        Text(
            text = Strings.CURRENCY_DETAIL_HISTORY,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = AppTheme.spacing.l, bottom = AppTheme.spacing.s),
        )
    }
    items(items = history.rows, key = RateHistoryRow::date) { row ->
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.spacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(row.date, style = MaterialTheme.typography.bodyMedium)
            Text(row.rate, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RateSummary(history: Content.History) = Column(
    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
) {
    Text(Strings.CURRENCY_DETAIL_LATEST, style = MaterialTheme.typography.titleMedium)
    Text(history.latest.rate, style = MaterialTheme.typography.headlineLarge)
    Text(history.latest.date, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.neutral)
    Text(
        text = Strings.CURRENCY_DETAIL_CHANGE,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = AppTheme.spacing.m),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        Text(
            text = history.absoluteChange ?: Strings.CURRENCY_DETAIL_CHANGE_UNAVAILABLE,
            style = MaterialTheme.typography.bodyLarge,
        )
        history.percentChange?.let { percentChange ->
            Text(percentChange, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) = Centered {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Text(Strings.ERROR_TITLE, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) {
            Text(Strings.RETRY)
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) = Box(
    modifier = Modifier.fillMaxSize().padding(horizontal = AppTheme.spacing.m),
    contentAlignment = Alignment.Center,
) {
    content.invoke()
}

@Preview
@Composable
private fun CurrencyDetailUiContentPreview(
    @PreviewParameter(CurrencyDetailUiStatePreviewProvider::class) state: CurrencyDetailUiState,
) = AppTheme {
    CurrencyDetailUiContent(state = state, onBack = {}, onRetry = {})
}
