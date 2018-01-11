package com.procurement.operation.exception.security

import com.procurement.operation.model.RequestContext

class InvalidAuthHeaderTypeException(val context: RequestContext) : RuntimeException()