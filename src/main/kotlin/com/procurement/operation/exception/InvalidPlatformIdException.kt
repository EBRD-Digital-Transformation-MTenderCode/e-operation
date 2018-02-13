package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class InvalidPlatformIdException(message: String, val context: RequestContext, cause: Exception) :
    RuntimeException(message, cause)