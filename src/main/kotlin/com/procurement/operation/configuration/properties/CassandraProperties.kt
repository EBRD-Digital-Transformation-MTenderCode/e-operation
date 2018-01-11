package com.procurement.operation.configuration.properties

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.ProtocolOptions
import com.datastax.driver.core.QueryOptions
import com.datastax.driver.core.policies.LoadBalancingPolicy
import com.datastax.driver.core.policies.ReconnectionPolicy
import com.datastax.driver.core.policies.RetryPolicy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.convert.DefaultDurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

@ConfigurationProperties(prefix = "cassandra")
class CassandraProperties {

    /**
     * Keyspace name to use.
     */
    var keyspaceName: String? = null

    /**
     * Name of the Cassandra cluster.
     */
    var clusterName: String? = null

    /**
     * Comma-separated list of cluster node addresses.
     */
    var contactPoints = "localhost"

    /**
     * Port of the Cassandra server.
     */
    var port = ProtocolOptions.DEFAULT_PORT

    /**
     * Login user of the server.
     */
    var username: String? = null

    /**
     * Login password of the server.
     */
    var password: String? = null

    /**
     * Compression supported by the Cassandra binary protocol.
     */
    var compression = ProtocolOptions.Compression.NONE

    /**
     * Class name of the load balancing policy.
     */
    var loadBalancingPolicy: Class<out LoadBalancingPolicy>? = null

    /**
     * Queries consistency level.
     */
    var consistencyLevel: ConsistencyLevel? = null

    /**
     * Queries serial consistency level.
     */
    var serialConsistencyLevel: ConsistencyLevel? = null

    /**
     * Queries default fetch size.
     */
    var fetchSize = QueryOptions.DEFAULT_FETCH_SIZE

    /**
     * Reconnection policy class.
     */
    var reconnectionPolicy: Class<out ReconnectionPolicy>? = null

    /**
     * Class name of the retry policy.
     */
    var retryPolicy: Class<out RetryPolicy>? = null

    /**
     * Socket option: connection time out.
     */
    var connectTimeout: Duration? = null

    /**
     * Socket option: read time out.
     */
    var readTimeout: Duration? = null

    /**
     * Enable SSL support.
     */
    var isSsl = false

    /**
     * Pool configuration.
     */
    val pool = Pool()

    /**
     * Pool properties.
     */
    class Pool {

        /**
         * Idle timeout before an idle connection is removed. If a duration suffix is not
         * specified, seconds will be used.
         */
        @DefaultDurationUnit(ChronoUnit.SECONDS)
        var idleTimeout: Duration = Duration.ofSeconds(120)

        /**
         * Pool timeout when trying to acquire a connection from a host's pool.
         */
        var poolTimeout: Duration = Duration.ofMillis(5000)

        /**
         * Heartbeat interval after which a message is sent on an idle connection to make
         * sure it's still alive. If a duration suffix is not specified, seconds will be
         * used.
         */
        @DefaultDurationUnit(ChronoUnit.SECONDS)
        var heartbeatInterval: Duration = Duration.ofSeconds(30)

        /**
         * Maximum number of requests that get queued if no connection is available.
         */
        var maxQueueSize = 256
    }
}