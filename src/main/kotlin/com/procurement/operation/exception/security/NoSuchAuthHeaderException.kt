package com.procurement.operation.exception.security

import com.procurement.operation.model.RequestContext

class NoSuchAuthHeaderException(message: String, context: RequestContext) : SecurityBaseException(message, context)