package com.fxrates.core.money

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * The change between two rates of the same currency pair.
 *
 * @property absolute `current - previous`, in units of the quote currency.
 * @property percent the change relative to the previous rate, or `null` when it is zero.
 * A value of `1.5` means +1.5%.
 */
data class RateChange(
    val absolute: BigDecimal,
    val percent: BigDecimal?,
)

object RateChangeCalculator {

    private val hundred = BigDecimal.fromInt(100)
    private val percentMode = DecimalMode(
        decimalPrecision = 34,
        roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
    )
    private val divisionMode = DecimalMode(
        decimalPrecision = percentMode.decimalPrecision + 1,
        roundingMode = RoundingMode.TOWARDS_ZERO,
    )

    fun calculate(previous: BigDecimal, current: BigDecimal): RateChange {
        val absolute = current - previous
        val percent = if (previous.isZero()) {
            null
        } else {
            // bignum 0.3.10 can misround recurring division. Truncate with one guard digit, then round.
            (absolute * hundred).divide(previous, divisionMode).roundSignificand(percentMode)
        }
        return RateChange(absolute = absolute, percent = percent)
    }

}
