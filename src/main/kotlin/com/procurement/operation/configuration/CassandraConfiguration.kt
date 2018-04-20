package com.procurement.operation.configuration

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.QueryOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.SocketOptions
import com.procurement.operation.configuration.properties.CassandraProperties
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.cassandra.ClusterBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils

@Configuration
@EnableConfigurationProperties(
    value = [
        CassandraProperties::class
    ]
)
class CassandraConfiguration(
    private val cassandraProperties: CassandraProperties,
    builderCustomizers: ObjectProvider<List<ClusterBuilderCustomizer>>
) {
    private val builderCustomizers = builderCustomizers.ifAvailable

    @Bean
    fun session(): Session =
        cassandraProperties.keyspaceName?.let { cassandraCluster().connect(it) }
            ?: cassandraCluster().connect()

    private fun cassandraCluster(): Cluster {
        val builder = Cluster.builder()

        cassandraProperties.clusterName?.also {
            builder.withClusterName(cassandraProperties.clusterName)
        }

        builder.withPort(cassandraProperties.port)

        cassandraProperties.username?.also {
            builder.withCredentials(cassandraProperties.username, cassandraProperties.password)
        }

        builder.withCompression(cassandraProperties.compression)

        cassandraProperties.loadBalancingPolicy?.also {
            val policy = instantiate(it)
            builder.withLoadBalancingPolicy(policy)
        }

        builder.withQueryOptions(getQueryOptions())

        cassandraProperties.reconnectionPolicy?.also {
            val policy = instantiate(it)
            builder.withReconnectionPolicy(policy)
        }

        cassandraProperties.retryPolicy?.also {
            val policy = instantiate(it)
            builder.withRetryPolicy(policy)
        }

        builder.withSocketOptions(getSocketOptions())

        if (cassandraProperties.isSsl) {
            builder.withSSL()
        }

        builder.withPoolingOptions(getPoolingOptions())

        val points = cassandraProperties.contactPoints
        builder.addContactPoints(*StringUtils.commaDelimitedListToStringArray(points))

        builder.withProtocolVersion(ProtocolVersion.NEWEST_SUPPORTED)

        customize(builder)
        return builder.build()
    }

    private fun customize(builder: Cluster.Builder) {
        builderCustomizers?.forEach { it.customize(builder) }
    }

    private fun <T> instantiate(type: Class<out T>): T {
        return BeanUtils.instantiateClass(type)
    }

    private fun getQueryOptions(): QueryOptions = QueryOptions()
        .also { options ->
            cassandraProperties.consistencyLevel?.also { options.consistencyLevel = it }
            cassandraProperties.serialConsistencyLevel?.also { options.serialConsistencyLevel = it }
            options.fetchSize = cassandraProperties.fetchSize
        }

    private fun getSocketOptions(): SocketOptions = SocketOptions()
        .also { options ->
            cassandraProperties.connectTimeout?.also { options.connectTimeoutMillis = it.toMillis().toInt() }
            cassandraProperties.readTimeout?.also { options.readTimeoutMillis = it.toMillis().toInt() }
        }

    private fun getPoolingOptions(): PoolingOptions = PoolingOptions()
        .also { options ->
            val pool = cassandraProperties.pool
            options.idleTimeoutSeconds = pool.idleTimeout.seconds.toInt()
            options.poolTimeoutMillis = pool.poolTimeout.toMillis().toInt()
            options.heartbeatIntervalSeconds = pool.heartbeatInterval.seconds.toInt()
            options.maxQueueSize = pool.maxQueueSize
        }
}