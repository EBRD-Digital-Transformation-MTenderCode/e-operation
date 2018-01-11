package com.procurement.operation.configuration

import com.procurement.operation.dao.OperationDao
import com.procurement.operation.dao.OperationDaoImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(CassandraConfiguration::class)
class DaoConfiguration(private val cassandraConfiguration: CassandraConfiguration) {
    @Bean
    fun operationDao(): OperationDao =
        OperationDaoImpl(cassandraConfiguration.session())
}