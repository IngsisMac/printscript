package com.printscript.runner.integration

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.common.Version
import com.printscript.lexer.Lexer
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader

class LexerParserIntegrationTest {
    private lateinit var validV10Code: String
    private lateinit var validV11Code: String
    private lateinit var operatorPrecedenceCode: String
    private lateinit var emptyCode: String
    private lateinit var syntaxErrorCases: List<String>

    @BeforeEach
    fun setUp() {
        validV10Code = """
            let x: number = 42;
            x = 100;
        """.trimIndent()

        validV11Code = """
            const isReady: boolean = true;
            if (isReady) {
                println("Ready!");
            } else {
                println("Waiting...");
            }
        """.trimIndent()

        operatorPrecedenceCode = "println(5 + 3 * 2);"
        emptyCode = "   \n\t  "

        syntaxErrorCases = listOf(
            "let x: number = ;",
            "let : number = 5;",
            "if (true) { println(1); ",
            "println(;"
        )
    }

    @Test
    @DisplayName("Integración lexer y parser - declaración y asignación válidas en versión 1.0")
    fun validV10DeclarationAndAssignment() {
        val lexer = Lexer(StringReader(validV10Code), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(2, statements.size)
        val decl = statements[0] as Declaration
        assertEquals("x", decl.name)
        assertEquals("number", decl.type)
        assertTrue(decl.value is NumberLiteral)
        assertEquals("42", (decl.value as NumberLiteral).value)

        val assign = statements[1] as Assignment
        assertEquals("x", assign.name)
        assertTrue(assign.value is NumberLiteral)
        assertEquals("100", (assign.value as NumberLiteral).value)
    }

    @Test
    @DisplayName("Integración lexer y parser - precedencia de operaciones binarias")
    fun binaryOperationPrecedence() {
        val lexer = Lexer(StringReader(operatorPrecedenceCode), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val printStmt = statements[0] as PrintStatement
        val binOp = printStmt.expression as BinaryOp
        assertEquals("+", binOp.operator)
        assertEquals("5", (binOp.left as NumberLiteral).value)

        val rightBinOp = binOp.right as BinaryOp
        assertEquals("*", rightBinOp.operator)
        assertEquals("3", (rightBinOp.left as NumberLiteral).value)
        assertEquals("2", (rightBinOp.right as NumberLiteral).value)
    }

    @Test
    @DisplayName("Integración lexer y parser - const y sentencia if válidos en versión 1.1")
    fun validV11ConstAndIfStatement() {
        val lexer = Lexer(StringReader(validV11Code), Version.V1_1)
        val parser = Parser(lexer, Version.V1_1)
        val statements = parser.parse().asSequence().toList()

        assertEquals(2, statements.size)
        val decl = statements[0] as Declaration
        assertEquals("isReady", decl.name)
        assertEquals("boolean", decl.type)
        assertTrue(decl.isConst)

        val ifStmt = statements[1] as IfStatement
        assertEquals(1, ifStmt.thenBranch.size)
        assertNotNull(ifStmt.elseBranch)
        assertEquals(1, ifStmt.elseBranch!!.size)

        val thenPrint = ifStmt.thenBranch[0] as PrintStatement
        assertEquals("Ready!", (thenPrint.expression as StringLiteral).value)
    }

    @Test
    @DisplayName("Integración lexer y parser - llamada a función readInput en versión 1.1")
    fun v11ReadInputFunctionCall() {
        val code = "let name: string = readInput(\"Enter your name:\");"
        val lexer = Lexer(StringReader(code), Version.V1_1)
        val parser = Parser(lexer, Version.V1_1)
        val statements = parser.parse().asSequence().toList()

        assertEquals(1, statements.size)
        val decl = statements[0] as Declaration
        assertEquals("name", decl.name)
        val call = decl.value as CallExpression
        assertEquals("readInput", call.name)
        assertEquals("Enter your name:", (call.argument as StringLiteral).value)
    }

    @Test
    @DisplayName("Integración lexer y parser - fuente vacía retorna cero sentencias")
    fun emptySourceYieldsZeroStatements() {
        val lexer = Lexer(StringReader(emptyCode), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse().asSequence().toList()

        assertTrue(statements.isEmpty(), "Expected empty statement list for empty code input")
    }

    @Test
    @DisplayName("Integración lexer y parser - sintaxis inválida lanza ParseException")
    fun invalidSyntaxThrowsParseException() {
        for (invalidCode in syntaxErrorCases) {
            val lexer = Lexer(StringReader(invalidCode), Version.V1_1)
            val parser = Parser(lexer, Version.V1_1)

            assertThrows<ParseException>("Expected ParseException for code: '$invalidCode'") {
                parser.parse().asSequence().toList()
            }
        }
    }
}
