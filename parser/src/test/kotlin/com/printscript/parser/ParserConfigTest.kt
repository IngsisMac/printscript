package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.statement.IfStatementParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ParserConfigTest {
    private lateinit var configV10: ParserConfig
    private lateinit var configV11: ParserConfig

    @BeforeEach
    fun setUp() {
        configV10 = ParserConfig.from(Version.V1_0)
        configV11 = ParserConfig.from(Version.V1_1)
    }

    @Test
    @DisplayName("La configuración de la versión 1.0 excluye el parser de sentencias condicionales")
    fun version10ConfigExcludesIfStatementParser() {
        val hasIfParser = configV10.statementParsers.any { it is IfStatementParser }

        assertFalse(hasIfParser)
        assertEquals(Version.V1_0, configV10.version)
    }

    @Test
    @DisplayName("La configuración de la versión 1.1 incluye el parser de sentencias condicionales")
    fun version11ConfigIncludesIfStatementParser() {
        val hasIfParser = configV11.statementParsers.any { it is IfStatementParser }

        assertTrue(hasIfParser)
        assertEquals(Version.V1_1, configV11.version)
    }
}
