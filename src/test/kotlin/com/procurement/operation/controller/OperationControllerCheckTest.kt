package com.procurement.operation.controller

import com.auth0.jwt.algorithms.Algorithm
import com.nhaarman.mockito_kotlin.*
import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.model.BEARER_REALM
import com.procurement.operation.model.HEADER_NAME_WWW_AUTHENTICATE
import com.procurement.operation.model.RequestContext
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import com.procurement.operation.service.OperationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import javax.servlet.http.HttpServletRequest

class OperationControllerCheckTest {
    companion object {
        private const val URL_CHECK_OPERATION_ID = "/operation/check"
    }

    private val algorithm: Algorithm
    private lateinit var mockMvc: MockMvc
    private lateinit var operationService: OperationService

    private val httpServletRequest: HttpServletRequest
        get() {
            val request = MockHttpServletRequest()
            request.remoteAddr = "127.0.0.1"
            request.remoteHost = "localhost"
            return request
        }

    init {
        val rsaKeyPair = RSAKeyGenerator().generate(2048)
        val rsaService = RSAServiceImpl(keyFactoryService = KeyFactoryServiceImpl())
        algorithm = Algorithm.RSA256(rsaService.toPublicKey(rsaKeyPair.publicKey),
                                     rsaService.toPrivateKey(rsaKeyPair.privateKey)
        )
    }

    @BeforeEach
    fun setUp() {
        operationService = mock()

        val controller = OperationController(operationService = operationService)
        val exceptionHandler = WebExceptionHandler()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionHandler)
            .build()
    }

    @Test
    @DisplayName("checkOperationId - OK")
    fun checkOperationId() {
        doNothing().whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("checkOperationId - no valid")
    fun checkOperationId1() {
        doThrow(OperationIdNotFoundException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("checkOperationId - NoSuchAuthHeaderException")
    fun checkOperationId2() {
        doThrow(NoSuchAuthHeaderException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(HEADER_NAME_WWW_AUTHENTICATE,
                            BEARER_REALM
                    )
            )
    }

    @Test
    @DisplayName("checkOperationId - InvalidAuthHeaderTypeException")
    fun checkOperationId3() {
        doThrow(InvalidAuthHeaderTypeException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(HEADER_NAME_WWW_AUTHENTICATE,
                            BEARER_REALM
                    )
            )
    }

    @Test
    @DisplayName("checkOperationId - InvalidBearerTokenException")
    fun checkOperationId4() {
        doThrow(InvalidBearerTokenException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(HEADER_NAME_WWW_AUTHENTICATE,
                            """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid""""
                    )
            )
    }

    @Test
    @DisplayName("checkOperationId - BearerTokenWrongTypeException")
    fun checkOperationId5() {
        doThrow(BearerTokenWrongTypeException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(HEADER_NAME_WWW_AUTHENTICATE,
                            """$BEARER_REALM, error_code="invalid_token", error_message="The token of wrong type""""
                    )
            )
    }

    @Test
    @DisplayName("checkOperationId - MissingPlatformIdException")
    fun checkOperationId6() {
        doThrow(MissingPlatformIdException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isBadRequest)
            .andExpect(
                header()
                    .string(HEADER_NAME_WWW_AUTHENTICATE,
                            """$BEARER_REALM, error_code="invalid_request", error_message="Missing platform id""""
                    )
            )
    }

    @Test
    @DisplayName("checkOperationId - MissingOperationIdException")
    fun checkOperationId7() {
        doThrow(MissingOperationIdException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("checkOperationId - OperationIdNotFoundException")
    fun checkOperationId8() {
        doThrow(OperationIdNotFoundException(RequestContext(request = httpServletRequest)))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("checkOperationId - PersistenceException")
    fun checkOperationId9() {
        doThrow(PersistenceException(RequestContext(request = httpServletRequest), cause = Exception()))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("checkOperationId - InvalidOperationIdException")
    fun checkOperationId10() {
        doThrow(InvalidOperationIdException(RequestContext(request = httpServletRequest), ex = Exception()))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("checkOperationId - InvalidPlatformIdException")
    fun checkOperationId11() {
        doThrow(InvalidPlatformIdException(RequestContext(request = httpServletRequest), ex = Exception()))
            .whenever(operationService)
            .checkOperationTx(any())

        mockMvc.perform(head(URL_CHECK_OPERATION_ID))
            .andExpect(status().isBadRequest)
    }
}