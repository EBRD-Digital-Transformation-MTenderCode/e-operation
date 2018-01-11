package com.procurement.operation.helper

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.procurement.operation.model.ACCESS_TOKEN_TYPE
import com.procurement.operation.model.CLAIM_NAME_PLATFORM_ID
import com.procurement.operation.model.HEADER_NAME_TOKEN_TYPE
import com.procurement.operation.model.REFRESH_TOKEN_TYPE
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

fun genAccessToken(platformId: UUID, expiresOn: Date, algorithm: Algorithm): String =
    genToken(
        claims = mapOf<String, Any>(CLAIM_NAME_PLATFORM_ID to platformId),
        header = mapOf<String, Any>(HEADER_NAME_TOKEN_TYPE to ACCESS_TOKEN_TYPE),
        expiresOn = expiresOn,
        algorithm = algorithm
    )

fun genRefreshToken(platformId: UUID, expiresOn: Date, algorithm: Algorithm): String =
    genToken(
        claims = mapOf<String, Any>(CLAIM_NAME_PLATFORM_ID to platformId),
        header = mapOf<String, Any>(HEADER_NAME_TOKEN_TYPE to REFRESH_TOKEN_TYPE),
        expiresOn = expiresOn,
        algorithm = algorithm
    )

fun genToken(claims: Map<String, Any>, header: Map<String, Any>, expiresOn: Date, algorithm: Algorithm): String =
    JWT.create()
        .also {
            claims.forEach { key, value ->
                when (value) {
                    is Date -> it.withClaim(key, value)
                    is Boolean -> it.withClaim(key, value)
                    is Int -> it.withClaim(key, value)
                    is Long -> it.withClaim(key, value)
                    is Double -> it.withClaim(key, value)
                    is String -> it.withClaim(key, value)
                    else -> it.withClaim(key, value.toString())
                }
            }
        }
        .withHeader(header)
        .withExpiresAt(expiresOn)
        .sign(algorithm)

fun LocalDateTime.genExpiresOn(lifeTime: Long): Date =
    Date.from(this.genExpiredZonedDateTime(lifeTime).toInstant())

private fun LocalDateTime.genExpiredZonedDateTime(expired: Long): ZonedDateTime =
    ZonedDateTime.of(this.plusSeconds(expired), ZoneId.systemDefault())