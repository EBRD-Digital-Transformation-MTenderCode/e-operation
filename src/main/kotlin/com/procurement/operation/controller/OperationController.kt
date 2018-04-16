package com.procurement.operation.controller

import com.procurement.operation.model.response.CheckRS
import com.procurement.operation.model.response.Data
import com.procurement.operation.model.response.OperationRS
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
    fun startOperation(request: HttpServletRequest): ResponseEntity<OperationRS> {
        val operationId = operationService.getOperationId(request)
        return ResponseEntity.ok(
            OperationRS(data = Data(operationId))
        )
    }

    @RequestMapping("/check", method = [RequestMethod.GET])
    fun checkOperationId(request: HttpServletRequest): ResponseEntity<CheckRS> {
        operationService.checkOperationTx(request)
        return ResponseEntity.ok(CheckRS())
    }
}