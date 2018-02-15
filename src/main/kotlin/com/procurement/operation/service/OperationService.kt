package com.procurement.operation.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.procurement.operation.dao.OperationDao
import com.procurement.operation.exception.InvalidPlatformIdException
import com.procurement.operation.exception.OperationIdNotFoundException
import com.procurement.operation.exception.database.PersistenceException
import com.procurement.operation.exception.database.ReadException
import com.procurement.operation.exception.token.MissingPlatformIdException
import com.procurement.operation.helper.extractAccessJWT
import com.procurement.operation.helper.extractOperationId
import com.procurement.operation.logging.MDCKey
import com.procurement.operation.logging.mdc
import com.procurement.operation.model.CLAIM_NAME_PLATFORM_ID
import com.procurement.operation.model.OperationTX
import java.util.*
import javax.servlet.http.HttpServletRequest

interface OperationService {
    fun getOperationId(request: HttpServletRequest): UUID

    fun checkOperationTx(request: HttpServletRequest)
}

class OperationServiceImpl(
    private val operationDao: OperationDao
) : OperationService {

    override fun getOperationId(request: HttpServletRequest): UUID {
        val jwt = request.extractAccessJWT()
        val platformId = jwt.getPlatformId()
        val operation = OperationTX(platformId = platformId)
        operation.persist()
        return operation.id
    }

    override fun checkOperationTx(request: HttpServletRequest) {
        val jwt = request.extractAccessJWT()
        val platformId = jwt.getPlatformId()
        val operationId = request.extractOperationId()
        val operationTX = getOperationTX(operationId)
        if (operationTX.platformId != platformId) {
            throw OperationIdNotFoundException(message = "Operation not found.")
        }
    }

    private fun DecodedJWT.getPlatformId(): UUID {
        val platformId = this.getClaim(CLAIM_NAME_PLATFORM_ID)
        if (platformId.isNull) {
            throw MissingPlatformIdException(message = "Missing platform id.")
        }
        mdc(MDCKey.PLATFORM_ID, platformId.asString())
        return try {
            UUID.fromString(platformId.asString())
        } catch (ex: Exception) {
            throw InvalidPlatformIdException(message = "Invalid platform id.", cause = ex)
        }
    }

    private fun OperationTX.persist() = try {
        operationDao.persistOperationTX(this)
    } catch (ex: Exception) {
        throw PersistenceException(message = "Error writing to database.", cause = ex)
    }

    private fun getOperationTX(operationId: UUID): OperationTX = try {
        operationDao.getOperationTX(operationId)
    } catch (ex: Exception) {
        throw ReadException(message = "Error read from database.", cause = ex)
    } ?: throw OperationIdNotFoundException(message = "Operation not found.")
}