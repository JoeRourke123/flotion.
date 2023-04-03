package me.flotion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer


@SpringBootApplication
class FlotionApplication {
	@Bean
	fun configurer(): WebFluxConfigurer = object : WebFluxConfigurer {
		override fun addCorsMappings(registry: CorsRegistry) {
			registry.addMapping("/*").allowedOrigins("http://localhost:3000", "https://flotion.space", "http://joes-macbook-pro:3000")
		}
	}
}

fun main(args: Array<String>) {
	runApplication<FlotionApplication>(*args)
}
