package com.procurement.operation.service

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.operation.OPERATION_ID
import com.procurement.operation.OPERATION_TX
import com.procurement.operation.OTHER_OPERATION_TX
import com.procurement.operation.PLATFORM_ID
import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.helper.genAccessJWT
import com.procurement.operation.model.OperationTX
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OperationServiceTest {
    private val algorithm: Algorithm

    private lateinit var jwtService: JwtService
    private lateinit var operationDao: OperationDao
    private lateinit var service: OperationService

    init {
        val rsaKeyPair = RSAKeyGenerator().generate(2048)
        val rsaService = RSAServiceImpl(keyFactoryService = KeyFactoryServiceImpl())
        algorithm = Algorithm.RSA256(
            rsaService.toPublicKey(rsaKeyPair.publicKey),
            rsaService.toPrivateKey(rsaKeyPair.privateKey)
        )
    }

    @BeforeEach
    fun init() {
        jwtService = mock()
        operationDao = mock()
        service = OperationServiceImpl(jwtService = jwtService, operationDao = operationDao)
    }

    @Nested
    inner class GetOperation {
        @Test
        @DisplayName("Get the operation id was successful")
        fun getOperationId() {
            val token = genAccessJWT(algorithm)

            val jwt = mock<DecodedJWT>()
            whenever(jwtService.toJWT(token))
                .thenReturn(jwt)
            whenever(jwtService.getPlatformId(jwt))
                .thenReturn(PLATFORM_ID)
            doNothing()
                .whenever(operationDao)
                .persistOperationTX(any())

            val operationId = service.getOperationId(token)

            assertNotNull(operationId)

            val operationTX = argumentCaptor<OperationTX>()
            verify(operationDao, times(1))
                .persistOperationTX(operationTX.capture())
            assertEquals(PLATFORM_ID, operationTX.firstValue.platformId)
        }
    }

    @Nested
    inner class CheckOperation {
        @Test
        @DisplayName("Check the operation id was successful")
        fun checkOperationTx() {
            val token = genAccessJWT(algorithm)

            val jwt = mock<DecodedJWT>()
            whenever(jwtService.toJWT(token))
                .thenReturn(jwt)
            whenever(jwtService.getPlatformId(jwt))
                .thenReturn(PLATFORM_ID)
            whenever(operationDao.getOperationTX(eq(OPERATION_ID)))
                .thenReturn(OPERATION_TX)

            service.checkOperation(token, OPERATION_ID)

            verify(operationDao, times(1))
                .getOperationTX(OPERATION_ID)
        }

        @Test
        @DisplayName("The operation id is unknown")
        fun operationIdUnknown() {
            val token = genAccessJWT(algorithm)

            val jwt = mock<DecodedJWT>()
            whenever(jwtService.toJWT(token))
                .thenReturn(jwt)
            whenever(jwtService.getPlatformId(jwt))
                .thenReturn(PLATFORM_ID)
            whenever(operationDao.getOperationTX(eq(OPERATION_ID)))
                .thenReturn(OTHER_OPERATION_TX)

            assertThrows(
                UnknownOperationException::class.java,
                {
                    service.checkOperation(token, OPERATION_ID)
                }
            )
        }
    }
}