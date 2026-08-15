package com.printscript.linter

import com.printscript.ast.BinaryOp
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class DefaultLinterTest {
    private lateinit var linter: DefaultLinter
    private lateinit var span: Span

    @BeforeEach
    fun setUp() {
        linter = DefaultLinter()
        span = Span(Position(1, 1), Position(1, 20))
    }

    @Test
    @DisplayName("Análisis de secuencia de sentencias válidas no retorna errores")
    fun analyzeValidStatementsReturnsNoErrors() {
        val statements =
            listOf(
                Declaration("x", "number", NumberLiteral("5", span), span),
                PrintStatement(Variable("x", span), span),
            )
        val config = LinterConfig(identifierFormat = IdentifierFormat.CAMEL_CASE, mandatoryVariableOrLiteralInPrintln = true)

        val errors = linter.analyze(statements.iterator(), config)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Análisis acumula múltiples errores de diferentes reglas")
    fun analyzeAccumulatesMultipleErrors() {
        val statements =
            listOf(
                Declaration("my_variable", "number", NumberLiteral("5", span), span),
                PrintStatement(BinaryOp(Variable("a", span), "+", Variable("b", span), span), span),
            )
        val config = LinterConfig(identifierFormat = IdentifierFormat.CAMEL_CASE, mandatoryVariableOrLiteralInPrintln = true)

        val errors = linter.analyze(statements.iterator(), config)

        assertEquals(2, errors.size)
    }

    @Test
    @DisplayName("Análisis recursivo analiza sentencias dentro de bloques de un IfStatement")
    fun analyzeRecursesIntoIfStatementBranches() {
        val invalidThenStmt = Declaration("invalid_name", "number", NumberLiteral("1", span), span)
        val invalidElseStmt = PrintStatement(BinaryOp(Variable("a", span), "+", Variable("b", span), span), span)
        val ifStmt = IfStatement(Variable("cond", span), listOf(invalidThenStmt), listOf(invalidElseStmt), span)
        val config = LinterConfig(identifierFormat = IdentifierFormat.CAMEL_CASE, mandatoryVariableOrLiteralInPrintln = true)

        val errors = linter.analyze(listOf(ifStmt).iterator(), config)

        assertEquals(2, errors.size)
    }
}
