package com.procurement.operation.service

import com.auth0.jwt.algorithms.Algorithm
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.token.InvalidAuthTokenException
import com.procurement.operation.exception.token.InvalidTokenTypeException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.helper.genAccessJWT
import com.procurement.operation.helper.genAccessJWTWithInvalidPlatformId
import com.procurement.operation.helper.genAccessJWTWithoutPlatformId
import com.procurement.operation.helper.genAccessJWTWithoutTokenType
import com.procurement.operation.helper.genRefreshJWT
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JwtServiceTest {
    private val algorithm: Algorithm
    private val service = JwtServiceImpl()

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
    }

    @Nested
    inner class ToJwt {
        @Test
        @DisplayName("Decode the token to JWT was successful")
        fun toJWT() {
            val token = genAccessJWT(algorithm)
            service.toJWT(token)
        }

        @Test
        @DisplayName("Invalid the auth token")
        fun invalidAuthTokenException() {
            assertEquals(
                "Invalid the auth token.",
                assertThrows(
                    InvalidAuthTokenException::class.java,
                    {
                        service.toJWT("UNKNOWN_TOKEN")
                    }
                ).message
            )
        }

        @Test
        @DisplayName("Missing type of auth token")
        fun missingTokenType() {
            val token = genAccessJWTWithoutTokenType(algorithm)
            assertEquals(
                "Invalid type of the auth token.",
                assertThrows(
                    InvalidTokenTypeException::class.java,
                    {
                        service.toJWT(token)
                    }
                ).message
            )
        }

        @Test
        @DisplayName("Invalid type of auth token")
        fun invalidTokenType() {
            val token = genRefreshJWT(algorithm)
            assertEquals(
                "Invalid type of the auth token.",
                assertThrows(
                    InvalidTokenTypeException::class.java,
                    {
                        service.toJWT(token)
                    }
                ).message
            )
        }
    }

    @Nested
    inner class GetPlatformId {
        @Test
        @DisplayName("Getting the platform id successfully")
        fun getPlatformId() {
            val token = genAccessJWT(algorithm)
            val jwt = service.toJWT(token)
            service.getPlatformId(jwt)
        }

        @Test
        @DisplayName("Missing the platform id")
        fun missingPlatformId() {
            val token = genAccessJWTWithoutPlatformId(algorithm)
            val jwt = service.toJWT(token)
            assertEquals(
                "Missing the platform id.",
                assertThrows(
                    MissingPlatformIdException::class.java,
                    {
                        service.getPlatformId(jwt)
                    }
                ).message
            )
        }

        @Test
        @DisplayName("Invalid the platform id")
        fun invalidPlatformId() {
            val token = genAccessJWTWithInvalidPlatformId(algorithm)
            val jwt = service.toJWT(token)
            assertEquals(
                "Invalid the platform id.",
                assertThrows(
                    InvalidPlatformIdException::class.java,
                    {
                        service.getPlatformId(jwt)
                    }
                ).message
            )
        }
    }
}