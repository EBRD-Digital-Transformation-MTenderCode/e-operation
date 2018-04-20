package com.procurement.operation.configuration

import com.procurement.operation.dao.OperationDao
import com.procurement.operation.service.JwtService
import com.procurement.operation.service.JwtServiceImpl
import com.procurement.operation.service.OperationService
import com.procurement.operation.service.OperationServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration(private val operationDao: OperationDao) {
    @Bean
    fun jwtService(): JwtService = JwtServiceImpl()

    @Bean
    fun operationService(): OperationService =
        OperationServiceImpl(jwtService = jwtService(), operationDao = operationDao)
}
