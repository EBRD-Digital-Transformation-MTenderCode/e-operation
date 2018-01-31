package com.procurement.operation.dao

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.eq
import com.procurement.operation.model.OperationTX
import java.util.*

interface OperationDao {
    fun persistOperationTX(operation: OperationTX): Boolean

    fun getOperationTX(operationId: UUID): OperationTX?
}

class OperationDaoImpl(private val session: Session) : OperationDao {
    companion object {
        private const val KEY_SPACE = "ocds"
        private const val OPERATION_TABLE = "operations"
        private const val OPERATION_ID_FIELD = "id"
        private const val PLATFORM_ID_FIELD = "platform_id"
    }

    override fun persistOperationTX(operation: OperationTX): Boolean {
        val insert = QueryBuilder.insertInto(KEY_SPACE, OPERATION_TABLE).also {
            it.value(OPERATION_ID_FIELD, operation.id)
            it.value(PLATFORM_ID_FIELD, operation.platformId)
            it.ifNotExists()
        }

        val resultSet = session.execute(insert)
        return resultSet.wasApplied()
    }

    override fun getOperationTX(operationId: UUID): OperationTX? {
        val select = QueryBuilder.select()
            .from(KEY_SPACE, OPERATION_TABLE)
            .where(eq(OPERATION_ID_FIELD, operationId))

        return session.execute(select)
            .one()
            ?.let {
                OperationTX(
                    id = UUID.fromString(it.getString(OPERATION_ID_FIELD)),
                    platformId = UUID.fromString(it.getString(PLATFORM_ID_FIELD))
                )
            }
    }
}