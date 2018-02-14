package com.procurement.operation.helper

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import com.procurement.operation.logging.MDCKey
import com.procurement.operation.model.*
import java.util.*
import javax.servlet.http.HttpServletRequest

typealias JWToken = String

fun HttpServletRequest.extractAccessJWT(): DecodedJWT = getBearerToken(this).asJWT()

private fun getBearerToken(request: HttpServletRequest): JWToken {
    val header = request.getHeader(HEADER_NAME_AUTHORIZATION)
        ?: throw NoSuchAuthHeaderException("There is no 'Bearer' authentication header.")
    if (!header.startsWith(AUTHORIZATION_PREFIX_BEARER)) {
        throw InvalidAuthHeaderTypeException("Invalid authentication type, requires a 'Bearer' authentication type.")
    }
    return header.substring(AUTHORIZATION_PREFIX_BEARER.length)
}

private fun JWToken.asJWT() = try {
    JWT.decode(this).also { it.check() }
} catch (ex: JWTDecodeException) {
    throw InvalidBearerTokenException("Invalid authentication type, requires a 'Bearer' authentication type.")
}

private fun DecodedJWT.check() {
    val tokenType = this.getHeaderClaim(HEADER_NAME_TOKEN_TYPE)
    if (tokenType.isNull || tokenType.asString() != ACCESS_TOKEN_TYPE) {
        throw BearerTokenWrongTypeException("The bearer token of wrong type.")
    }
}

fun HttpServletRequest.extractOperationId(): UUID =
    this.getHeader(HEADER_NAME_OPERATION_ID)?.let {
        MDCKey.OPERATION_ID.mapping(it)
        try {
            UUID.fromString(it)
        } catch (ex: Exception) {
            throw InvalidOperationIdException(message = "Invalid operation id.", cause = ex)
        }
    } ?: throw MissingOperationIdException(message = "Missing operation id.")