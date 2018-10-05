package com.procurement.operation.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.operation.exception.FormsException
import com.procurement.operation.exception.client.RemoteServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.client.RestTemplate
import java.net.URI

class FormsServiceTest {
    private val RESPONSE_FORM = "RESPONSE FORM"
    private val PARAM_FORM_NAME = "form"
    private val PARAM_LANG_NAME = "lang"
    private val FORM_NAME = "cn"
    private val LANG = "UA"
    private val URL = "http://forms:8080/forms/$FORM_NAME?lang=$LANG"

    private lateinit var webClient: RestTemplate
    private lateinit var service: FormsServiceImpl

    @BeforeEach
    fun init() {
        webClient = mock()
        service = FormsServiceImpl(webClient = webClient)
    }

    @Test
    fun success() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_LANG_NAME, LANG)

        val response = ResponseEntity.ok(RESPONSE_FORM)
        whenever(webClient.getForEntity(any<URI>(), eq(String::class.java)))
            .thenReturn(response)

        val form = service.getForms(request)
        assertEquals(RESPONSE_FORM, form)

        val urlCaptor = ArgumentCaptor.forClass(URI::class.java)
        verify(webClient, times(1))
            .getForEntity(urlCaptor.capture(), eq(String::class.java))

        val url = urlCaptor.value.toString()
        assertEquals(URL, url)
    }

    @Test
    fun noNameForm() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_LANG_NAME, LANG)

        val form = service.getForms(request)
        assertNull(form)

        verify(webClient, times(0))
            .getForEntity(any<URI>(), eq(String::class.java))
    }

    @Test
    fun emptyNameForm() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, "")
        request.addParameter(PARAM_LANG_NAME, LANG)

        assertEquals(
            "The query parameter '$PARAM_FORM_NAME' is empty.",
            assertThrows<FormsException> {
                service.getForms(request)
            }.message
        )

        verify(webClient, times(0))
            .getForEntity(any<URI>(), eq(String::class.java))
    }

    @Test
    fun multiNameForm() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_LANG_NAME, LANG)

        assertEquals(
            "The query parameter '$PARAM_FORM_NAME' contains more than one value.",
            assertThrows<FormsException> {
                service.getForms(request)
            }.message
        )

        verify(webClient, times(0))
            .getForEntity(any<URI>(), eq(String::class.java))
    }

    @Test
    fun remote400() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_LANG_NAME, LANG)

        val response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("")
        whenever(webClient.getForEntity(any<URI>(), eq(String::class.java)))
            .thenReturn(response)

        assertEquals(
            "Client error of remote service by uri: '$URL'.",
            assertThrows<RemoteServiceException> {
                service.getForms(request)
            }.message
        )

        val urlCaptor = ArgumentCaptor.forClass(URI::class.java)
        verify(webClient, times(1))
            .getForEntity(urlCaptor.capture(), eq(String::class.java))

        val url = urlCaptor.value.toString()
        assertEquals(URL, url)
    }

    @Test
    fun remote500() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_LANG_NAME, LANG)

        val response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("")
        whenever(webClient.getForEntity(any<URI>(), eq(String::class.java)))
            .thenReturn(response)

        assertEquals(
            "Server error of remote service by uri: '$URL'.",
            assertThrows<RemoteServiceException> {
                service.getForms(request)
            }.message
        )

        val urlCaptor = ArgumentCaptor.forClass(URI::class.java)
        verify(webClient, times(1))
            .getForEntity(urlCaptor.capture(), eq(String::class.java))

        val url = urlCaptor.value.toString()
        assertEquals(URL, url)
    }

    @Test
    fun webClientError() {
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_FORM_NAME, FORM_NAME)
        request.addParameter(PARAM_LANG_NAME, LANG)


        whenever(webClient.getForEntity(any<URI>(), eq(String::class.java)))
            .thenThrow(RuntimeException())

        assertEquals(
            "Error of remote service by uri: '$URL'.",
            assertThrows<RemoteServiceException> {
                service.getForms(request)
            }.message
        )

        val urlCaptor = ArgumentCaptor.forClass(URI::class.java)
        verify(webClient, times(1))
            .getForEntity(urlCaptor.capture(), eq(String::class.java))

        val url = urlCaptor.value.toString()
        assertEquals(URL, url)
    }
}
