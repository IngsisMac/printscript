package com.printscript.cli

import com.printscript.cli.commands.ExecuteCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter

class ExecuteCommandTest {
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
    @DisplayName("Ejecución exitosa de un archivo PrintScript válido")
    fun ejecucionExitosaDeArchivoValido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "sample.ps").apply {
                writeText("let x: number = 42;\nprintln(x);")
            }

        val exitCode = commandLine.execute("execute", scriptFile.absolutePath, "--version", "1.0")

        assertEquals(0, exitCode)
    }

    @Test
    @DisplayName("Ejecución emite la salida estándar mediante el OutputEmitter")
    fun ejecucionEmiteSalidaEstandarConOutputEmitter(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "print_sample.ps").apply {
                writeText("println(\"Hello Output\");")
            }

        val exitCode = commandLine.execute("execute", scriptFile.absolutePath)
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("Hello Output"))
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a ejecutar no existe")
    fun retornaErrorAlEjecutarArchivoInexistente() {
        val exitCode = commandLine.execute("execute", "non_existent_file.ps")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error al especificar una versión del lenguaje no soportada al ejecutar")
    fun retornaErrorAlEjecutarConVersionInvalida(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "sample.ps").apply {
                writeText("let x: number = 42;")
            }

        val exitCode = commandLine.execute("execute", scriptFile.absolutePath, "--version", "9.9")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando el script contiene errores de sintaxis al ejecutar")
    fun retornaErrorAlEjecutarScriptConErroresSintacticos(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid.ps").apply {
                writeText("let x: number = ;")
            }

        val exitCode = commandLine.execute("execute", scriptFile.absolutePath)

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Manejo de archivo nulo directamente en la llamada a ExecuteCommand")
    fun manejoDeArchivoNuloEnExecuteCommandDirecto() {
        val cmd = ExecuteCommand()
        cmd.file = null

        val code = cmd.call()

        assertEquals(2, code)
    }
}
