package com.procurement.operation.exception.token

import com.procurement.operation.model.RequestContext

class BearerTokenWrongTypeException(val context: RequestContext) : RuntimeException()
