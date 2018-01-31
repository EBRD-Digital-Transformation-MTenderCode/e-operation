package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class InvalidOperationIdException(val context: RequestContext, ex: Exception) : RuntimeException(ex)