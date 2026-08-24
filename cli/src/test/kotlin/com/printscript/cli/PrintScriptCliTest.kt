package com.printscript.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringReader

class PrintScriptCliTest {
    private lateinit var outContent: ByteArrayOutputStream
    private lateinit var errContent: ByteArrayOutputStream
    private lateinit var outWriter: PrintWriter
    private lateinit var errWriter: PrintWriter
    private lateinit var printScriptCli: PrintScriptCli
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun setUp() {
        outContent = ByteArrayOutputStream()
        errContent = ByteArrayOutputStream()
        outWriter = PrintWriter(PrintStream(outContent), true)
        errWriter = PrintWriter(PrintStream(errContent), true)

        printScriptCli = PrintScriptCli()
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
        assertTrue(output.contains("validate"))
        assertTrue(output.contains("execute"))
        assertTrue(output.contains("format"))
        assertTrue(output.contains("analyze"))
        assertTrue(output.contains("demo"))
    }

    @Test
    @DisplayName("Inicia la consola interactiva y procesa comando exit")
    fun iniciarConsolaInteractivaYSalir() {
        val input = "exit\n"
        printScriptCli.customReader = BufferedReader(StringReader(input))

        val exitCode = printScriptCli.call()
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("CONSOLA INTERACTIVA"))
        assertTrue(output.contains("Sesión finalizada."))
    }

    @Test
    @DisplayName("Inicia la consola interactiva y procesa comando help seguido de salir")
    fun iniciarConsolaInteractivaComandoHelpYSalir() {
        val input = "help\nsalir\n"
        printScriptCli.customReader = BufferedReader(StringReader(input))

        val exitCode = printScriptCli.call()
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("Comandos disponibles:"))
        assertTrue(output.contains("Sesión finalizada."))
    }

    @Test
    @DisplayName("Inicia la consola interactiva y ejecuta código inline antes de salir")
    fun iniciarConsolaInteractivaEjecutarCodigoYSalir() {
        val input = "execute --code \"let x: number = 7; println(x);\"\nquit\n"
        printScriptCli.customReader = BufferedReader(StringReader(input))

        val exitCode = printScriptCli.call()
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("7"))
        assertTrue(output.contains("Sesión finalizada."))
    }

    @Test
    @DisplayName("Consola interactiva ignora líneas vacías y finaliza ante fin de entrada (EOF)")
    fun consolaInteractivaIgnoraLineasVaciasYTerminaEnEOF() {
        val input = "\n   \n"
        printScriptCli.customReader = BufferedReader(StringReader(input))

        val exitCode = printScriptCli.call()
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("CONSOLA INTERACTIVA"))
    }

    @Test
    @DisplayName("tokenize divide correctamente argumentos con comillas simples y dobles")
    fun tokenizeDivideCorrectamenteArgumentos() {
        val tokens =
            com.printscript.cli.util.CommandLineTokenizer.tokenize(
                "execute --code \"let a: string = 'hola';\" -v 1.0"
            )
        assertEquals(
            listOf("execute", "--code", "let a: string = 'hola';", "-v", "1.0"),
            tokens,
        )
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
