package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class OperationIdNotFoundException(message: String, val context: RequestContext) : RuntimeException(message)
