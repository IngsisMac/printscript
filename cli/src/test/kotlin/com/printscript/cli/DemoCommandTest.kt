package com.printscript.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.PrintWriter

class DemoCommandTest {
    private lateinit var outContent: ByteArrayOutputStream
    private lateinit var errContent: ByteArrayOutputStream
    private lateinit var outWriter: PrintWriter
    private lateinit var errWriter: PrintWriter
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun setUp() {
        outContent = ByteArrayOutputStream()
        errContent = ByteArrayOutputStream()
        outWriter = PrintWriter(PrintStream(outContent), true)
        errWriter = PrintWriter(PrintStream(errContent), true)

        val printScriptCli = PrintScriptCli()
        commandLine = CommandLine(printScriptCli)
        commandLine.out = outWriter
        commandLine.err = errWriter
    }

    @Test
    @DisplayName("Ejecuta la demostración E2E completa con todas las etapas reales del pipeline")
    fun ejecutaDemostracionE2ECompletaConExito() {
        val exitCode = commandLine.execute("demo")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("PRINTSCRIPT - DEMOSTRACIÓN END-TO-END (E2E)"))
        assertTrue(output.contains("Ejecución real de PrintScript 1.0"))
        assertTrue(output.contains("Resultado: 3"))
        assertTrue(output.contains("Ejecución real de PrintScript 1.1"))
        assertTrue(output.contains("Score maximo: 100"))
        assertTrue(output.contains("Usuario activo: ingsis_demo_user"))
        assertTrue(output.contains("Análisis estático real"))
        assertTrue(output.contains("does not conform to camelCase format"))
        assertTrue(output.contains("println argument must be a simple variable or literal"))
        assertTrue(output.contains("Formateador de código real"))
        assertTrue(output.contains("let x: number = 10;"))
        assertTrue(output.contains("Diagnóstico de errores de sintaxis real"))
        assertTrue(output.contains("Expected SEMICOLON"))
        assertTrue(output.contains("DEMOSTRACIÓN E2E DE PRINTSCRIPT FINALIZADA CON ÉXITO"))
    }

    @Test
    @DisplayName("Ejecuta la demostración a través del alias e2e")
    fun ejecutaDemostracionConAliasE2E() {
        val exitCode = commandLine.execute("e2e")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("DEMOSTRACIÓN END-TO-END"))
    }
}
