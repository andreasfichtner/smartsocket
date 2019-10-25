package de.retterdesapok.smartSocketServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmartSocketServerApplication

fun main(args: Array<String>) {
	runApplication<SmartSocketServerApplication>(*args)
}
