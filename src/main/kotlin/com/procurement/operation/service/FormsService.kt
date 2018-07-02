package com.procurement.operation.service

import com.procurement.operation.client.execute
import com.procurement.operation.exception.FormsException
import kotlinx.coroutines.experimental.reactive.awaitFirst
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest

private data class FormParameters(val form: String, val parameters: MultiValueMap<String, String>)

interface FormsService {
    suspend fun getForms(request: HttpServletRequest): String?
}

class FormsServiceImpl(private val webClientBuilder: WebClient.Builder) : FormsService {

    override suspend fun getForms(request: HttpServletRequest): String? =
        getQueryParams(request)?.let { parameters ->
            val uri = genUri(parameters)
            webClientBuilder.execute<String>(uri) { it.awaitFirst() }
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

    private fun isNameForm(name: String) = name == "form"

    private fun getNameForm(values: Array<String>): String {
        if (values.isEmpty())
            throw FormsException("The query parameter 'form' is empty.")
        if (values.size > 1)
            throw FormsException("The query parameter 'form' contains more than one value.")
        return values[0]
    }

    private fun genUri(queryParams: FormParameters) = UriComponentsBuilder.fromHttpUrl("http://E-FORMS")
        .pathSegment("forms")
        .pathSegment(queryParams.form)
        .queryParams(queryParams.parameters)
        .build(emptyMap<String, Any>())
}