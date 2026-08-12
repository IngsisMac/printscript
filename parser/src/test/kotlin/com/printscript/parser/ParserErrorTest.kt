package com.printscript.parser

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserErrorTest {
    private lateinit var defaultSpan: Span
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        defaultSpan = Span(Position(1, 1), Position(1, 10))
        version = Version.V1_0
    }

    private fun token(
        type: TokenType,
        lexeme: String,
        span: Span = defaultSpan,
    ): Token = Token(type, lexeme, span)

    @Test
    @DisplayName("Falta el punto y coma lanza ParseException")
    fun missingSemicolonThrowsParseException() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
            ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected SEMICOLON"))
    }

    @Test
    @DisplayName("Paréntesis sin cerrar en println lanza ParseException")
    fun unclosedParenthesisInPrintlnThrowsParseException() {
        val tokens =
            listOf(
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "hola"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected RPAREN"))
    }

    @Test
    @DisplayName("Token inesperado al inicio de la sentencia lanza ParseException")
    fun unexpectedTokenAtStatementStartThrowsParseException() {
        val tokens =
            listOf(
                token(TokenType.PLUS, "+"),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Unexpected token"))
    }

    @Test
    @DisplayName("Falta de dos puntos en declaración lanza ParseException")
    fun missingColonInDeclarationThrowsParseException() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("Expected COLON"))
    }
}
