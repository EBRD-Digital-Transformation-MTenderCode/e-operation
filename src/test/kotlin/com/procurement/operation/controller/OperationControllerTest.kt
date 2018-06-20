package com.procurement.operation.controller

import com.auth0.jwt.algorithms.Algorithm
import com.nhaarman.mockito_kotlin.*
import com.procurement.operation.*
import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.exception.database.ReadOperationException
import com.procurement.operation.exception.database.SaveOperationException
import com.procurement.operation.exception.token.InvalidAuthTokenException
import com.procurement.operation.exception.token.InvalidTokenTypeException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.helper.genAccessJWT
import com.procurement.operation.model.*
import com.procurement.operation.security.KeyFactoryServiceImpl
import com.procurement.operation.security.RSAKeyGenerator
import com.procurement.operation.security.RSAServiceImpl
import com.procurement.operation.service.OperationService
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.*
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.testcontainers.shaded.org.glassfish.jersey.internal.util.Base64

@ExtendWith(RestDocumentationExtension::class)
class OperationControllerTest {
    private val algorithm: Algorithm
    private lateinit var mockMvc: MockMvc
    private lateinit var operationService: OperationService

    init {
        val rsaKeyPair = RSAKeyGenerator().generate(2048)
        val rsaService = RSAServiceImpl(keyFactoryService = KeyFactoryServiceImpl())
        algorithm = Algorithm.RSA256(
            rsaService.toPublicKey(rsaKeyPair.publicKey),
            rsaService.toPrivateKey(rsaKeyPair.privateKey)
        )
    }

