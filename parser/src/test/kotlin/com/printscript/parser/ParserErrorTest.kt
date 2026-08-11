package com.printscript.parser

import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserErrorTest {
    private val defaultSpan = Span(Position(1, 1), Position(1, 10))

    private fun token(
        type: TokenType,
        lexeme: String,
        span: Span = defaultSpan,
    ): Token = Token(type, lexeme, span)

    // PS-PAR-008 — Falta el punto y coma
    @Test
    fun `missing semicolon throws ParseException`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("Expected SEMICOLON"))
    }

    // PS-PAR-009 — Paréntesis sin cerrar
    @Test
    fun `unclosed parenthesis in println throws ParseException`() {
        val tokens =
            listOf(
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "hola"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("Expected RPAREN"))
    }

    @Test
    fun `unexpected token at statement start throws ParseException`() {
        val tokens =
            listOf(
                token(TokenType.PLUS, "+"),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("Unexpected token"))
    }

    @Test
    fun `missing colon in declaration throws ParseException`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("Expected COLON"))
    }
}
