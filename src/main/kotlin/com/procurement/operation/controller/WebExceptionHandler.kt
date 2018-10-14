package com.procurement.operation.controller

import com.procurement.operation.exception.FormsException
import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.exception.client.RemoteServiceException
import com.procurement.operation.exception.database.ReadOperationException
import com.procurement.operation.exception.database.SaveOperationException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.EmptyAuthTokenException
import com.procurement.operation.exception.token.InvalidAuthTokenException
import com.procurement.operation.exception.token.InvalidTokenTypeException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.model.BEARER_REALM
import com.procurement.operation.model.CodesOfErrors
import com.procurement.operation.model.WWW_AUTHENTICATE_HEADER_NAME
import com.procurement.operation.model.response.ErrorRS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class WebExceptionHandler : ResponseEntityExceptionHandler() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(WebExceptionHandler::class.java)
    }

    // ***********************
    // * Handlers for header *
    // ***********************
    @ExceptionHandler(value = [NoSuchAuthHeaderException::class])
    fun noSuchAuthHeader(exception: NoSuchAuthHeaderException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_HEADER_NO_SUCH.code,
                            description = "The authentication header is missing."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidAuthHeaderTypeException::class])
    fun invalidAuthHeaderType(exception: InvalidAuthHeaderTypeException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_HEADER_INVALID_TYPE.code,
                            description = "Invalid type of the authentication header. Expected type is 'Bearer'."
                        )
                    )
                )
            )
    }

    // **********************
    // * Handlers for token *
    // **********************
    @ExceptionHandler(value = [EmptyAuthTokenException::class])
    fun emptyAuthToken(exception: EmptyAuthTokenException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_TOKEN_EMPTY.code,
                            description = "The authentication token is empty."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidAuthTokenException::class])
    fun invalidAuthToken(exception: InvalidAuthTokenException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_TOKEN_INVALID.code,
                            description = "Invalid the access token."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidTokenTypeException::class])
    fun invalidTokenType(exception: InvalidTokenTypeException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid type of the authentication token.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_TOKEN_INVALID_TYPE.code,
                            description = "Invalid type of the authentication token."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [MissingPlatformIdException::class])
    fun missingPlatformIdException(exception: MissingPlatformIdException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Missing the platform id.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_TOKEN_PLATFORM_MISSING.code,
                            description = "Missing the platform id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidPlatformIdException::class])
    fun invalidPlatformIdException(exception: InvalidPlatformIdException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid the platform id.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.AUTH_TOKEN_PLATFORM_INVALID.code,
                            description = "Invalid the platform id."
                        )
                    )
                )
            )
    }

    // **************************
    // * Handlers for operation *
    // **************************
    @ExceptionHandler(value = [MissingOperationIdException::class])
    fun missingOperationIdException(exception: MissingOperationIdException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.OPERATION_MISSING.code,
                            description = "Missing the operation id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidOperationIdException::class])
    fun invalidOperationIdException(exception: InvalidOperationIdException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.OPERATION_INVALID.code,
                            description = "Invalid the operation id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [UnknownOperationException::class])
    fun unknownOperation(exception: UnknownOperationException): ResponseEntity<ErrorRS> {
        log.warn(exception.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.OPERATION_NOT_FOUND.code,
                            description = "Unknown the operation."
                        )
                    )
                )
            )
    }

    // *************************
    // * Handlers for database *
    // *************************
    @ExceptionHandler(value = [SaveOperationException::class])
    fun persistenceException(exception: SaveOperationException): ResponseEntity<ErrorRS> {
        log.error(exception.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.INTERNAL_SERVER_ERROR.code,
                            description = "Internal server error."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [ReadOperationException::class])
    fun persistenceException(exception: ReadOperationException): ResponseEntity<ErrorRS> {
        log.error(exception.message, exception)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.INTERNAL_SERVER_ERROR.code,
                            description = "Internal server error."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [FormsException::class])
    fun formsException(exception: FormsException): ResponseEntity<*> {
        log.error(exception.message, exception)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = CodesOfErrors.REQUEST_FORM_INVALID.code,
                            description = "Invalid value of query parameter - 'form'."
                        )
                    )
                )
            )
    }

    // *******************************
    // * Handlers for remote service *
    // *******************************
    @ExceptionHandler(value = [RemoteServiceException::class])
    fun remoteService(exception: RemoteServiceException): ResponseEntity<*> {
        log.error(exception.message)

        return ResponseEntity.status(exception.code ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .body(exception.payload)
    }
}