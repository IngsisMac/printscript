package com.printscript.parser

import com.printscript.common.Version
import com.printscript.parser.expression.prefix.BooleanLiteralPrefixParser
import com.printscript.parser.expression.prefix.NumberLiteralPrefixParser
import com.printscript.parser.expression.prefix.PrefixParser
import com.printscript.parser.expression.prefix.ReadFunctionPrefixParser
import com.printscript.parser.expression.prefix.StringLiteralPrefixParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ExpressionParserFactoryTest {
    private lateinit var prefixV10: List<PrefixParser>
    private lateinit var prefixV11: List<PrefixParser>

    @BeforeEach
    fun setUp() {
        prefixV10 = ExpressionParserFactory.createPrefixParsers(Version.V1_0)
        prefixV11 = ExpressionParserFactory.createPrefixParsers(Version.V1_1)
    }

    @Test
    @DisplayName("ExpressionParserFactory incluye los prefix parsers esperados para la versión 1.0")
    fun expressionParserFactoryVersion10CreatesPrefixParsers() {
        assertTrue(prefixV10.any { it is NumberLiteralPrefixParser })
        assertTrue(prefixV10.any { it is StringLiteralPrefixParser })
    }

    @Test
    @DisplayName("ExpressionParserFactory incluye los prefix parsers esperados para la versión 1.1")
    fun expressionParserFactoryVersion11CreatesPrefixParsers() {
        assertTrue(prefixV11.any { it is BooleanLiteralPrefixParser })
        assertTrue(prefixV11.any { it is ReadFunctionPrefixParser })
    }

    @Test
    @DisplayName("ExpressionParserFactory crea los infix parsers para operaciones binarias")
    fun expressionParserFactoryCreatesInfixParsers() {
        val infixParsers = ExpressionParserFactory.createInfixParsers()
        assertEquals(4, infixParsers.size)
    }
}
