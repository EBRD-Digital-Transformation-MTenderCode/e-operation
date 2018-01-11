package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class OperationIdNotFoundException(val context: RequestContext) : RuntimeException()
