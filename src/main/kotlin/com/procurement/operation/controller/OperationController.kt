package com.procurement.operation.controller

import com.procurement.operation.helper.extractOperationId
import com.procurement.operation.helper.getBearerTokenByAuthHeader
import com.procurement.operation.model.AUTHORIZATION_HEADER_NAME
import com.procurement.operation.model.OPERATION_ID_HEADER_NAME
import com.procurement.operation.service.FormsService
import com.procurement.operation.service.OperationService
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.annotations.NotNull
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
        @NotNull request: HttpServletRequest,
        @NotNull
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = ""
        ) authorizationHeader: String): ResponseEntity<String> {

        val token = getBearerTokenByAuthHeader(authorizationHeader)
        val form = runBlocking { formsService.getForms(request) }
        val operationId = operationService.getOperationId(token)

        return if (form == null)
            ResponseEntity.ok(
                """
            {
                "data": {
                  "operationId": "$operationId"
                }
            }
            """.trimIndent()
            )
        else
            ResponseEntity.ok(
                """
            {
                "data": {
                  "operationId": "$operationId",
                  "form": $form
                }
            }
            """.trimIndent()
            )
    }

    @RequestMapping("/check", method = [RequestMethod.GET]/*, produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]*/)
    fun checkOperationId(
        @NotNull
        @RequestHeader(
            value = AUTHORIZATION_HEADER_NAME,
            required = false,
            defaultValue = ""
        ) authorizationHeader: String,
        @NotNull
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