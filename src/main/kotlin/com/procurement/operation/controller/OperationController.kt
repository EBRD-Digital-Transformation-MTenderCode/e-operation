package com.procurement.operation.controller

import com.procurement.operation.helper.extractOperationId
import com.procurement.operation.helper.getBearerTokenByAuthHeader
import com.procurement.operation.model.AUTHORIZATION_HEADER_NAME
import com.procurement.operation.model.OPERATION_ID_HEADER_NAME
import com.procurement.operation.model.response.CheckRS
import com.procurement.operation.model.response.Data
import com.procurement.operation.model.response.OperationRS
import com.procurement.operation.service.OperationService
import org.jetbrains.annotations.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/operations")
class OperationController(
    private val operationService: OperationService
) {
    @RequestMapping(method = [RequestMethod.POST])
    fun startOperation(
        @NotNull
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = "") authorizationHeader: String): ResponseEntity<OperationRS> {

        val token = getBearerTokenByAuthHeader(authorizationHeader)
        val operationId = operationService.getOperationId(token)
        return ResponseEntity.ok(
            OperationRS(data = Data(operationId))
        )
    }

    @RequestMapping("/check", method = [RequestMethod.GET])
    fun checkOperationId(
        @NotNull
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = "") authorizationHeader: String,
        @NotNull
        @RequestHeader(
            value = OPERATION_ID_HEADER_NAME,
            required = false,
            defaultValue = "") operationHeader: String): ResponseEntity<CheckRS> {

        val token = getBearerTokenByAuthHeader(authorizationHeader)
        val operationId = extractOperationId(operationHeader)
        operationService.checkOperation(token = token, operationId = operationId)
        return ResponseEntity.ok(CheckRS())
    }
}