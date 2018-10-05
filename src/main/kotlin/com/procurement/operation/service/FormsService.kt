package com.procurement.operation.service

import com.procurement.operation.exception.FormsException
import com.procurement.operation.exception.client.RemoteServiceException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpServletRequest

private data class FormParameters(val form: String, val parameters: MultiValueMap<String, String>)

interface FormsService {
    fun getForms(request: HttpServletRequest): String?
}

class FormsServiceImpl(private val webClient: RestTemplate) : FormsService {
    companion object {
        private const val NAME_PARAM_FORM = "form"
    }

    override fun getForms(request: HttpServletRequest): String? =
        getQueryParams(request)?.let { parameters ->
            val uri = genUri(parameters)
            remoteService(uri)
        }

    private fun getQueryParams(request: HttpServletRequest): FormParameters? {
        var nameForm: String? = null
        val parameters = LinkedMultiValueMap<String, String>().apply {
            request.parameterMap.forEach { key, values ->
                val name = key.toLowerCase()
                if (isNameForm(name))
                    nameForm = getNameForm(values)
                else
                    this[name] = values.asList()
            }
        }

        return if (nameForm != null) FormParameters(form = nameForm!!, parameters = parameters) else null
    }

    private fun isNameForm(name: String) = name == NAME_PARAM_FORM

    private fun getNameForm(values: Array<String>): String {
        if (values.size > 1)
            throw FormsException("The query parameter 'form' contains more than one value.")
        if (values[0].isBlank())
            throw FormsException("The query parameter 'form' is empty.")
        return values[0]
    }

    private fun genUri(queryParams: FormParameters) = UriComponentsBuilder.fromHttpUrl("http://forms:8080")
        .pathSegment("forms")
        .pathSegment(queryParams.form)
        .queryParams(queryParams.parameters)
        .build(emptyMap<String, Any>())

    private fun remoteService(uri: URI): String? {
        val response = doRemoteRequest(uri)
        return when {
            response.statusCode.is4xxClientError -> {
                throw RemoteServiceException(
                    code = response.statusCode,
                    payload = response.body,
                    message = "Client error of remote service by uri: '$uri'."
                )
            }
            response.statusCode.is5xxServerError -> {
                throw RemoteServiceException(
                    code = response.statusCode,
                    payload = response.body,
                    message = "Server error of remote service by uri: '$uri'."
                )
            }
            else -> response.body
        }
    }

    private fun doRemoteRequest(uri: URI) = try {
        webClient.getForEntity(uri, String::class.java)
    } catch (exception: Exception) {
        throw RemoteServiceException(
            message = "Error of remote service by uri: '$uri'.",
            exception = exception
        )
    }
}