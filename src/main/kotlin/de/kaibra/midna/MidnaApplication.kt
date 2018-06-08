package de.kaibra.midna

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MidnaApplication

fun main(args: Array<String>) {
    runApplication<MidnaApplication>(*args)
}
