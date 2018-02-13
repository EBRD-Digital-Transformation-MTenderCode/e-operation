package com.procurement.operation.exception.token

import com.procurement.operation.model.RequestContext

open class TokenBaseException(message: String, val context: RequestContext): RuntimeException(message)