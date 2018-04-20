package com.procurement.operation.service

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.token.InvalidAuthTokenException
import com.procurement.operation.exception.token.InvalidTokenTypeException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.logging.MDCKey
import com.procurement.operation.logging.mdc
import com.procurement.operation.model.ACCESS_TOKEN_TYPE
import com.procurement.operation.model.CLAIM_NAME_PLATFORM_ID
import com.procurement.operation.model.HEADER_NAME_TOKEN_TYPE
import java.util.*

interface JwtService {
    fun toJWT(token: String): DecodedJWT

    fun getPlatformId(jwt: DecodedJWT): UUID
}

class JwtServiceImpl : JwtService {
    override fun toJWT(token: String): DecodedJWT = decode(token).also { it.checkTypeToken() }

    override fun getPlatformId(jwt: DecodedJWT): UUID {
        val platformIdClaim = getPlatformIdClaim(jwt)
        mdc(MDCKey.PLATFORM_ID, platformIdClaim.asString())
        return toUUID(platformIdClaim)
    }

    private fun decode(token: String) = try {
        JWT.decode(token)
    } catch (ex: JWTDecodeException) {
        throw InvalidAuthTokenException("Invalid the auth token.")
    }

    private fun DecodedJWT.checkTypeToken() {
        val tokenType = this.getHeaderClaim(HEADER_NAME_TOKEN_TYPE)
        if (tokenType.isNull || tokenType.asString() != ACCESS_TOKEN_TYPE) {
            throw InvalidTokenTypeException("Invalid type of the auth token.")
        }
    }

    private fun getPlatformIdClaim(jwt: DecodedJWT): Claim {
        val platformIdClaim = jwt.getClaim(CLAIM_NAME_PLATFORM_ID)
        if (platformIdClaim.isNull) {
            throw MissingPlatformIdException(message = "Missing the platform id.")
        }
        return platformIdClaim
    }

    private fun toUUID(claim: Claim) = try {
        UUID.fromString(claim.asString())
    } catch (ex: Exception) {
        throw InvalidPlatformIdException(message = "Invalid the platform id.", cause = ex)
    }
}