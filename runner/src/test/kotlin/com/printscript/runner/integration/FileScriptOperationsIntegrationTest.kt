package com.printscript.runner.integration

import com.printscript.common.Version
import com.printscript.runner.PrintScriptRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.io.StringWriter

class FileScriptOperationsIntegrationTest {
    private lateinit var version10: Version
    private lateinit var version11: Version

    @BeforeEach
    fun setUp() {
        version10 = Version.V1_0
        version11 = Version.V1_1
    }

    private fun getScriptReader(path: String): InputStreamReader {
        val stream =
            javaClass.getResourceAsStream(path)
                ?: error("Resource not found: $path")
        return InputStreamReader(stream)
    }

    @Test
    @DisplayName("Validación exitosa de script válido en versión 1.0")
    fun validacionDeScriptValido10() {
        val reader = getScriptReader("/scripts/1.0/01_declarations.ps")

        val result = PrintScriptRunner.validate(reader, version10)

        assertTrue(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Validación de script con error de sintaxis reporta error")
    fun validacionDeScriptInvalidoReportaError() {
        val reader = getScriptReader("/scripts/invalid/syntax_error.ps")

        val result = PrintScriptRunner.validate(reader, version10)

        assertFalse(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Formateo de script 1.0 escribe salida formateada")
    fun formateoDeScript10() {
        val reader = getScriptReader("/scripts/1.0/01_declarations.ps")
        val writer = StringWriter()

        val result = PrintScriptRunner.format(reader, version10, emptyMap(), writer)

        assertTrue(result.errors.isEmpty())
        assertTrue(writer.toString().contains("let a: number = 10;"))
    }

    @Test
    @DisplayName("Análisis estático de script 1.0 con reglas por defecto")
    fun analisisEstaticoDeScript10() {
        val reader = getScriptReader("/scripts/1.0/01_declarations.ps")

        val result = PrintScriptRunner.analyze(reader, version10, emptyMap())

        assertTrue(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Validación exitosa de script condicional en versión 1.1")
    fun validacionDeScriptCondicional11() {
        val reader = getScriptReader("/scripts/1.1/02_if_else_branches.ps")

        val result = PrintScriptRunner.validate(reader, version11)

        assertTrue(result.errors.isEmpty())
    }
}
