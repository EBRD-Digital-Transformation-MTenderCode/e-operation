package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class MissingOperationIdException(message: String, val context: RequestContext) : RuntimeException(message)
