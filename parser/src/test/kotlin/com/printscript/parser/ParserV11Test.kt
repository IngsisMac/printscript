package com.printscript.parser

import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.token.Token
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserV11Test {
    private val defaultSpan = Span(Position(1, 1), Position(1, 10))

    private fun token(
        type: TokenType,
        lexeme: String,
    ): Token = Token(type, lexeme, defaultSpan)

    // PS-PAR-014 — const es válido en 1.1
    @Test
    fun `const is valid in version 1 1`() {
        val tokens =
            listOf(
                token(TokenType.CONST, "const"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.COLON, ":"),
                token(TokenType.NUMBER, "number"),
                token(TokenType.EQUAL, "="),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val decl = parser.parse().next() as Declaration
        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertTrue(decl.isConst)
        assertEquals("5", (decl.value as NumberLiteral).value)
    }

    // PS-PAR-015 — if con else produce el árbol esperado
    @Test
    fun `if with else parses correctly in version 1 1`() {
        val tokens =
            listOf(
                token(TokenType.IF, "if"),
                token(TokenType.LPAREN, "("),
                token(TokenType.IDENTIFIER, "flag"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.LBRACE, "{"),
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "si"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.RBRACE, "}"),
                token(TokenType.ELSE, "else"),
                token(TokenType.LBRACE, "{"),
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "no"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.RBRACE, "}"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val ifStmt = parser.parse().next() as IfStatement

        assertEquals("flag", (ifStmt.condition as Variable).name)
        assertEquals(1, ifStmt.thenBranch.size)
        assertEquals("si", ((ifStmt.thenBranch[0] as PrintStatement).expression as StringLiteral).value)

        assertNotNull(ifStmt.elseBranch)
        assertEquals(1, ifStmt.elseBranch!!.size)
        assertEquals("no", ((ifStmt.elseBranch!![0] as PrintStatement).expression as StringLiteral).value)
    }

    // PS-PAR-016 — else if no está soportado
    @Test
    fun `else if is not supported in version 1 1`() {
        val tokens =
            listOf(
                token(TokenType.IF, "if"),
                token(TokenType.LPAREN, "("),
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.LBRACE, "{"),
                token(TokenType.RBRACE, "}"),
                token(TokenType.ELSE, "else"),
                token(TokenType.IF, "if"),
                token(TokenType.LPAREN, "("),
                token(TokenType.IDENTIFIER, "b"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.LBRACE, "{"),
                token(TokenType.RBRACE, "}"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val ex = assertThrows<ParseException> { parser.parse().next() }
        assertTrue(ex.rawMessage.contains("else if is not supported"))
    }

    // Support readInput function call expression
    @Test
    fun `parse readInput expression`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "name"),
                token(TokenType.COLON, ":"),
                token(TokenType.STRING, "string"),
                token(TokenType.EQUAL, "="),
                token(TokenType.READ_INPUT, "readInput"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "enter name:"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val decl = parser.parse().next() as Declaration
        val call = decl.value as CallExpression
        assertEquals("readInput", call.name)
        assertEquals("enter name:", (call.argument as StringLiteral).value)
    }

    // Support readEnv function call expression
    @Test
    fun `parse readEnv expression`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "path"),
                token(TokenType.COLON, ":"),
                token(TokenType.STRING, "string"),
                token(TokenType.EQUAL, "="),
                token(TokenType.READ_ENV, "readEnv"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "PATH"),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val decl = parser.parse().next() as Declaration
        val call = decl.value as CallExpression
        assertEquals("readEnv", call.name)
        assertEquals("PATH", (call.argument as StringLiteral).value)
    }

    // Support boolean declarations and literals
    @Test
    fun `parse boolean declaration and true literal`() {
        val tokens =
            listOf(
                token(TokenType.LET, "let"),
                token(TokenType.IDENTIFIER, "isOk"),
                token(TokenType.COLON, ":"),
                token(TokenType.BOOLEAN, "boolean"),
                token(TokenType.EQUAL, "="),
                token(TokenType.TRUE, "true"),
                token(TokenType.SEMICOLON, ";"),
            ).iterator()

        val parser = Parser(tokens, Version.V1_1)
        val decl = parser.parse().next() as Declaration
        assertEquals("boolean", decl.type)
        assertEquals(true, (decl.value as BooleanLiteral).value)
    }
}
