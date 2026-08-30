package com.printscript.linter.rule

import com.printscript.ast.BinaryOp
import com.printscript.ast.Declaration
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.linter.LinterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class NoUnusedVariablesRuleTest {
    private lateinit var rule: NoUnusedVariablesRule
    private lateinit var span1: Span
    private lateinit var span2: Span

    @BeforeEach
    fun setUp() {
        rule = NoUnusedVariablesRule()
        span1 = Span(Position(1, 1), Position(1, 20))
        span2 = Span(Position(2, 1), Position(2, 20))
    }

    @Test
    @DisplayName("Variable declarada y utilizada en println no genera errores")
    fun variableDeclaradaYUtilizadaEnPrintlnNoGeneraErrores() {
        val decl = Declaration("total", "number", NumberLiteral("10", span1), span1)
        val print = PrintStatement(Variable("total", span2), span2)
        val config = LinterConfig(noUnusedVariables = true)

        rule.check(decl, config)
        rule.check(print, config)
        val errors = rule.finish(config)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Variable declarada y utilizada en operacion binaria no genera errores")
    fun variableDeclaradaYUtilizadaEnOperacionBinariaNoGeneraErrores() {
        val declA = Declaration("a", "number", NumberLiteral("5", span1), span1)
        val declB =
            Declaration("b", "number", BinaryOp(Variable("a", span2), "+", NumberLiteral("1", span2), span2), span2)
        val print = PrintStatement(Variable("b", span2), span2)
        val config = LinterConfig(noUnusedVariables = true)

        rule.check(declA, config)
        rule.check(declB, config)
        rule.check(print, config)
        val errors = rule.finish(config)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Variable declarada que nunca es utilizada genera error al finalizar")
    fun variableDeclaradaNuncaUtilizadaGeneraError() {
        val decl = Declaration("unusedVar", "string", NumberLiteral("1", span1), span1)
        val config = LinterConfig(noUnusedVariables = true)

        rule.check(decl, config)
        val errors = rule.finish(config)

        assertEquals(1, errors.size)
        assertEquals("Variable 'unusedVar' is declared but never used", errors.first().message)
        assertEquals(span1, errors.first().span)
    }

    @Test
    @DisplayName("Regla desactivada no genera errores aunque haya variables no utilizadas")
    fun reglaDesactivadaNoGeneraErrores() {
        val decl = Declaration("unusedVar", "number", NumberLiteral("1", span1), span1)
        val config = LinterConfig(noUnusedVariables = false)

        rule.check(decl, config)
        val errors = rule.finish(config)

        assertTrue(errors.isEmpty())
    }
}
