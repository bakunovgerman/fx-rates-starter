package com.fxrates.domain.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.LocalDate

data class RateObservation(
    val date: LocalDate,
    val rate: BigDecimal,
)
