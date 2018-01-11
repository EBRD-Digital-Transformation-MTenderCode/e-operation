package com.procurement.operation.controller

import com.procurement.operation.model.HEADER_NAME_OPERATION_ID
import com.procurement.operation.model.RequestContext
import com.procurement.operation.service.OperationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/operation")
class OperationController(
    private val operationService: OperationService
) {
    @RequestMapping("/start", method = [RequestMethod.POST])
    fun startOperation(request: HttpServletRequest): ResponseEntity<Void> {
        val operationId = operationService.getOperationId(RequestContext(request = request))
        return ResponseEntity.ok()
            .header(HEADER_NAME_OPERATION_ID, operationId.toString())
            .build()
    }

    @RequestMapping("/check", method = [RequestMethod.HEAD])
    fun checkOperationId(request: HttpServletRequest): ResponseEntity<Void> {
        operationService.checkOperationTx(RequestContext(request = request))
        return ResponseEntity.ok().build()
    }
}