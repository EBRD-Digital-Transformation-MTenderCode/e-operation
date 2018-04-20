package com.procurement.operation.exception.token

/**
 * The EmptyAuthTokenException is thrown when authorization header is empty.
 */
class EmptyAuthTokenException(message: String) : RuntimeException(message)