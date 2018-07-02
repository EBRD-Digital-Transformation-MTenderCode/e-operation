package com.procurement.operation.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("errors")
data class ErrorRS @JsonCreator constructor(
    @field:JsonProperty("errors")
    @param:JsonProperty("errors") val errors: List<ErrorRS.Error>
) {

    @JsonPropertyOrder("code", "description")
    class Error @JsonCreator constructor(
        @field:JsonProperty("code")
        @param:JsonProperty("code")
        private val code: String,

        @field:JsonProperty("description")
        @param:JsonProperty("description")
        private val description: String
    )
}
