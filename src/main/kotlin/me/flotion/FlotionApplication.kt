package me.flotion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class FlotionApplication

fun main(args: Array<String>) {
	runApplication<FlotionApplication>(*args)
}
