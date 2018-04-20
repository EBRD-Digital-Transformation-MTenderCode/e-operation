package com.procurement.operation

import com.procurement.operation.model.OperationTX
import java.util.*

const val AUTHORIZATION_HEADER_DESCRIPTION = "Basic auth credentials."
const val WWW_AUTHENTICATE_HEADER_DESCRIPTION =
    "The HTTP WWW-Authenticate response header defines the authentication method that should be used to gain access to a resource."

val OPERATION_ID: UUID = UUID.randomUUID()
const val INVALID_OPERATION_ID = "INVALID_OPERATION_ID"
val PLATFORM_ID: UUID = UUID.randomUUID()
val OTHER_PLATFORM_ID: UUID = UUID.randomUUID()
val OPERATION_TX = OperationTX(id = OPERATION_ID, platformId = PLATFORM_ID)
val OTHER_OPERATION_TX = OperationTX(id = OPERATION_ID, platformId = OTHER_PLATFORM_ID)

const val ACCESS_TOKEN = "ACCESS_TOKEN"
const val INVALID_ACCESS_TOKEN = "INVALID_ACCESS_TOKEN"
const val REFRESH_TOKEN = "REFRESH_TOKEN"