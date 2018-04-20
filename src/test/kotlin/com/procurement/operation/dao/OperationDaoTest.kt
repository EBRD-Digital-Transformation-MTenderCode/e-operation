package com.procurement.operation.dao

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.Statement
import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.querybuilder.Insert
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.operation.OPERATION_ID
import com.procurement.operation.OPERATION_TX
import com.procurement.operation.PLATFORM_ID
import com.procurement.operation.exception.UnknownOperationException
import com.procurement.operation.exception.database.ReadOperationException
import com.procurement.operation.exception.database.SaveOperationException
import com.procurement.operation.model.OperationTX
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

class OperationDaoTest {
    private lateinit var session: Session
    private lateinit var dao: OperationDao

    @BeforeEach
    fun init() {
        session = mock()
        dao = OperationDaoImpl(session)
    }

    @Nested
    inner class SaveOperation {
        @Test
        @DisplayName("The saving operation was successful")
        fun persistOperation() {
            val resultSet: ResultSet = mock()
            whenever(resultSet.wasApplied())
                .thenReturn(true)
            whenever(session.execute(any<Statement>()))
                .thenReturn(resultSet)

            dao.persistOperationTX(OPERATION_TX)

            val insertCapture = argumentCaptor<Insert>()
            verify(session, times(1))
                .execute(insertCapture.capture())
            persistOperationCheckSql(insertCapture)
        }

        private fun persistOperationCheckSql(insertCapture: KArgumentCaptor<Insert>) {
            val template =
                "INSERT INTO ocds.operations (id,platform_id) VALUES ($OPERATION_ID,$PLATFORM_ID) IF NOT EXISTS;"
            assertEquals(template, insertCapture.firstValue.toString())
        }

        @Test
        @DisplayName("Error writing to the database")
        fun errorPersist() {
            doThrow(RuntimeException::class)
                .whenever(session)
                .execute(any<Insert>())

            assertEquals(
                "Error writing to the database.",
                assertThrows(
                    SaveOperationException::class.java,
                    {
                        dao.persistOperationTX(OPERATION_TX)
                    }
                ).message
            )
        }

        @Test
        @DisplayName("An error occurred when inserting a record in the database")
        fun notApplied() {
            val resultSet: ResultSet = mock()
            whenever(resultSet.wasApplied())
                .thenReturn(false)
            whenever(session.execute(any<Insert>()))
                .thenReturn(resultSet)

            assertEquals(
                "An error occurred when inserting a record with the operation id: '${OPERATION_TX.id}' and  the platform id: '$PLATFORM_ID' in the database.",
                assertThrows(
                    SaveOperationException::class.java,
                    {
                        dao.persistOperationTX(OPERATION_TX)
                    }
                ).message
            )
        }
    }

    @Nested
    inner class GetOperation {
        @Test
        @DisplayName("Testing the method checkOperationTX")
        fun checkOperationTX() {
            val row = mock<Row>()
            val resultSet: ResultSet = mock()

            whenever(row.getUUID("id"))
                .thenReturn(OPERATION_ID)
            whenever(row.getUUID("platform_id"))
                .thenReturn(PLATFORM_ID)
            whenever(resultSet.one())
                .thenReturn(row)
            whenever(session.execute(any<Statement>()))
                .thenReturn(resultSet)

            val result: OperationTX = dao.getOperationTX(OPERATION_ID)

            assertEquals(OPERATION_ID, result.id)
            assertEquals(PLATFORM_ID, result.platformId)

            val selectCapture = argumentCaptor<Statement>()
            verify(session, times(1))
                .execute(selectCapture.capture())
            getOperationCheckSql(selectCapture)
        }

        private fun getOperationCheckSql(selectCapture: KArgumentCaptor<Statement>) {
            val template = "SELECT * FROM ocds.operations WHERE id=$OPERATION_ID;"
            assertEquals(template, selectCapture.firstValue.toString())
        }

        @Test
        @DisplayName("Error read from the database")
        fun errorRead() {
            doThrow(RuntimeException::class)
                .whenever(session)
                .execute(any<BuiltStatement>())

            assertEquals(
                "Error read from the database.",
                assertThrows(
                    ReadOperationException::class.java,
                    {
                        dao.getOperationTX(OPERATION_ID)
                    }
                ).message
            )
        }

        @Test
        @DisplayName("The unknown operation")
        fun unknownOperation() {
            val resultSet: ResultSet = mock()

            whenever(resultSet.one())
                .thenReturn(null)
            whenever(session.execute(any<BuiltStatement>()))
                .thenReturn(resultSet)

            assertEquals(
                "The operation with id: '$OPERATION_ID' not found.",
                assertThrows(
                    UnknownOperationException::class.java,
                    {
                        dao.getOperationTX(OPERATION_ID)
                    }
                ).message
            )
        }
    }
}