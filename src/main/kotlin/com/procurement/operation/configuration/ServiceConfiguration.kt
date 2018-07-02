package com.procurement.operation.configuration

import com.procurement.operation.dao.OperationDao
import com.procurement.operation.service.FormsService
import com.procurement.operation.service.FormsServiceImpl
import com.procurement.operation.service.JwtService
import com.procurement.operation.service.JwtServiceImpl
import com.procurement.operation.service.OperationService
import com.procurement.operation.service.OperationServiceImpl
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ServiceConfiguration(private val operationDao: OperationDao) {
    @Bean
    fun jwtService(): JwtService = JwtServiceImpl()

    @Bean
    fun operationService(): OperationService =
        OperationServiceImpl(jwtService = jwtService(), operationDao = operationDao)

    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(): WebClient.Builder = WebClient.builder()

    @Bean
    fun formsService(): FormsService =
        FormsServiceImpl(webClientBuilder = loadBalancedWebClientBuilder())
}
