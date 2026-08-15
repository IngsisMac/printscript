package com.printscript.linter.rule

import com.printscript.ast.BinaryOp
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
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

class ReadInputExpressionRuleTest {
    private lateinit var rule: ReadInputExpressionRule
    private lateinit var span: Span
    private lateinit var enabledConfig: LinterConfig
    private lateinit var disabledConfig: LinterConfig

    @BeforeEach
    fun setUp() {
        rule = ReadInputExpressionRule()
        span = Span(Position(1, 1), Position(1, 30))
        enabledConfig = LinterConfig(mandatoryVariableOrLiteralInReadInput = true)
        disabledConfig = LinterConfig(mandatoryVariableOrLiteralInReadInput = false)
    }

    @Test
    @DisplayName("readInput con literal de texto como argumento es válido")
    fun readInputWithStringLiteralIsValid() {
        val call = CallExpression("readInput", StringLiteral("Name: ", span), span)
        val statement = Declaration("x", "string", call, span)

        val errors = rule.check(statement, enabledConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("readInput con variable como argumento es válido")
    fun readInputWithVariableIsValid() {
        val call = CallExpression("readInput", Variable("prompt", span), span)
        val statement = Declaration("x", "string", call, span)

        val errors = rule.check(statement, enabledConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("readInput con expresión binaria como argumento genera error cuando la regla está activada")
    fun readInputWithBinaryOpFailsWhenRuleEnabled() {
        val binaryOp = BinaryOp(StringLiteral("Prompt: ", span), "+", Variable("name", span), span)
        val call = CallExpression("readInput", binaryOp, span)
        val statement = Declaration("x", "string", call, span)

        val errors = rule.check(statement, enabledConfig)

        assertEquals(1, errors.size)
        assertEquals("readInput argument must be a simple variable or literal", errors[0].message)
        assertEquals(span, errors[0].span)
    }

    @Test
    @DisplayName("readInput con expresión binaria no genera error cuando la regla está desactivada")
    fun readInputWithBinaryOpProducesNoErrorsWhenRuleDisabled() {
        val binaryOp = BinaryOp(StringLiteral("Prompt: ", span), "+", Variable("name", span), span)
        val call = CallExpression("readInput", binaryOp, span)
        val statement = Declaration("x", "string", call, span)

        val errors = rule.check(statement, disabledConfig)

        assertTrue(errors.isEmpty())
    }
}
