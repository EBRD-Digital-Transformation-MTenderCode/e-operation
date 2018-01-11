package com.procurement.operation.service

import com.auth0.jwt.algorithms.Algorithm
import com.nhaarman.mockito_kotlin.*
import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.security.InvalidAuthHeaderTypeException
import com.procurement.operation.exception.security.NoSuchAuthHeaderException
import com.procurement.operation.exception.token.BearerTokenWrongTypeException
import com.procurement.operation.exception.token.InvalidBearerTokenException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.helper.genAccessToken
import com.procurement.operation.helper.genExpiresOn
import com.procurement.operation.helper.genRefreshToken
import com.procurement.operation.helper.genToken
import com.procurement.operation.model.*
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import java.time.LocalDateTime
import java.util.*

class OperationServiceTest {
    companion object {
        private val OPERATION_ID = UUID.randomUUID()
        private val PLATFORM_ID = UUID.randomUUID()
        private val OPERATION_TX = OperationTX(id = OPERATION_ID, platformId = PLATFORM_ID)
    }

    private val algorithm: Algorithm

    private lateinit var operationDao: OperationDao
    private lateinit var service: OperationService

    init {
        val rsaKeyPair = RSAKeyGenerator().generate(2048)
        val rsaService = RSAServiceImpl(keyFactoryService = KeyFactoryServiceImpl())
        algorithm = Algorithm.RSA256(rsaService.toPublicKey(rsaKeyPair.publicKey),
                                     rsaService.toPrivateKey(rsaKeyPair.privateKey)
        )
    }

    @BeforeEach
    fun init() {
        operationDao = mock()
        service = OperationServiceImpl(operationDao)
    }

    @Test
    @DisplayName("startOperation - OK")
    fun getOperationTx() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
        }
        val requestContext = RequestContext(request = request)

        whenever(operationDao.persistOperationTX(any()))
            .thenReturn(true)

        service.getOperationId(requestContext)

        val operationTX = argumentCaptor<OperationTX>()
        verify(operationDao, times(1))
            .persistOperationTX(operationTX.capture())

        assertEquals(PLATFORM_ID, operationTX.firstValue.platformId)
    }

    @Test
    @DisplayName("startOperation - NoSuchAuthHeaderException")
    fun getOperationTx2() {
        val request = MockHttpServletRequest()
        val requestContext = RequestContext(request = request)

        assertThrows(
            NoSuchAuthHeaderException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - InvalidAuthHeaderTypeException")
    fun getOperationTx3() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BASIC + genAccessJWT())
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            InvalidAuthHeaderTypeException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - InvalidBearerTokenException")
    fun getOperationTx4() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + "UNKNOWN_TOKEN")
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            InvalidBearerTokenException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - BearerTokenWrongTypeException(typToken is missing)")
    fun getOperationTx5() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWTWithoutTokenType())
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            BearerTokenWrongTypeException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - BearerTokenWrongTypeException(invalid typToken)")
    fun getOperationTx6() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genRefreshJWT())
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            BearerTokenWrongTypeException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - MissingPlatformIdException")
    fun getOperationTx7() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWTWithoutPlatformId())
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            MissingPlatformIdException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("startOperation - PersistenceException")
    fun getOperationTx8() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
        }
        val requestContext = RequestContext(request = request)

        doThrow(PersistenceException(context = requestContext, cause = Exception()))
            .whenever(operationDao)
            .persistOperationTX(any())

        assertThrows(
            PersistenceException::class.java,
            {
                service.getOperationId(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - OK")
    fun checkOperationTx() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        whenever(operationDao.getOperationTX(eq(OPERATION_ID)))
            .thenReturn(OPERATION_TX)

        service.checkOperationTx(requestContext)

        verify(operationDao, times(1))
            .getOperationTX(OPERATION_ID)
    }

    @Test
    @DisplayName("checkOperationTx - NoSuchAuthHeaderException")
    fun checkOperationTx2() {
        val request = MockHttpServletRequest()
        val requestContext = RequestContext(request = request)

        assertThrows(
            NoSuchAuthHeaderException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - InvalidAuthHeaderTypeException")
    fun checkOperationTx3() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BASIC + genAccessJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            InvalidAuthHeaderTypeException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - InvalidBearerTokenException")
    fun checkOperationTx4() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + "UNKNOWN_TOKEN")
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            InvalidBearerTokenException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - BearerTokenWrongTypeException(typToken is missing)")
    fun checkOperationTx5() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWTWithoutTokenType())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            BearerTokenWrongTypeException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - BearerTokenWrongTypeException(invalid typToken)")
    fun checkOperationTx6() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genRefreshJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            BearerTokenWrongTypeException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - MissingPlatformIdException")
    fun checkOperationTx7() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWTWithoutPlatformId())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            MissingPlatformIdException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - MissingOperationIdException")
    fun checkOperationTx8() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
        }
        val requestContext = RequestContext(request = request)

        assertThrows(
            MissingOperationIdException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - OperationIdNotFoundException")
    fun checkOperationTx9() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        whenever(operationDao.getOperationTX(OPERATION_ID))
            .thenReturn(null)

        assertThrows(
            PersistenceException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - PersistenceException")
    fun checkOperationTx10() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        doThrow(PersistenceException(context = requestContext, cause = Exception()))
            .whenever(operationDao)
            .getOperationTX(OPERATION_ID)

        assertThrows(
            PersistenceException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    @Test
    @DisplayName("checkOperationTx - do not match platform id")
    fun checkOperationTx11() {
        val request = MockHttpServletRequest().also {
            it.addHeader(HEADER_NAME_AUTHORIZATION, AUTHORIZATION_PREFIX_BEARER + genAccessJWT())
            it.addHeader(HEADER_NAME_OPERATION_ID, OPERATION_ID)
        }
        val requestContext = RequestContext(request = request)

        whenever(operationDao.getOperationTX(OPERATION_ID))
            .thenReturn(OperationTX(id = OPERATION_ID, platformId = UUID.randomUUID()))

        assertThrows(
            OperationIdNotFoundException::class.java,
            {
                service.checkOperationTx(requestContext)
            }
        )
    }

    private fun genAccessJWT(): JWToken = genAccessToken(platformId = PLATFORM_ID,
                                                         expiresOn = genExpiresOn(),
                                                         algorithm = algorithm
    )

    private fun genRefreshJWT(): JWToken = genRefreshToken(platformId = PLATFORM_ID,
                                                           expiresOn = genExpiresOn(),
                                                           algorithm = algorithm
    )

    private fun genAccessJWTWithoutPlatformId(): JWToken = genToken(
        claims = mapOf(),
        header = mapOf(HEADER_NAME_TOKEN_TYPE to ACCESS_TOKEN_TYPE),
        expiresOn = genExpiresOn(),
        algorithm = algorithm
    )

    private fun genAccessJWTWithoutTokenType(): JWToken = genToken(
        claims = mapOf<String, Any>(CLAIM_NAME_PLATFORM_ID to PLATFORM_ID),
        header = mapOf(),
        expiresOn = genExpiresOn(),
        algorithm = algorithm
    )

    private fun genExpiresOn() = LocalDateTime.now().genExpiresOn(6000)
}