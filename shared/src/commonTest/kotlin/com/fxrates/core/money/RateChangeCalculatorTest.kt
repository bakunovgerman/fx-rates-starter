package com.fxrates.core.money

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class RateChangeCalculatorTest : FunSpec({

    test("an unchanged rate gives a zero change") {
        val change = RateChangeCalculator.calculate(previous = "0.9".toBigDecimal(), current = "0.9".toBigDecimal())

        change.absolute.isZero() shouldBe true
        change.percent shouldBe "0".toBigDecimal()
    }

    test("the absolute change is the difference between the rates") {
        val change = RateChangeCalculator.calculate(
            previous = "157.25".toBigDecimal(),
            current = "157.9".toBigDecimal(),
        )

        change.absolute shouldBe "0.65".toBigDecimal()
    }

    context("percentage change uses the previous rate") {
        withTests(
            Triple("100", "125", "25"),
            Triple("125", "100", "-20"),
            Triple("3", "4", "33.33333333333333333333333333333333"),
            Triple("3", "5", "66.66666666666666666666666666666667"),
            Triple("3", "1", "-66.66666666666666666666666666666667"),
            Triple("6", "7", "16.66666666666666666666666666666667"),
            Triple("7", "6", "-14.28571428571428571428571428571429"),
            Triple("100", "0", "-100"),
        ) { (previous, current, expected) ->
            val change = RateChangeCalculator.calculate(previous.toBigDecimal(), current.toBigDecimal())

            change.percent shouldBe expected.toBigDecimal()
        }
    }

    test("a zero previous rate keeps the absolute change without a percentage") {
        val change = RateChangeCalculator.calculate(previous = "0".toBigDecimal(), current = "1".toBigDecimal())

        change.absolute shouldBe "1".toBigDecimal()
        change.percent shouldBe null
    }

})
