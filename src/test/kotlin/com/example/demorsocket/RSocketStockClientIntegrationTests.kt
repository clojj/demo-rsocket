package com.example.demorsocket

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.connectTcpAndAwait
import org.springframework.security.rsocket.metadata.BasicAuthenticationEncoder
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import reactor.core.publisher.Flux
import reactor.test.StepVerifier


@SpringBootTest
class RSocketStockClientIntegrationTests {

    @Test
    fun shouldRetrieveStockPricesFromTheService() {
        // given
        val rSocketStockClient = RSocketStockClient()

        // when
        val prices: Flux<String> = rSocketStockClient.pricesFor("SYMBOL")

        // then
        // then
        StepVerifier.create(prices.take(3))
                .expectNextMatches { symbol -> symbol.contains("SYMBOL") }
                .expectNextMatches { symbol -> symbol.contains("SYMBOL") }
                .expectNextMatches { symbol -> symbol.contains("SYMBOL") }
                .verifyComplete()
    }
}

class RSocketStockClient() {

    private val requester = runBlocking {
        val credentials = UsernamePasswordMetadata("user", "password")
        RSocketRequester.builder()
                .rsocketStrategies { builder -> builder.encoder(BasicAuthenticationEncoder()) }
                .setupMetadata(credentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .connectTcpAndAwait("localhost", 7000)
    }

    fun pricesFor(symbol: String): Flux<String> {
        return requester
                .route("stockPrices")
                .data(symbol)
                .retrieveFlux(String::class.java)
    }
}
