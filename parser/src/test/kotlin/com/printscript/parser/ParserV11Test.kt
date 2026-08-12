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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserV11Test {
    private lateinit var defaultSpan: Span
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        defaultSpan = Span(Position(1, 1), Position(1, 10))
        version = Version.V1_1
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
    @DisplayName("const es válido en versión 1.1")
    fun constIsValidInVersion11() {
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

        val parser = Parser(tokens, version)
        val decl = parser.parse().next() as Declaration

        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertTrue(decl.isConst)
        assertEquals("5", (decl.value as NumberLiteral).value)
    }

    @Test
    @DisplayName("if con else produce el árbol esperado")
    fun ifWithElseParsesCorrectlyInVersion11() {
        val code = "if (flag) { println(\"si\"); } else { println(\"no\"); }"

        val ifStmt = parseString(code)[0] as IfStatement

        assertEquals("flag", (ifStmt.condition as Variable).name)
        assertEquals(1, ifStmt.thenBranch.size)
        assertEquals("si", ((ifStmt.thenBranch[0] as PrintStatement).expression as StringLiteral).value)

        assertNotNull(ifStmt.elseBranch)
        assertEquals(1, ifStmt.elseBranch!!.size)
        assertEquals("no", ((ifStmt.elseBranch!![0] as PrintStatement).expression as StringLiteral).value)
    }

    @Test
    @DisplayName("else if no está soportado")
    fun elseIfIsNotSupportedInVersion11() {
        val code = "if (a) {} else if (b) {}"

        val ex = assertThrows<ParseException> { parseString(code) }

        assertTrue(ex.rawMessage.contains("else if is not supported"))
    }

    @Test
    @DisplayName("Parse de expresión readInput")
    fun parseReadInputExpression() {
        val decl = parseString("let name: string = readInput(\"enter name:\");")[0] as Declaration

        val call = decl.value as CallExpression

        assertEquals("readInput", call.name)
        assertEquals("enter name:", (call.argument as StringLiteral).value)
    }

    @Test
    @DisplayName("Parse de expresión readEnv")
    fun parseReadEnvExpression() {
        val decl = parseString("let path: string = readEnv(\"PATH\");")[0] as Declaration

        val call = decl.value as CallExpression

        assertEquals("readEnv", call.name)
        assertEquals("PATH", (call.argument as StringLiteral).value)
    }

    @Test
    @DisplayName("Parse de declaración boolean y literal true")
    fun parseBooleanDeclarationAndTrueLiteral() {
        val decl = parseString("let isOk: boolean = true;")[0] as Declaration

        assertEquals("boolean", decl.type)
        assertEquals(true, (decl.value as BooleanLiteral).value)
    }
}
