package com.procurement.operation.exception.token

import com.procurement.operation.model.RequestContext

class MissingPlatformIdException(val context: RequestContext) : RuntimeException()
