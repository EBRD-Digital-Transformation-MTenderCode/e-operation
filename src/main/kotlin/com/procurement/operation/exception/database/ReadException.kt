package com.procurement.operation.exception.database

import com.procurement.operation.model.RequestContext

class ReadException(message: String, context: RequestContext, cause: Throwable) :
    DatabaseBaseException(message, context, cause)