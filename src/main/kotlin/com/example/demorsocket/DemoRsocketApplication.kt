package com.example.demorsocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
class DemoRsocketApplication

fun main(args: Array<String>) {
    runApplication<DemoRsocketApplication>(*args)
}

@Controller
class RSocketController(val priceService: PriceService) {

    @MessageMapping("stockPrices")
    fun prices(symbol: String) = priceService.generatePrices(symbol)
}

@Service
class PriceService {
    private val prices = ConcurrentHashMap<String, Flux<String>>()

    fun generatePrices(symbol: String): Flux<String> {
        return prices.computeIfAbsent(symbol) {
            Flux
                    .interval(Duration.ofMillis(500))
                    .map { symbol + randomStockPrice() + now() }
                    .share()
        }
    }

    private fun randomStockPrice(): Double {
        return ThreadLocalRandom.current().nextDouble(100.0)
    }
}

@Configuration
@EnableRSocketSecurity
class HelloRSocketSecurityConfig {

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("Test")
                .build()
        return MapReactiveUserDetailsService(user)
    }
}

// TODO json decode error from client
data class StockPrice(val symbol: String,
                      val price: Double,
                      val time: LocalDateTime)
