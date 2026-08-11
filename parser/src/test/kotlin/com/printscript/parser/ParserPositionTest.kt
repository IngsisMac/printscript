package com.printscript.parser

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.Declaration
import com.printscript.ast.PrintStatement
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParserPositionTest {
    private fun pos(
        line: Int,
        col: Int,
    ): Position = Position(line, col)

    private fun span(
        startLine: Int,
        startCol: Int,
        endLine: Int,
        endCol: Int,
    ): Span = Span(pos(startLine, startCol), pos(endLine, endCol))

    private fun token(
        type: TokenType,
        lexeme: String,
        startCol: Int,
        endCol: Int,
    ): Token = Token(type, lexeme, span(1, startCol, 1, endCol))

    @Test
    fun `declaration span spans from start keyword to semicolon`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let", 1, 4),
                token(TokenType.IDENTIFIER, "x", 5, 6),
                token(TokenType.COLON, ":", 6, 7),
                token(TokenType.NUMBER, "number", 8, 14),
                token(TokenType.EQUAL, "=", 15, 16),
                token(TokenType.NUMBER_LITERAL, "5", 17, 18),
                token(TokenType.SEMICOLON, ";", 18, 19),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val decl = parser.parse().next() as Declaration

        assertEquals(pos(1, 1), decl.span.start)
        assertEquals(pos(1, 19), decl.span.end)
    }

    @Test
    fun `assignment span spans from identifier to semicolon`() {
        val tokens =
            listOf(
                token(TokenType.IDENTIFIER, "x", 3, 4),
                token(TokenType.EQUAL, "=", 5, 6),
                token(TokenType.NUMBER_LITERAL, "10", 7, 9),
                token(TokenType.SEMICOLON, ";", 9, 10),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val assign = parser.parse().next() as Assignment

        assertEquals(pos(1, 3), assign.span.start)
        assertEquals(pos(1, 10), assign.span.end)
    }

    @Test
    fun `print statement span spans from println to semicolon`() {
        val tokens =
            listOf(
                token(TokenType.PRINTLN, "println", 1, 8),
                token(TokenType.LPAREN, "(", 8, 9),
                token(TokenType.STRING_LITERAL, "hi", 9, 13),
                token(TokenType.RPAREN, ")", 13, 14),
                token(TokenType.SEMICOLON, ";", 14, 15),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val printStmt = parser.parse().next() as PrintStatement

        assertEquals(pos(1, 1), printStmt.span.start)
        assertEquals(pos(1, 15), printStmt.span.end)
    }

    @Test
    fun `binary op span spans from left operand start to right operand end`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let", 1, 4),
                token(TokenType.IDENTIFIER, "x", 5, 6),
                token(TokenType.COLON, ":", 6, 7),
                token(TokenType.NUMBER, "number", 8, 14),
                token(TokenType.EQUAL, "=", 15, 16),
                token(TokenType.NUMBER_LITERAL, "2", 17, 18),
                token(TokenType.PLUS, "+", 19, 20),
                token(TokenType.NUMBER_LITERAL, "3", 21, 22),
                token(TokenType.SEMICOLON, ";", 22, 23),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val decl = parser.parse().next() as Declaration
        val op = decl.value as BinaryOp

        assertEquals(pos(1, 17), op.span.start)
        assertEquals(pos(1, 22), op.span.end)
    }
}
