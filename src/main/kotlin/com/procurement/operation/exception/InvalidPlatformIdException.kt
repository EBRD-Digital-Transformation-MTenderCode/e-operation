package com.procurement.operation.exception

import com.procurement.operation.model.RequestContext

class InvalidPlatformIdException(val context: RequestContext, ex: Exception) : RuntimeException(ex)