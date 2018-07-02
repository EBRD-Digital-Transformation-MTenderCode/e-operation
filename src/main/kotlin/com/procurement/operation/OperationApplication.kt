package com.procurement.operation

import com.procurement.operation.configuration.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication(
    scanBasePackageClasses = [
        ApplicationConfiguration::class
    ]
)
@EnableEurekaClient
class OperationApplication

fun main(args: Array<String>) {
    runApplication<OperationApplication>(*args)
}
