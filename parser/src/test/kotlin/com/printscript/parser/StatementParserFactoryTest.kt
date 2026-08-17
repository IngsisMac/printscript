package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.statement.AssignmentStatementParser
import com.printscript.parser.statement.DeclarationStatementParser
import com.printscript.parser.statement.IfStatementParser
import com.printscript.parser.statement.PrintStatementParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StatementParserFactoryTest {
    private lateinit var version10Parsers: List<com.printscript.parser.statement.StatementParser>
    private lateinit var version11Parsers: List<com.printscript.parser.statement.StatementParser>

    @BeforeEach
    fun setUp() {
        version10Parsers = StatementParserFactory.create(Version.V1_0)
        version11Parsers = StatementParserFactory.create(Version.V1_1)
    }

    @Test
    @DisplayName("StatementParserFactory para la versión 1.0 incluye 3 parsers de sentencia")
    fun statementParserFactoryVersion10CreatesThreeParsers() {
        assertEquals(3, version10Parsers.size)
        assertTrue(version10Parsers.any { it is DeclarationStatementParser })
        assertTrue(version10Parsers.any { it is AssignmentStatementParser })
        assertTrue(version10Parsers.any { it is PrintStatementParser })
    }

    @Test
    @DisplayName("StatementParserFactory para la versión 1.1 incluye 4 parsers de sentencia")
    fun statementParserFactoryVersion11CreatesFourParsers() {
        assertEquals(4, version11Parsers.size)
        assertTrue(version11Parsers.any { it is IfStatementParser })
    }
}
