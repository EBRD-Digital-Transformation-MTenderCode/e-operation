package com.procurement.operation.model

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import javax.servlet.http.HttpServletRequest

typealias JWToken = String



data class RequestContext(val request: HttpServletRequest) {

    fun getAccessJWT(): DecodedJWT = getBearerToken().asJWT()

    private fun getBearerToken(): JWToken {
        val header = request.getHeader(HEADER_NAME_AUTHORIZATION)
            ?: throw NoSuchAuthHeaderException("There is no 'Bearer' authentication header.", this)
        if (!header.startsWith(AUTHORIZATION_PREFIX_BEARER)) {
            throw InvalidAuthHeaderTypeException("Invalid authentication type, requires a 'Bearer' authentication type.", this)
        }
        return header.substring(AUTHORIZATION_PREFIX_BEARER.length)
    }

    private fun JWToken.asJWT() = try {
        JWT.decode(this).also {
            it.check()
        }
    } catch (ex: JWTDecodeException) {
        throw InvalidBearerTokenException("Invalid authentication type, requires a 'Bearer' authentication type.", this@RequestContext)
    }

    private fun DecodedJWT.check() {
        val tokenType = this.getHeaderClaim(HEADER_NAME_TOKEN_TYPE)
        if (tokenType.isNull || tokenType.asString() != ACCESS_TOKEN_TYPE) {
            throw BearerTokenWrongTypeException("The bearer token of wrong type.", this@RequestContext)
        }
    }
}
