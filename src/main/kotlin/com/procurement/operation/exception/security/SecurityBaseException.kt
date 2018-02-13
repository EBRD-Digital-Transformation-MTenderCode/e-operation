package com.procurement.operation.exception.security

import com.procurement.operation.model.RequestContext

open class SecurityBaseException(message: String, val context: RequestContext) : RuntimeException(message)