package com.procurement.operation.exception.client

import org.springframework.http.HttpStatus

class RemoteServiceException(val code: HttpStatus, val payload: String?, message: String, exception: Throwable) :
    RuntimeException(message, exception)