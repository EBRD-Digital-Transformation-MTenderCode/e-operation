package com.procurement.operation.model.response

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("success")
class CheckRS : BaseRS(true)
