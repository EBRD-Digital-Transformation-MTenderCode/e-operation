package com.procurement.operation.controller

import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.database.ReadException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.model.BEARER_REALM
import com.procurement.operation.model.HEADER_NAME_WWW_AUTHENTICATE
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

    @ExceptionHandler(value = [NoSuchAuthHeaderException::class])
    fun noSuchAuthHeaderException(e: NoSuchAuthHeaderException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(HEADER_NAME_WWW_AUTHENTICATE, BEARER_REALM)
            .build<Any>()
    }

    @ExceptionHandler(value = [InvalidAuthHeaderTypeException::class])
    fun invalidAuthHeaderTypeException(e: InvalidAuthHeaderTypeException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(HEADER_NAME_WWW_AUTHENTICATE, BEARER_REALM)
            .build<Any>()
    }

    @ExceptionHandler(value = [InvalidBearerTokenException::class])
    fun invalidBearerTokenException(e: InvalidBearerTokenException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                HEADER_NAME_WWW_AUTHENTICATE,
                """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid""""
            )
            .build<Any>()
    }

    @ExceptionHandler(value = [BearerTokenWrongTypeException::class])
    fun bearerTokenWrongTypeException(e: BearerTokenWrongTypeException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
            .header(
                HEADER_NAME_WWW_AUTHENTICATE,
                """$BEARER_REALM, error_code="invalid_token", error_message="The token of wrong type""""
            )
            .build<Any>()
    }

    @ExceptionHandler(value = [MissingPlatformIdException::class])
    fun missingPlatformIdException(e: MissingPlatformIdException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
            .header(
                HEADER_NAME_WWW_AUTHENTICATE,
                """$BEARER_REALM, error_code="invalid_request", error_message="Missing platform id""""
            )
            .build<Any>()
    }

    @ExceptionHandler(value = [MissingOperationIdException::class])
    fun missingOperationIdException(e: MissingOperationIdException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build<Any>()
    }

    @ExceptionHandler(value = [InvalidPlatformIdException::class])
    fun invalidPlatformIdException(e: InvalidPlatformIdException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build<Any>()
    }

    @ExceptionHandler(value = [InvalidOperationIdException::class])
    fun invalidOperationIdException(e: InvalidOperationIdException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build<Any>()
    }


    @ExceptionHandler(value = [OperationIdNotFoundException::class])
    fun operationIdNotFoundException(e: OperationIdNotFoundException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build<Any>()
    }

    @ExceptionHandler(value = [PersistenceException::class])
    fun persistenceException(e: PersistenceException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build<Any>()
    }

    @ExceptionHandler(value = [ReadException::class])
    fun persistenceException(e: ReadException): ResponseEntity<*> {
        log.warn(e.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build<Any>()
    }
}