package com.printscript.linter.rule

import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.StringLiteral
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.linter.LinterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class NoEmptyPrintlnRuleTest {
    private lateinit var rule: NoEmptyPrintlnRule
    private lateinit var dummySpan: Span

    @BeforeEach
    fun setUp() {
        rule = NoEmptyPrintlnRule()
        dummySpan = Span(Position(1, 1), Position(1, 15))
    }

    @Test
    @DisplayName("Println con string no vacío no genera errores")
    fun printlnConStringNoVacioNoGeneraErrores() {
        val statement = PrintStatement(StringLiteral("Hello world", dummySpan), dummySpan)
        val config = LinterConfig(noEmptyPrintln = true)

        val errors = rule.check(statement, config)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Println con número literal no genera errores")
    fun printlnConNumeroLiteralNoGeneraErrores() {
        val statement = PrintStatement(NumberLiteral("123", dummySpan), dummySpan)
        val config = LinterConfig(noEmptyPrintln = true)

        val errors = rule.check(statement, config)

        assertTrue(errors.isEmpty())
    }

    @Test
    @DisplayName("Println con string vacío genera error cuando la regla está activa")
    fun printlnConStringVacioGeneraErrorCuandoReglaActiva() {
        val statement = PrintStatement(StringLiteral("", dummySpan), dummySpan)
        val config = LinterConfig(noEmptyPrintln = true)

        val errors = rule.check(statement, config)

        assertEquals(1, errors.size)
        assertEquals("Empty println expression is not allowed", errors.first().message)
        assertEquals(dummySpan, errors.first().span)
    }

    @Test
    @DisplayName("Println con string vacío no genera error cuando la regla está inactiva")
    fun printlnConStringVacioNoGeneraErrorCuandoReglaInactiva() {
        val statement = PrintStatement(StringLiteral("", dummySpan), dummySpan)
        val config = LinterConfig(noEmptyPrintln = false)

        val errors = rule.check(statement, config)

        assertTrue(errors.isEmpty())
    }
}
