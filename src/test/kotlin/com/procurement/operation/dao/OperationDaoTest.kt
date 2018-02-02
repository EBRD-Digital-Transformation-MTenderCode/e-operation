package com.procurement.operation.dao

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.Statement
import com.datastax.driver.core.querybuilder.Insert
import com.nhaarman.mockito_kotlin.*
import com.procurement.operation.model.OperationTX
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class OperationDaoTest {
    companion object {
        private val OPERATION_ID = UUID.randomUUID()
        private val PLATFORM_ID = UUID.randomUUID()
        private val OPERATION_TX = OperationTX(id = OPERATION_ID, platformId = PLATFORM_ID)
    }

    private lateinit var session: Session
    private lateinit var dao: OperationDao

    @BeforeEach
    fun init() {
        session = mock()
        dao = OperationDaoImpl(session)
    }

    @Test
    @DisplayName("Testing the method persistOperationTX.")
    fun persistOperationTX() {
        val resultSet: ResultSet = mock()
        whenever(session.execute(any<Statement>()))
            .thenReturn(resultSet)

        dao.persistOperationTX(OPERATION_TX)

        val insertCapture = argumentCaptor<Insert>()
        verify(session, times(1))
            .execute(insertCapture.capture())
        persistOperationCheckSql(insertCapture)
    }

    private fun persistOperationCheckSql(insertCapture: KArgumentCaptor<Insert>) {
        val template = "INSERT INTO ocds.operations (id,platform_id) VALUES ($OPERATION_ID,$PLATFORM_ID) IF NOT EXISTS;"
        assertEquals(template, insertCapture.firstValue.toString())
    }

    @Test
    @DisplayName("Testing the method checkOperationTX.")
    fun checkOperationTX() {
        val row = mock<Row>()
        val resultSet: ResultSet = mock()

        whenever(session.execute(any<Statement>()))
            .thenReturn(resultSet)
        whenever(resultSet.one())
            .thenReturn(row)
        whenever(row.getUUID("id"))
            .thenReturn(OPERATION_ID)
        whenever(row.getUUID("platform_id"))
            .thenReturn(PLATFORM_ID)

        val result: OperationTX? = dao.getOperationTX(OPERATION_ID)

        assertNotNull(result)


        assertEquals(OPERATION_ID, result!!.id)
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
}