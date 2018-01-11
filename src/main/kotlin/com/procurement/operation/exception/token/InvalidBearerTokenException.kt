package com.procurement.operation.exception.token

import com.procurement.operation.model.RequestContext

class InvalidBearerTokenException(val context: RequestContext) : RuntimeException()