    @BeforeEach
    fun init(restDocumentation: RestDocumentationContextProvider) {
        operationService = mock()

        val controller = OperationController(operationService = operationService)
        val exceptionHandler = WebExceptionHandler()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionHandler)
            .apply<StandaloneMockMvcBuilder>(
                MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                    .uris()
                    .withScheme("https")
                    .withHost("eprocurement.systems")
                    .and()
                    .snippets()
                    .and()
                    .operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint())
            )
            .build()
    }

    @Nested
    inner class StartOperation {
        private val URL = "/operations"

        @Test
        @DisplayName("The check the operation was successful")
        fun startOperation() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token

            whenever(operationService.getOperationId(token))
                .thenReturn(OPERATION_ID)

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", equalTo(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.operationId", equalTo(OPERATION_ID.toString())))
                .andDo(
                    document(
                        "start/success",
                        requestHeaders(
                            headerWithName(AUTHORIZATION_HEADER_NAME)
                                .description("Bearer auth credentials.")
                        ),
                        responseFields(ModelDescription.Start.responseSuccessful())
                    )
                )
        }

        @Test
        @DisplayName("No such the authentication header")
        fun noSuchAuthHeader() {
            val authHeaderValue = ""
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.header.noSuch")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        IsEqual.equalTo("The authentication header is missing.")
                    )
                )
                .andDo(
                    document(
                        "start/errors/no_such_auth_header",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid type of the authentication header")
        fun invalidAuthHeaderType() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BASIC + Base64.encodeAsString(ACCESS_TOKEN)
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.header.invalidType")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid type of the authentication header. Expected type is 'Bearer'.")
                    )
                )
                .andDo(
                    document(
                        "start/errors/invalid_type_auth_header",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("The authentication token is empty")
        fun emptyAuthToken() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.empty")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("The authentication token is empty.")))
                .andDo(
                    document(
                        "start/errors/auth_token_is_empty",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid type of the authentication token.")
        fun invalidTokenType() {
            val token = REFRESH_TOKEN
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid type of the authentication token.""""

            doThrow(InvalidTokenTypeException::class)
                .whenever(operationService)
                .getOperationId(eq(token))

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.invalidType")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid type of the authentication token.")
                    )
                )
                .andDo(
                    document(
                        "start/errors/invalid_type_auth_token",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid the authentication token")
        fun invalidAuthToken() {
            val token = INVALID_ACCESS_TOKEN
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid.""""

            doThrow(InvalidAuthTokenException::class)
                .whenever(operationService)
                .getOperationId(eq(token))

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.invalid")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid the access token.")
                    )
                )
                .andDo(
                    document(
                        "start/errors/invalid_auth_token",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Missing the platform id")
        fun missingPlatformId() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Missing the platform id.""""

            doThrow(MissingPlatformIdException::class)
                .whenever(operationService)
                .getOperationId(eq(token))

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isBadRequest)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.platform.missing")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Missing the platform id.")))
                .andDo(
                    document(
                        "start/errors/missing_platform",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid the platform id")
        fun invalidPlatformId() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid the platform id.""""

            doThrow(InvalidPlatformIdException::class)
                .whenever(operationService)
                .getOperationId(eq(token))

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isBadRequest)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.platform.invalid")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Invalid the platform id.")))
                .andDo(
                    document(
                        "start/errors/invalid_platform",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Error of save operation to database")
        fun errorSaveOperation() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token

            doThrow(SaveOperationException::class)
                .whenever(operationService)
                .getOperationId(token)

            mockMvc.perform(
                post(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isInternalServerError)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("global.internal_server_error")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Internal server error.")))
                .andDo(
                    document(
                        "start/errors/operation_save_in_database",
                        responseFields(ModelDescription.responseError())
                    )
                )
        }
    }

    @Nested
    inner class CheckOperation {
        private val URL = "/operations/check"

        @Test
        @DisplayName("The check the operation was successful")
        fun checkOperation() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + genAccessJWT(algorithm)
            mockMvc.perform(
                MockMvcRequestBuilders.get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", equalTo(true)))
                .andDo(
                    document(
                        "check/success",
                        requestHeaders(
                            headerWithName(AUTHORIZATION_HEADER_NAME)
                                .description("Bearer auth credentials."),
                            headerWithName(OPERATION_ID_HEADER_NAME)
                                .description("The token of an operation that needs checking.")
                        ),
                        responseFields(ModelDescription.Check.responseSuccessful())
                    )
                )
        }

        @Test
        @DisplayName("No such the authentication header")
        fun noSuchAuthHeader() {
            val authHeaderValue = ""
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.header.noSuch")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        IsEqual.equalTo("The authentication header is missing.")
                    )
                )
                .andDo(
                    document(
                        "check/errors/no_such_auth_header",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid type of the authentication header")
        fun invalidAuthHeaderType() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BASIC + Base64.encodeAsString(ACCESS_TOKEN)
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.header.invalidType")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid type of the authentication header. Expected type is 'Bearer'.")
                    )
                )
                .andDo(
                    document(
                        "check/errors/invalid_type_auth_header",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("The authentication token is empty")
        fun emptyAuthToken() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER
            val wwwAuthHeaderValue = BEARER_REALM

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.empty")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("The authentication token is empty.")))
                .andDo(
                    document(
                        "check/errors/auth_token_is_empty",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid type of the authentication token.")
        fun invalidTokenType() {
            val token = REFRESH_TOKEN
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid type of the authentication token.""""

            doThrow(InvalidTokenTypeException::class)
                .whenever(operationService)
                .checkOperation(eq(token), eq(OPERATION_ID))

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.invalidType")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid type of the authentication token.")
                    )
                )
                .andDo(
                    document(
                        "check/errors/invalid_type_auth_token",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        HeaderDocumentation.responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid type of the authentication header")
        fun invalidAuthToken() {
            val token = INVALID_ACCESS_TOKEN
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="The access token is invalid.""""

            doThrow(InvalidAuthTokenException::class)
                .whenever(operationService)
                .checkOperation(eq(token), eq(OPERATION_ID))

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.invalid")))
                .andExpect(
                    jsonPath(
                        "$.errors[0].description",
                        equalTo("Invalid the access token.")
                    )
                )
                .andDo(
                    document(
                        "check/errors/invalid_auth_token",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Missing the platform id")
        fun missingPlatformId() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Missing the platform id.""""

            doThrow(MissingPlatformIdException::class)
                .whenever(operationService)
                .checkOperation(eq(token), eq(OPERATION_ID))

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isBadRequest)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.platform.missing")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Missing the platform id.")))
                .andDo(
                    document(
                        "check/errors/missing_platform",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid the platform id")
        fun invalidPlatformId() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token
            val wwwAuthHeaderValue =
                """$BEARER_REALM, error_code="invalid_token", error_message="Invalid the platform id.""""

            doThrow(InvalidPlatformIdException::class)
                .whenever(operationService)
                .checkOperation(eq(token), eq(OPERATION_ID))

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isBadRequest)
                .andExpect(header().string(WWW_AUTHENTICATE_HEADER_NAME, wwwAuthHeaderValue))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("auth.token.platform.invalid")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Invalid the platform id.")))
                .andDo(
                    document(
                        "check/errors/invalid_platform",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseHeaders(
                            ModelDescription.wwwAuthHeader(wwwAuthHeaderValue)
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Missing the operation id")
        fun missingOperationId() {
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + genAccessJWT(algorithm)

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("operation.missing")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Missing the operation id.")))
                .andDo(
                    document(
                        "check/errors/missing_operation",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Invalid operation id")
        fun invalidOperationId() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token

            doThrow(InvalidOperationIdException::class)
                .whenever(operationService)
                .checkOperation(eq(token), any())

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, INVALID_OPERATION_ID))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("operation.invalid")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Invalid the operation id.")))
                .andDo(
                    document(
                        "check/errors/invalid_operation",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Unknown operation")
        fun unknownOperation() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token

            doThrow(UnknownOperationException::class)
                .whenever(operationService)
                .checkOperation(token, OPERATION_ID)

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("operation.unknown")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Unknown the operation.")))
                .andDo(
                    document(
                        "check/errors/unknown_operation",
                        requestHeaders(
                            ModelDescription.authHeader()
                        ),
                        responseFields(ModelDescription.responseError())
                    )
                )
        }

        @Test
        @DisplayName("Error of read operation from database")
        fun errorReadOperation() {
            val token = genAccessJWT(algorithm)
            val authHeaderValue = AUTHORIZATION_PREFIX_BEARER + token

            doThrow(ReadOperationException::class)
                .whenever(operationService)
                .checkOperation(token, OPERATION_ID)

            mockMvc.perform(
                get(URL)
                    .header(AUTHORIZATION_HEADER_NAME, authHeaderValue)
                    .header(OPERATION_ID_HEADER_NAME, OPERATION_ID))
                .andExpect(status().isInternalServerError)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.errors.length()", equalTo(1)))
                .andExpect(jsonPath("$.errors[0].code", equalTo("global.internal_server_error")))
                .andExpect(jsonPath("$.errors[0].description", equalTo("Internal server error.")))
                .andDo(
                    document(
                        "check/errors/operation_read_from_database",
                        responseFields(ModelDescription.responseError())
                    )
                )
        }
    }
}