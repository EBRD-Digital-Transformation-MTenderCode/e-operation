package com.procurement.operation.exception.database

class SaveOperationException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)
}