package com.printscript.runner

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class LinterRunnerTest {
    private lateinit var validCode: String
    private lateinit var invalidCode: String

    @BeforeEach
    fun setUp() {
        validCode =
            """
            let myVariable: number = 5;
            println(myVariable);
            """.trimIndent()

        invalidCode =
            """
            let my_variable: number = 5;
            println(1 + 2);
            """.trimIndent()
    }

    @Test
    @DisplayName("Análisis end-to-end de código válido retorna lista de errores vacía")
    fun analyzeValidCodeReturnsNoErrors() {
        val source = StringReader(validCode)
        val config =
            mapOf(
                "identifier_format" to "camel case",
                "mandatory-variable-or-literal-in-println" to true,
            )

        val result = PrintScriptRunner.analyze(source, Version.V1_0, config)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
    }

    @Test
    @DisplayName("Análisis end-to-end de código inválido retorna violaciones detectadas por el linter")
    fun analyzeInvalidCodeReturnsLinterViolations() {
        val source = StringReader(invalidCode)
        val config =
            mapOf(
                "identifier_format" to "camel case",
                "mandatory-variable-or-literal-in-println" to true,
            )

        val result = PrintScriptRunner.analyze(source, Version.V1_0, config)

        assertEquals(2, result.errors.size)
    }

    @Test
    @DisplayName("Análisis end-to-end en versión 1.1 con readInput reporta violaciones de expresión compuesta")
    fun analyzeVersion11ReadInputViolations() {
        val source = StringReader("let x: string = readInput(\"Prompt: \" + \"value\");")
        val config = mapOf("mandatory-variable-or-literal-in-readInput" to true)

        val result = PrintScriptRunner.analyze(source, Version.V1_1, config)

        assertEquals(1, result.errors.size)
        assertEquals("readInput argument must be a simple variable or literal", result.errors[0].message)
    }
}
