package com.printscript.cli

import com.printscript.cli.commands.FormatCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter

class FormatCommandTest {
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
    @DisplayName("Formateo exitoso de un archivo PrintScript con configuración válida")
    fun formateoExitosoDeArchivoValido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "unformatted.ps").apply {
                writeText("let x:number=10;")
            }
        val configFile =
            File(tempDir, "config.json").apply {
                writeText("{\"enforce-spacing-around-equals\": true}")
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--config", configFile.absolutePath)

        assertEquals(0, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a formatear no existe")
    fun retornaErrorAlFormatearArchivoInexistente() {
        val exitCode = commandLine.execute("format", "missing.ps")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error al especificar una versión del lenguaje no soportada al formatear")
    fun retornaErrorAlFormatearConVersionInvalida(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "sample.ps").apply {
                writeText("let x: number = 42;")
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--version", "9.9")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a formatear contiene errores de sintaxis")
    fun retornaErrorAlFormatearScriptConErroresSintacticos(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid.ps").apply {
                writeText("let x: number = ;")
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath)

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Manejo de archivo nulo directamente en la llamada a FormatCommand")
    fun manejoDeArchivoNuloEnFormatCommandDirecto() {
        val cmd = FormatCommand()
        cmd.file = null

        val code = cmd.call()

        assertEquals(2, code)
    }
}
