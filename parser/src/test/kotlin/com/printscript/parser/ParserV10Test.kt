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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserV10Test {
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
    ): Token = Token(type, lexeme, defaultSpan)

    private fun parseString(source: String): List<com.printscript.ast.Statement> =
        Parser(com.printscript.lexer.Lexer(java.io.StringReader(source), version), version)
            .parse()
            .asSequence()
            .toList()

    @Test
    @DisplayName("PS-PAR-001 | Declaración con inicialización produce un Declaration")
    fun parseDeclarationWithInitialization() {
        val decl = parseString("let x: number = 5;")[0] as Declaration

        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertFalse(decl.isConst)
        assertEquals("5", (decl.value as NumberLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-002 | Declaración sin inicialización es válida")
    fun parseDeclarationWithoutInitialization() {
        val decl = parseString("let x: number;")[0] as Declaration

        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertNull(decl.value)
    }

    @Test
    @DisplayName("PS-PAR-003 | Asignación a variable ya declarada")
    fun parseAssignment() {
        val assign = parseString("x = 5;")[0] as Assignment

        assertEquals("x", assign.name)
        assertEquals("5", (assign.value as NumberLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-004 | println produce un PrintStatement")
    fun parsePrintStatement() {
        val printStmt = parseString("println(\"hola\");")[0] as PrintStatement

        assertEquals("hola", (printStmt.expression as StringLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-005 | La multiplicación liga más fuerte que la suma (2 + 3 * 4)")
    fun parseExpressionMultiplicationHigherPrecedenceThanAddition() {
        val decl = parseString("let x: number = 2 + 3 * 4;")[0] as Declaration

        val rootOp = decl.value as BinaryOp
        assertEquals("+", rootOp.operator)
        assertEquals("2", (rootOp.left as NumberLiteral).value)

        val rightOp = rootOp.right as BinaryOp
        assertEquals("*", rightOp.operator)
        assertEquals("3", (rightOp.left as NumberLiteral).value)
        assertEquals("4", (rightOp.right as NumberLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-006 | Los operadores de igual precedencia asocian a izquierda (10 - 3 - 2)")
    fun parseExpressionLeftAssociativityForEqualPrecedence() {
        val decl = parseString("let x: number = 10 - 3 - 2;")[0] as Declaration

        val rootOp = decl.value as BinaryOp
        assertEquals("-", rootOp.operator)
        assertEquals("2", (rootOp.right as NumberLiteral).value)

        val leftOp = rootOp.left as BinaryOp
        assertEquals("-", leftOp.operator)
        assertEquals("10", (leftOp.left as NumberLiteral).value)
        assertEquals("3", (leftOp.right as NumberLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-007 | Los paréntesis cambian la precedencia ((2 + 3) * 4)")
    fun parseExpressionParenthesesOverridePrecedence() {
        val decl = parseString("let x: number = (2 + 3) * 4;")[0] as Declaration

        val rootOp = decl.value as BinaryOp
        assertEquals("*", rootOp.operator)

        val leftOp = rootOp.left as BinaryOp
        assertEquals("+", leftOp.operator)
        assertEquals("2", (leftOp.left as NumberLiteral).value)
        assertEquals("3", (leftOp.right as NumberLiteral).value)
        assertEquals("4", (rootOp.right as NumberLiteral).value)
    }

    @Test
    @DisplayName("PS-PAR-010 | const es inválido en 1.0")
    fun constIsInvalidInVersion10() {
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
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("const is not supported in version 1.0"))
    }

    @Test
    @DisplayName("PS-PAR-011 | if es inválido en 1.0")
    fun ifIsInvalidInVersion10() {
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
        val parser = Parser(tokens, version)

        val ex = assertThrows<ParseException> { parser.parse().next() }

        assertTrue(ex.rawMessage.contains("if statements are not supported in version 1.0"))
    }
}
