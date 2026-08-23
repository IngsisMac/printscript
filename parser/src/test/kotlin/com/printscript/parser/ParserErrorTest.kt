package com.printscript.parser

import com.printscript.common.Version
import com.printscript.token.TokenFactory
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserErrorTest {
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        version = Version.V1_0
    }

    @Test
    @DisplayName("Falta el punto y coma lanza ParseException")
    fun missingSemicolonThrowsParseException() {
        val tokens =
            TokenFactory
                .tokens(
                    TokenType.LET,
                    TokenType.IDENTIFIER to "x",
                    TokenType.COLON,
                    TokenType.NUMBER,
                    TokenType.EQUAL,
                    TokenType.NUMBER_LITERAL to "5",
                ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected SEMICOLON"))
    }

    @Test
    @DisplayName("Paréntesis sin cerrar en println lanza ParseException")
    fun unclosedParenthesisInPrintlnThrowsParseException() {
        val tokens =
            TokenFactory
                .tokens(
                    TokenType.PRINTLN,
                    TokenType.LPAREN,
                    TokenType.STRING_LITERAL to "hola",
                    TokenType.SEMICOLON,
                ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected RPAREN"))
    }

    @Test
    @DisplayName("Token inesperado al inicio de la sentencia lanza ParseException")
    fun unexpectedTokenAtStatementStartThrowsParseException() {
        val tokens =
            TokenFactory
                .tokens(
                    TokenType.PLUS,
                    TokenType.NUMBER_LITERAL to "5",
                    TokenType.SEMICOLON,
                ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Unexpected token"))
    }

    @Test
    @DisplayName("Falta de dos puntos en declaración lanza ParseException")
    fun missingColonInDeclarationThrowsParseException() {
        val tokens =
            TokenFactory
                .tokens(
                    TokenType.LET,
                    TokenType.IDENTIFIER to "x",
                    TokenType.NUMBER,
                    TokenType.SEMICOLON,
                ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected COLON"))
    }
}
