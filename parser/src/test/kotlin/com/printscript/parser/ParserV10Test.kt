package com.printscript.parser

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.Declaration
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserV10Test {
    private val defaultSpan = Span(Position(1, 1), Position(1, 10))

    private fun token(
        type: TokenType,
        lexeme: String,
    ): Token = Token(type, lexeme, defaultSpan)

    // PS-PAR-001 — Declaración con inicialización produce un Declaration
    @Test
    fun `parse declaration with initialization`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val decl = statements[0] as Declaration
        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertFalse(decl.isConst)
        assertNotNull(decl.value)
        assertEquals("5", (decl.value as NumberLiteral).value)
    }

    // PS-PAR-002 — Declaración sin inicialización es válida
    @Test
    fun `parse declaration without initialization`() {
        val tokens =
            listOf(
                token(TokenType.LET, "x"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val decl = statements[0] as Declaration
        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertNull(decl.value)
    }

    // PS-PAR-003 — Asignación a variable ya declarada
    @Test
    fun `parse assignment`() {
        val tokens =
            listOf(
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val assign = statements[0] as Assignment
        assertEquals("x", assign.name)
        assertEquals("5", (assign.value as NumberLiteral).value)
    }

    // PS-PAR-004 — println produce un PrintStatement
    @Test
    fun `parse print statement`() {
        val tokens =
            listOf(
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "hola"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val printStmt = statements[0] as PrintStatement
        assertEquals("hola", (printStmt.expression as StringLiteral).value)
    }

    // PS-PAR-005 — La multiplicación liga más fuerte que la suma (2 + 3 * 4)
    @Test
    fun `parse expression multiplication higher precedence than addition`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "2"),
                token(TokenType.PLUS, "+"),
                token(TokenType.NUMBER_LITERAL, "3"),
                token(TokenType.STAR, "*"),
                token(TokenType.NUMBER_LITERAL, "4"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        val decl = statements[0] as Declaration
        val rootOp = decl.value as BinaryOp
        assertEquals("+", rootOp.operator)
        assertEquals("2", (rootOp.left as NumberLiteral).value)

        val rightOp = rootOp.right as BinaryOp
        assertEquals("*", rightOp.operator)
        assertEquals("3", (rightOp.left as NumberLiteral).value)
        assertEquals("4", (rightOp.right as NumberLiteral).value)
    }

    // PS-PAR-006 — Los operadores de igual precedencia asocian a izquierda (10 - 3 - 2)
    @Test
    fun `parse expression left associativity for equal precedence`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "10"),
                token(TokenType.MINUS, "-"),
                token(TokenType.NUMBER_LITERAL, "3"),
                token(TokenType.MINUS, "-"),
                token(TokenType.NUMBER_LITERAL, "2"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val decl = parser.parse().next() as Declaration

        val rootOp = decl.value as BinaryOp
        assertEquals("-", rootOp.operator)
        assertEquals("2", (rootOp.right as NumberLiteral).value)

        val leftOp = rootOp.left as BinaryOp
        assertEquals("-", leftOp.operator)
        assertEquals("10", (leftOp.left as NumberLiteral).value)
        assertEquals("3", (leftOp.right as NumberLiteral).value)
    }

    // PS-PAR-007 — Los paréntesis cambian la precedencia ((2 + 3) * 4)
    @Test
    fun `parse expression parentheses override precedence`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.LPAREN, "("),
                token(TokenType.NUMBER_LITERAL, "2"),
                token(TokenType.PLUS, "+"),
                token(TokenType.NUMBER_LITERAL, "3"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.STAR, "*"),
                token(TokenType.NUMBER_LITERAL, "4"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val decl = parser.parse().next() as Declaration

        val rootOp = decl.value as BinaryOp
        assertEquals("*", rootOp.operator)

        val leftOp = rootOp.left as BinaryOp
        assertEquals("+", leftOp.operator)
        assertEquals("2", (leftOp.left as NumberLiteral).value)
        assertEquals("3", (leftOp.right as NumberLiteral).value)
        assertEquals("4", (rootOp.right as NumberLiteral).value)
    }

    // PS-PAR-010 — const es inválido en 1.0
    @Test
    fun `const is invalid in version 1 0`() {
        val tokens =
            listOf(
                token(TokenType.IDENTIFIER, "const"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("const is not supported in version 1.0"))
    }

    // PS-PAR-011 — if es inválido en 1.0
    @Test
    fun `if is invalid in version 1 0`() {
        val tokens =
            listOf(
                token(TokenType.IDENTIFIER, "if"),
                token(TokenType.LPAREN, "("),
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.LBRACE, "{"),
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "hola"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.RBRACE, "}"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_0)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("if statements are not supported in version 1.0"))
    }
}
