package com.procurement.operation.helper

import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.EmptyAuthTokenException
import com.procurement.operation.logging.MDCKey
import com.procurement.operation.logging.mdc
import com.procurement.operation.model.AUTHORIZATION_PREFIX_BEARER
import java.util.*

fun getBearerTokenByAuthHeader(authorizationHeader: String): String {
    checkAuthHeader(authorizationHeader)
    if (!authorizationHeader.startsWith(AUTHORIZATION_PREFIX_BEARER)) {
        throw InvalidAuthHeaderTypeException(
            "Invalid type the authentication header. Requires 'Bearer' type of the authentication header."
        )
    }
    return getToken(authorizationHeader, AUTHORIZATION_PREFIX_BEARER)
}

fun extractOperationId(operationHeader: String): UUID {
    if (operationHeader.isEmpty())
        throw MissingOperationIdException(message = "Missing the operation id")
    mdc(MDCKey.OPERATION_ID, operationHeader)
    return toUUID(operationHeader)
}

private fun checkAuthHeader(authorizationHeader: String) {
    if (authorizationHeader.isEmpty())
        throw NoSuchAuthHeaderException("There is not the authentication header.")
}

private fun getToken(header: String, headerType: String): String {
    val token = header.substring(headerType.length).trim()
    if (token.isEmpty()) {
        throw EmptyAuthTokenException("The authentication token is empty.")
    }
    return token
}

private fun toUUID(text: String) = try {
    UUID.fromString(text)
} catch (ex: Exception) {
    throw InvalidOperationIdException(message = "Invalid operation id.", cause = ex)
}
