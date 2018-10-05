package com.procurement.operation.exception.client

import org.springframework.http.HttpStatus

class RemoteServiceException(val code: HttpStatus? = null, val payload: String? = null, message: String, exception: Throwable? = null) :
    RuntimeException(message, exception)