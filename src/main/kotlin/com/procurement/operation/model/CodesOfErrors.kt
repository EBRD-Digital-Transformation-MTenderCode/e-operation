package com.procurement.operation.model

import com.procurement.operation.configuration.properties.GlobalProperties
import org.springframework.http.HttpStatus

enum class CodesOfErrors(val httpStatus: HttpStatus, group: String, id: String) {
    AUTH_HEADER_NO_SUCH(httpStatus = HttpStatus.UNAUTHORIZED, group = "02", id = "01"),
    AUTH_HEADER_INVALID_TYPE(httpStatus = HttpStatus.UNAUTHORIZED, group = "02", id = "02"),
    AUTH_TOKEN_EMPTY(httpStatus = HttpStatus.UNAUTHORIZED, group = "03", id = "01"),
    AUTH_TOKEN_INVALID_TYPE(httpStatus = HttpStatus.UNAUTHORIZED, group = "03", id = "02"),
    AUTH_TOKEN_INVALID(httpStatus = HttpStatus.UNAUTHORIZED, group = "03", id = "06"),
    AUTH_TOKEN_PLATFORM_MISSING(httpStatus = HttpStatus.BAD_REQUEST, group = "03", id = "07"),
    AUTH_TOKEN_PLATFORM_INVALID(httpStatus = HttpStatus.BAD_REQUEST, group = "03", id = "08"),
    OPERATION_MISSING(httpStatus = HttpStatus.BAD_REQUEST, group = "04", id = "01"),
    OPERATION_INVALID(httpStatus = HttpStatus.BAD_REQUEST, group = "04", id = "02"),
    OPERATION_NOT_FOUND(httpStatus = HttpStatus.NOT_FOUND, group = "04", id = "03"),
    REQUEST_FORM_INVALID(httpStatus = HttpStatus.BAD_REQUEST, group = "05", id = "01"),
    INTERNAL_SERVER_ERROR(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, group = "00", id = "00");

    val code: String = "${httpStatus.value()}.${GlobalProperties.serviceId}.$group.$id"

    override fun toString(): String = code
}