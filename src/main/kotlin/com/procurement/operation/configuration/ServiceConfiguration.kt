package com.procurement.operation.configuration

import com.procurement.operation.dao.OperationDao
import com.procurement.operation.service.OperationService
import com.procurement.operation.service.OperationServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration(private val operationDao: OperationDao) {
    @Bean
    fun operationService(): OperationService = OperationServiceImpl(operationDao)
}
