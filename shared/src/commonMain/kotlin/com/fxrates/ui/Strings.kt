package com.fxrates.ui

/**
 * Every user-facing string of the app. The app is English-only, so a constant is enough; a
 * string is never written inline in a composable.
 */
object Strings {

    const val RATES_TITLE = "Rates"
    const val RATES_SEARCH_HINT = "Search by code or name"
    const val RATES_NOTHING_FOUND = "Nothing found"
    const val ERROR_TITLE = "Could not load the rates"
    const val RETRY = "Retry"
    const val REFRESH = "Refresh"
    const val BACK = "Back"
    const val CURRENCY_DETAIL_LATEST = "Latest rate"
    const val CURRENCY_DETAIL_CHANGE = "30-day change"
    const val CURRENCY_DETAIL_CHANGE_UNAVAILABLE = "Change unavailable"
    const val CURRENCY_DETAIL_HISTORY = "Rate history"
    const val CURRENCY_DETAIL_NO_RATES = "No rates available for this period"

    fun ratesSubtitle(base: String, date: String): String = "1 $base · $date"

    fun currencyPair(base: String, code: String): String = "$base / $code"

}
