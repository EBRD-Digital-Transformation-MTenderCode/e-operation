package com.procurement.operation.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.model.CLAIM_NAME_PLATFORM_ID
import com.procurement.operation.model.HEADER_NAME_OPERATION_ID
import com.procurement.operation.model.OperationTX
import com.procurement.operation.model.RequestContext
import java.util.*

interface OperationService {
    fun getOperationId(context: RequestContext): UUID

    fun checkOperationTx(context: RequestContext)
}

class OperationServiceImpl(
    private val operationDao: OperationDao
) : OperationService {

    override fun getOperationId(context: RequestContext): UUID {
        val jwt = context.getAccessJWT()
        val operation = OperationTX(platformId = jwt.getPlatformId(context))
        operation.persist(context)
        return operation.id
    }

    override fun checkOperationTx(context: RequestContext) {
        val jwt = context.getAccessJWT()
        val platformId = jwt.getPlatformId(context)
        val operationId = context.getOperationIdFromHeader()
        val operationTX = getOperationTX(context, operationId)
        if (operationTX.platformId != platformId) {
            throw OperationIdNotFoundException(context = context)
        }
    }

    private fun DecodedJWT.getPlatformId(context: RequestContext): UUID {
        val platformId = this.getClaim(CLAIM_NAME_PLATFORM_ID)
        if (platformId.isNull) {
            throw MissingPlatformIdException(context)
        }
        return UUID.fromString(platformId.asString())
    }

    private fun OperationTX.persist(context: RequestContext) = try {
        operationDao.persistOperationTX(this)
    } catch (ex: Exception) {
        throw PersistenceException(context = context, cause = ex)
    }

    private fun getOperationTX(context: RequestContext, operationId: UUID): OperationTX = try {
        operationDao.getOperationTX(operationId)
            ?: throw OperationIdNotFoundException(context = context)
    } catch (ex: Exception) {
        throw PersistenceException(context = context, cause = ex)
    }

    private fun RequestContext.getOperationIdFromHeader(): UUID =
        this.request.getHeader(HEADER_NAME_OPERATION_ID)?.let {
            UUID.fromString(it)
        } ?: throw MissingOperationIdException(this)
}