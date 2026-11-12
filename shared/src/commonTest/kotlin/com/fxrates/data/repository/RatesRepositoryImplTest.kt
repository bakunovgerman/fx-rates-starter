package com.fxrates.data.repository

import com.fxrates.data.remote.FrankfurterApi
import com.fxrates.data.remote.FrankfurterBaseUrl
import com.fxrates.domain.model.RateObservation
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.encodedPath
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

class RatesRepositoryImplTest : FunSpec({

    test("history makes one request for the selected base across 30 inclusive UTC calendar dates") {
        val engine = MockEngine {
            respond(
                content = """
                    {
                        "base": "CHF",
                        "start_date": "2026-10-14",
                        "end_date": "2026-11-12",
                        "rates": {
                            "2026-11-12": { "JPY": 157.9 },
                            "2026-10-14": { "EUR": 0.87 },
                            "2026-11-11": { "EUR": 0.88, "JPY": 158.2 }
                        }
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(engine) {
            defaultRequest {
                url(FrankfurterBaseUrl)
            }
            install(ContentNegotiation) {
                json()
            }
        }
        var clockReads = 0
        val clock = object : Clock {

            override fun now(): Instant {
                clockReads += 1
                return Instant.parse("2026-11-12T23:59:59Z")
            }

        }
        val repository = RatesRepositoryImpl(api = FrankfurterApi(client), converter = RatesConverter(), clock = clock)

        try {
            val history = repository.getRateHistory(base = "CHF", code = "EUR")

            clockReads shouldBe 1
            val request = engine.requestHistory.single()
            request.method shouldBe HttpMethod.Get
            request.url.encodedPath shouldBe "/v1/2026-10-14..2026-11-12"
            request.url.parameters["base"] shouldBe "CHF"
            history shouldBe listOf(
                RateObservation(LocalDate(2026, 11, 11), "0.88".toBigDecimal()),
                RateObservation(LocalDate(2026, 10, 14), "0.87".toBigDecimal()),
            )
        } finally {
            client.close()
            engine.close()
        }
    }

})
