package com.printscript.linter.rule

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.NumberLiteral
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.linter.IdentifierFormat
import com.printscript.linter.LinterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class IdentifierFormatRuleTest {
    private lateinit var rule: IdentifierFormatRule
    private lateinit var span: Span
    private lateinit var camelCaseConfig: LinterConfig
    private lateinit var snakeCaseConfig: LinterConfig
    private lateinit var noneConfig: LinterConfig

    @BeforeEach
    fun setUp() {
        rule = IdentifierFormatRule()
        span = Span(Position(1, 1), Position(1, 20))
        camelCaseConfig = LinterConfig(identifierFormat = IdentifierFormat.CAMEL_CASE)
        snakeCaseConfig = LinterConfig(identifierFormat = IdentifierFormat.SNAKE_CASE)
        noneConfig = LinterConfig(identifierFormat = IdentifierFormat.NONE)
    }

    @Test
    @DisplayName("Declaración con camelCase válido no genera errores bajo configuración camelCase")
    fun declarationWithValidCamelCaseProducesNoErrors() {
        val declaration = Declaration("myVariable", "number", NumberLiteral("5", span), span)

        val errors = rule.check(declaration, camelCaseConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Declaración con snake_case genera error bajo configuración camelCase")
    fun declarationWithSnakeCaseFailsCamelCaseConfig() {
        val declaration = Declaration("my_variable", "number", NumberLiteral("5", span), span)

        val errors = rule.check(declaration, camelCaseConfig)

        assertEquals(1, errors.size)
        assertEquals("Identifier 'my_variable' does not conform to camelCase format", errors[0].message)
        assertEquals(span, errors[0].span)
    }

    @Test
    @DisplayName("Asignación con snake_case válido no genera errores bajo configuración snake_case")
    fun assignmentWithValidSnakeCaseProducesNoErrors() {
        val assignment = Assignment("my_variable", NumberLiteral("5", span), span)

        val errors = rule.check(assignment, snakeCaseConfig)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Asignación con camelCase genera error bajo configuración snake_case")
    fun assignmentWithCamelCaseFailsSnakeCaseConfig() {
        val assignment = Assignment("myVariable", NumberLiteral("5", span), span)

        val errors = rule.check(assignment, snakeCaseConfig)

        assertEquals(1, errors.size)
        assertEquals("Identifier 'myVariable' does not conform to snake_case format", errors[0].message)
    }

    @Test
    @DisplayName("Configuración NONE no reporta errores para ningún nombre de variable")
    fun noneConfigProducesNoErrors() {
        val declaration = Declaration("MY_VARIABLE", "number", NumberLiteral("5", span), span)

        val errors = rule.check(declaration, noneConfig)

        assertTrue(errors.isEmpty())
    }
}
