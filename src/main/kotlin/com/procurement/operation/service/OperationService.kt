package com.procurement.operation.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.InvalidOperationIdException
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.MissingOperationIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.database.ReadException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.logging.MDCKey
import com.procurement.operation.logging.mdc
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
        val platformId = jwt.getPlatformId(context)
        mdc(MDCKey.PLATFORM_ID, platformId) {
            val operation = OperationTX(platformId = platformId)
            operation.persist(context)
            return operation.id
        }
    }

    override fun checkOperationTx(context: RequestContext) {
        val jwt = context.getAccessJWT()
        val platformId = jwt.getPlatformId(context)
        val operationId = context.getOperationIdFromHeader()
        val operationTX = getOperationTX(context, operationId)
        if (operationTX.platformId != platformId) {
            throw OperationIdNotFoundException(message = "Operation not found.", context = context)
        }
    }

    private fun DecodedJWT.getPlatformId(context: RequestContext): UUID {
        val platformId = this.getClaim(CLAIM_NAME_PLATFORM_ID)
        if (platformId.isNull) {
            throw MissingPlatformIdException(message = "Missing platform id.", context = context)
        }
        return try {
            UUID.fromString(platformId.asString())
        } catch (ex: Exception) {
            throw InvalidPlatformIdException(message = "Invalid platform id.", context = context, cause = ex)
        }
    }

    private fun OperationTX.persist(context: RequestContext) = try {
        operationDao.persistOperationTX(this)
    } catch (ex: Exception) {
        throw PersistenceException(message = "Error writing to database.", context = context, cause = ex)
    }

    private fun getOperationTX(context: RequestContext, operationId: UUID): OperationTX = try {
        operationDao.getOperationTX(operationId)
    } catch (ex: Exception) {
        throw ReadException(message = "Error read from database.", context = context, cause = ex)
    } ?: throw OperationIdNotFoundException(message = "Operation not found.", context = context)

    private fun RequestContext.getOperationIdFromHeader(): UUID =
        this.request.getHeader(HEADER_NAME_OPERATION_ID)?.let {
            try {
                UUID.fromString(it)
            } catch (ex: Exception) {
                throw InvalidOperationIdException(message = "Invalid operation id.", context = this, cause = ex)
            }
        } ?: throw MissingOperationIdException(message = "Missing operation id.", context = this)
}