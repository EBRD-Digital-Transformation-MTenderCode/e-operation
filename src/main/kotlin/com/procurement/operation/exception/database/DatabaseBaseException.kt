package com.procurement.operation.exception.database

import com.procurement.operation.model.RequestContext


open class DatabaseBaseException(message: String, val context: RequestContext, cause: Throwable) : RuntimeException(message, cause)