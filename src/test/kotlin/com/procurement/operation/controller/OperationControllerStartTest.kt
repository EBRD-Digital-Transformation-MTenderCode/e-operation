package com.procurement.operation.controller

import com.auth0.jwt.algorithms.Algorithm
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.helper.JWToken
import com.procurement.operation.helper.genAccessToken
import com.procurement.operation.helper.genExpiresOn
import com.procurement.operation.model.BEARER_REALM
import com.procurement.operation.model.HEADER_NAME_AUTHORIZATION
import com.procurement.operation.model.HEADER_NAME_OPERATION_ID
import com.procurement.operation.model.HEADER_NAME_WWW_AUTHENTICATE
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import com.procurement.operation.service.OperationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest

class OperationControllerStartTest {
    companion object {
        private const val URL_START_OPERATION_ID = "/operation/start"
        private val PLATFORM_ID = UUID.randomUUID()
        private val OPERATION_ID = UUID.randomUUID()
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
        algorithm = Algorithm.RSA256(
            rsaService.toPublicKey(rsaKeyPair.publicKey),
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
    @DisplayName("startOperation - OK")
    fun startOperation() {
        whenever(operationService.getOperationId(any()))
            .thenReturn(OPERATION_ID)

        mockMvc.perform(post(URL_START_OPERATION_ID).header(HEADER_NAME_AUTHORIZATION, genAccessJWT()))
            .andExpect(status().isOk)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_OPERATION_ID,
                        OPERATION_ID.toString()
                    )
            )
    }

    @Test
    @DisplayName("startOperation - NoSuchAuthHeaderException")
    fun startOperation2() {
        doThrow(NoSuchAuthHeaderException(message = ""))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_WWW_AUTHENTICATE,
                        BEARER_REALM
                    )
            )
    }

    @Test
    @DisplayName("startOperation - InvalidAuthHeaderTypeException")
    fun startOperation3() {
        doThrow(InvalidAuthHeaderTypeException(message = ""))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_WWW_AUTHENTICATE,
                        BEARER_REALM
                    )
            )
    }

    @Test
    @DisplayName("startOperation - InvalidBearerTokenException")
    fun startOperation4() {
        doThrow(InvalidBearerTokenException(message = ""))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_WWW_AUTHENTICATE,
                        """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid""""
                    )
            )
    }

    @Test
    @DisplayName("startOperation - BearerTokenWrongTypeException")
    fun startOperation5() {
        doThrow(BearerTokenWrongTypeException(message = ""))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isUnauthorized)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_WWW_AUTHENTICATE,
                        """$BEARER_REALM, error_code="invalid_token", error_message="The token of wrong type""""
                    )
            )
    }

    @Test
    @DisplayName("startOperation - MissingPlatformIdException")
    fun startOperation6() {
        doThrow(MissingPlatformIdException(message = ""))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isBadRequest)
            .andExpect(
                header()
                    .string(
                        HEADER_NAME_WWW_AUTHENTICATE,
                        """$BEARER_REALM, error_code="invalid_request", error_message="Missing platform id""""
                    )
            )
    }

    @Test
    @DisplayName("startOperation - PersistenceException")
    fun startOperation7() {
        doThrow(PersistenceException(message = "", cause = Exception()))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("startOperation - InvalidPlatformIdException")
    fun startOperation8() {
        doThrow(InvalidPlatformIdException(message = "", cause = Exception()))
            .whenever(operationService)
            .getOperationId(any())

        mockMvc.perform(post(URL_START_OPERATION_ID))
            .andExpect(status().isBadRequest)
    }

    private fun genAccessJWT(): JWToken = genAccessToken(
        platformId = PLATFORM_ID.toString(),
        expiresOn = genExpiresOn(),
        algorithm = algorithm
    )

    private fun genExpiresOn() = LocalDateTime.now().genExpiresOn(6000)
}