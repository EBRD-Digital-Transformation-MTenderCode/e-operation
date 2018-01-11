package com.procurement.operation.model

import java.util.*

data class OperationTX(
    val id: UUID = UUID.randomUUID(),
    val platformId: UUID
)