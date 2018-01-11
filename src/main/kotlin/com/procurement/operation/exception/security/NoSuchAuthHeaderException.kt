package com.procurement.operation.exception.security

import com.procurement.operation.model.RequestContext

class NoSuchAuthHeaderException(val context: RequestContext) : RuntimeException()