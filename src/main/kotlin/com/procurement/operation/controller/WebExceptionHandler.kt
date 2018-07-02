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
    fun noSuchAuthHeader(e: NoSuchAuthHeaderException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.header.noSuch",
                            description = "The authentication header is missing."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidAuthHeaderTypeException::class])
    fun invalidAuthHeaderType(e: InvalidAuthHeaderTypeException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.header.invalidType",
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
    fun emptyAuthToken(e: EmptyAuthTokenException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(WWW_AUTHENTICATE_HEADER_NAME, BEARER_REALM)
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.token.empty",
                            description = "The authentication token is empty."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidAuthTokenException::class])
    fun invalidAuthToken(e: InvalidAuthTokenException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.token.invalid",
                            description = "Invalid the access token."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidTokenTypeException::class])
    fun invalidTokenType(e: InvalidTokenTypeException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid type of the authentication token.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.token.invalidType",
                            description = "Invalid type of the authentication token."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [MissingPlatformIdException::class])
    fun missingPlatformIdException(e: MissingPlatformIdException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Missing the platform id.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.token.platform.missing",
                            description = "Missing the platform id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidPlatformIdException::class])
    fun invalidPlatformIdException(e: InvalidPlatformIdException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .header(
                WWW_AUTHENTICATE_HEADER_NAME,
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid the platform id.""""
            )
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "auth.token.platform.invalid",
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
    fun missingOperationIdException(e: MissingOperationIdException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "operation.missing",
                            description = "Missing the operation id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [InvalidOperationIdException::class])
    fun invalidOperationIdException(e: InvalidOperationIdException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "operation.invalid",
                            description = "Invalid the operation id."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [UnknownOperationException::class])
    fun unknownOperation(e: UnknownOperationException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "operation.unknown",
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
    fun persistenceException(e: SaveOperationException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "global.internal_server_error",
                            description = "Internal server error."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [ReadOperationException::class])
    fun persistenceException(e: ReadOperationException): ResponseEntity<ErrorRS> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "global.internal_server_error",
                            description = "Internal server error."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [FormsException::class])
    fun remoteService(exception: FormsException): ResponseEntity<*> {
        log.error(exception.message)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .body(
                ErrorRS(
                    errors = listOf(
                        ErrorRS.Error(
                            code = "request.form.invalid",
                            description = "Invalid value of query parameter - 'form'."
                        )
                    )
                )
            )
    }

    @ExceptionHandler(value = [RemoteServiceException::class])
    fun remoteService(exception: RemoteServiceException): ResponseEntity<*> {
        log.error(exception.message)

        return ResponseEntity.status(exception.code)
            .body(exception.payload)
    }
}