package com.procurement.operation.exception.token

import com.procurement.operation.model.RequestContext

class MissingPlatformIdException(message: String, context: RequestContext) : TokenBaseException(message, context)
