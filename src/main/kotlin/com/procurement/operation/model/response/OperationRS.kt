package com.procurement.operation.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

@JsonPropertyOrder("success", "data")
data class OperationRS @JsonCreator constructor(
    @field:JsonProperty("data")
    @param:JsonProperty("data") val data: Data
) : BaseRS(true)

data class Data @JsonCreator
constructor(
    @field:JsonProperty("operationId")
    @param:JsonProperty("operationId")
    private val operationId: UUID
)
