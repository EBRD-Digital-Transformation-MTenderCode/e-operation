package com.procurement.operation.dao

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.querybuilder.Insert
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.eq
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.exception.database.ReadOperationException
import com.procurement.operation.exception.database.SaveOperationException
import com.procurement.operation.model.OperationTX
import java.util.*

interface OperationDao {
    fun persistOperationTX(operation: OperationTX)

    fun getOperationTX(operationId: UUID): OperationTX
}

class OperationDaoImpl(private val session: Session) : OperationDao {
    companion object {
        private const val KEY_SPACE = "ocds"
        private const val OPERATION_TABLE = "operations"
        private const val OPERATION_ID_FIELD = "id"
        private const val PLATFORM_ID_FIELD = "platform_id"
    }

    override fun persistOperationTX(operation: OperationTX) {
        val insert = QueryBuilder.insertInto(KEY_SPACE, OPERATION_TABLE)
            .also {
                it.value(OPERATION_ID_FIELD, operation.id)
                it.value(PLATFORM_ID_FIELD, operation.platformId)
                it.ifNotExists()
            }

        val resultSet = save(insert)

        if (!resultSet.wasApplied()) {
            throw SaveOperationException(message = "An error occurred when inserting a record with the operation id: '${operation.id}' and  the platform id: '${operation.platformId}' in the database.")
        }
    }

    private fun save(insert: Insert) = try {
        session.execute(insert)
    } catch (ex: Exception) {
        throw SaveOperationException(message = "Error writing to the database.", cause = ex)
    }

    override fun getOperationTX(operationId: UUID): OperationTX {
        val select = QueryBuilder.select()
            .from(KEY_SPACE, OPERATION_TABLE)
            .where(eq(OPERATION_ID_FIELD, operationId))

        return load(select)
            ?.let { toOperationTX(it) }
            ?: throw UnknownOperationException(message = "The operation with id: '$operationId' not found.")
    }

    private fun load(statement: BuiltStatement) = try {
        session.execute(statement).one()
    } catch (ex: Exception) {
        throw ReadOperationException(message = "Error read from the database.", cause = ex)
    }

    private fun toOperationTX(row: Row) = OperationTX(
        id = row.getUUID(OPERATION_ID_FIELD),
        platformId = row.getUUID(PLATFORM_ID_FIELD)
    )
}