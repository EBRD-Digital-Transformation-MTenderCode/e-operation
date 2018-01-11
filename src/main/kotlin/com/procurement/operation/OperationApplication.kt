package com.procurement.operation

import com.procurement.operation.configuration.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackageClasses = [
        ApplicationConfiguration::class
    ]
)
class OperationApplication

fun main(args: Array<String>) {
    runApplication<OperationApplication>(*args)
}
