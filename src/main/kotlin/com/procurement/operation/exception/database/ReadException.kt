package com.procurement.operation.exception.database

import com.procurement.operation.model.RequestContext

class ReadException(val context: RequestContext, cause: Throwable) : RuntimeException(cause)