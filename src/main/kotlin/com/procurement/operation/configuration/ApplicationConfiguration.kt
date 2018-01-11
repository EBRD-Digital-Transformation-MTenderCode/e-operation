package com.procurement.operation.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    value = [
        WebConfiguration::class,
        ServiceConfiguration::class,
        CassandraConfiguration::class
    ]
)
class ApplicationConfiguration
