package com.procurement.operation.infrastructure.metric

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

class CassandraHealthIndicator(
    private val session: Session
) : AbstractHealthIndicator() {
    companion object {
        private const val HealthCQL = "SELECT release_version FROM system.local;"
    }

    private val preparedHealthCQL = session.prepare(HealthCQL).bind()

    override fun doHealthCheck(builder: Health.Builder) {
        try {
            val results: ResultSet = session.execute(preparedHealthCQL)
            if (results.isExhausted) {
                builder.up()
            } else {
                val version = results.one().getString(0)
                builder.up().withDetail("version", version)
            }
        } catch (exception: Exception) {
            builder.down().withDetail("Cassandra health check error", exception.message)
        }
    }
}