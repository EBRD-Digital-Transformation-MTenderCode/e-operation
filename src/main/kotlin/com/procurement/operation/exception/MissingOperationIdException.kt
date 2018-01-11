package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class MissingOperationIdException(val context: RequestContext) : RuntimeException()
