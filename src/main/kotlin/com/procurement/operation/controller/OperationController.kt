package com.procurement.operation.controller

import com.procurement.operation.helper.extractOperationId
import com.procurement.operation.helper.getBearerTokenByAuthHeader
import com.procurement.operation.model.AUTHORIZATION_HEADER_NAME
import com.procurement.operation.model.OPERATION_ID_HEADER_NAME
import com.procurement.operation.service.FormsService
import com.procurement.operation.service.OperationService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/operations")
class OperationController(
    private val operationService: OperationService,
    private val formsService: FormsService
) {
    @RequestMapping(method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun startOperation(
        request: HttpServletRequest,
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = ""
        ) authorizationHeader: String): ResponseEntity<String> {

        val token = getBearerTokenByAuthHeader(authorizationHeader)
        val form = formsService.getForms(request)
        val operationId = operationService.getOperationId(token)

        return if (form == null)
            ResponseEntity.ok("{\n  \"data\": {\n    \"operationId\": \"$operationId\"\n  }\n}")
        else
            ResponseEntity.ok("{\n  \"data\": {\n    \"operationId\": \"$operationId\",\n    \"form\": $form\n  }\n}")
    }

    @RequestMapping("/check", method = [RequestMethod.GET])
    fun checkOperationId(
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = ""
        ) authorizationHeader: String,
        @RequestHeader(
            value = OPERATION_ID_HEADER_NAME,
            required = false,
            defaultValue = ""
        ) operationHeader: String): ResponseEntity<Unit> {

        val token = getBearerTokenByAuthHeader(authorizationHeader)
        val operationId = extractOperationId(operationHeader)
        operationService.checkOperation(token = token, operationId = operationId)
        return ResponseEntity.ok().build()
    }
}