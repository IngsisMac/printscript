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

class PrintScriptCliTest {

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
    @DisplayName("Muestra el mensaje de ayuda cuando se proporciona la opción --help")
    fun mostrarMensajeDeAyudaAlPasarOpcionHelp() {
        val exitCode = commandLine.execute("--help")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("PrintScript"))
        assertTrue(output.contains("execute"))
        assertTrue(output.contains("format"))
        assertTrue(output.contains("analyze"))
    }

    @Test
    @DisplayName("Muestra el mensaje predeterminado cuando no se pasa un subcomando")
    fun mostrarMensajePorDefectoSinSubcomando() {
        val exitCode = commandLine.execute()
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("PrintScript CLI"))
    }

    @Test
    @DisplayName("Muestra el mensaje de versión cuando se pasa la opción --version")
    fun mostrarMensajeDeVersionAlPasarOpcionVersion() {
        val exitCode = commandLine.execute("--version")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("1.0.0"))
    }
}
