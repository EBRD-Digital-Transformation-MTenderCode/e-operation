package com.procurement.operation.controller

import com.procurement.operation.AUTHORIZATION_HEADER_DESCRIPTION
import com.procurement.operation.WWW_AUTHENTICATE_HEADER_DESCRIPTION
import com.procurement.operation.model.AUTHORIZATION_HEADER_NAME
import com.procurement.operation.model.WWW_AUTHENTICATE_HEADER_NAME
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes.key

object ModelDescription {
    object Start {
        fun responseSuccessful(): List<FieldDescriptor> {
            return listOf(
                getFieldDescriptor("data", "The data of response."),
                getFieldDescriptor("data.operationId", "The operation id.")
            )
        }

        fun responseWithFormSuccessful(): List<FieldDescriptor> {
            return listOf(
                getFieldDescriptor("data", "The data of response."),
                getFieldDescriptor("data.operationId", "The operation id."),
                getFieldDescriptor("data.form", "The form.")
            )
        }
    }

    fun responseError(): List<FieldDescriptor> {
        return listOf(
            getFieldDescriptor("errors", "List of errors."),
            getFieldDescriptor("errors[].code", "The code of the error."),
            getFieldDescriptor("errors[].description", "The description of the error.")
        )
    }

    fun authHeader(): HeaderDescriptor = headerWithName(AUTHORIZATION_HEADER_NAME)
        .description(AUTHORIZATION_HEADER_DESCRIPTION)

    fun wwwAuthHeader(value: String): HeaderDescriptor = headerWithName(WWW_AUTHENTICATE_HEADER_NAME)
        .description(WWW_AUTHENTICATE_HEADER_DESCRIPTION)
        .attributes(
            key("value")
                .value(value)
        )
}

private fun getFieldDescriptor(property: String, description: String): FieldDescriptor {
    return fieldWithPath(property).description(description)
}
