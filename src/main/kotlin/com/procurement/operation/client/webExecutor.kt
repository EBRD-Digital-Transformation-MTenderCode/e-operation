package com.procurement.operation.client

import com.procurement.operation.exception.client.RemoteServiceException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.URI

inline fun <reified T> WebClient.Builder.execute(uri: URI, transformer: (Mono<T>) -> T): T =
    try {
        val response: Mono<T> = this.build()
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<T>() {})
        transformer(response)
    } catch (ex: WebClientResponseException) {
        throw RemoteServiceException(
            code = ex.statusCode,
            payload = ex.responseBodyAsString,
            message = "Error of remote service by uri: '$uri'.",
            exception = ex
        )
    }

