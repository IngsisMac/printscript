package com.printscript.linter.rule

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.linter.LinterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PrintlnExpressionRuleTest {
    private lateinit var rule: PrintlnExpressionRule
    private lateinit var span: Span
    private lateinit var enabledConfig: LinterConfig
    private lateinit var disabledConfig: LinterConfig

    @BeforeEach
    fun setUp() {
        rule = PrintlnExpressionRule()
        span = Span(Position(1, 1), Position(1, 20))
        enabledConfig = LinterConfig(mandatoryVariableOrLiteralInPrintln = true)
        disabledConfig = LinterConfig(mandatoryVariableOrLiteralInPrintln = false)
    }

    @Test
    @DisplayName("println con variable como argumento es válido")
    fun printlnWithVariableIsValid() {
        val statement = PrintStatement(Variable("x", span), span)

        val errors = rule.check(statement, enabledConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("println con literal de cadena como argumento es válido")
    fun printlnWithStringLiteralIsValid() {
        val statement = PrintStatement(StringLiteral("hello", span), span)

        val errors = rule.check(statement, enabledConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("println con literal numérico o booleano es válido")
    fun printlnWithNumberOrBooleanLiteralIsValid() {
        val stmt1 = PrintStatement(NumberLiteral("123", span), span)
        val stmt2 = PrintStatement(BooleanLiteral(true, span), span)

        assertTrue(rule.check(stmt1, enabledConfig).isEmpty())
        assertTrue(rule.check(stmt2, enabledConfig).isEmpty())
    }

    @Test
    @DisplayName("println con expresión binaria genera error cuando la regla está activada")
    fun printlnWithBinaryOpFailsWhenRuleEnabled() {
        val binaryOp = BinaryOp(Variable("a", span), "+", Variable("b", span), span)
        val statement = PrintStatement(binaryOp, span)

        val errors = rule.check(statement, enabledConfig)

        assertEquals(1, errors.size)
        assertEquals("println argument must be a simple variable or literal", errors[0].message)
        assertEquals(span, errors[0].span)
    }

    @Test
    @DisplayName("println con expresión binaria no genera error cuando la regla está desactivada")
    fun printlnWithBinaryOpProducesNoErrorsWhenRuleDisabled() {
        val binaryOp = BinaryOp(Variable("a", span), "+", Variable("b", span), span)
        val statement = PrintStatement(binaryOp, span)

        val errors = rule.check(statement, disabledConfig)

        assertTrue(errors.isEmpty())
    }
}
