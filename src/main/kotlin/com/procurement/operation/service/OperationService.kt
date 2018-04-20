package com.procurement.operation.service

import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.model.OperationTX
import java.util.*

interface OperationService {
    fun getOperationId(token: String): UUID

    fun checkOperation(token: String, operationId: UUID)
}

class OperationServiceImpl(
    private val jwtService: JwtService,
    private val operationDao: OperationDao
) : OperationService {

    override fun getOperationId(token: String): UUID {
        val platformId = getPlatformIdByToken(token)
        val operationTx = OperationTX(platformId = platformId)
        operationDao.persistOperationTX(operationTx)
        return operationTx.id
    }

    override fun checkOperation(token: String, operationId: UUID) {
        val platformId = getPlatformIdByToken(token)
        val operationTX = operationDao.getOperationTX(operationId)
        if (operationTX.platformId != platformId) {
            throw UnknownOperationException(message = "The operation is unknown.")
        }
    }

    private fun getPlatformIdByToken(token: String): UUID {
        val jwt = jwtService.toJWT(token)
        return jwtService.getPlatformId(jwt)
    }
}