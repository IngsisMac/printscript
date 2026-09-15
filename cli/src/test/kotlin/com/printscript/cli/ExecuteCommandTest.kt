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
    @DisplayName("Ejecución exitosa pasando código directo mediante la opción --code")
    fun ejecucionExitosaDeCodigoInline() {
        val exitCode = commandLine.execute("execute", "--code", "let a: number = 10;\nprintln(a);")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("10"))
    }

    @Test
    @DisplayName("Ejecución funciona mediante el alias execution de la consigna")
    fun ejecucionFuncionaMedianteAliasExecution(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "alias_exec.ps").apply {
                writeText("println(\"Exec Alias\");")
            }

        val exitCode = commandLine.execute("execution", scriptFile.absolutePath)
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("Exec Alias"))
    }

    @Test
    @DisplayName("Ejecución con flag --progress emite indicador de progreso en consola")
    fun ejecucionConFlagProgressEmiteProgreso(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "progress_exec.ps").apply {
                writeText("let a: number = 10;\nprintln(a);")
            }

        val exitCode = commandLine.execute("execute", scriptFile.absolutePath, "--progress")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("[Progreso] Sentencia #1 parseada"))
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
    @DisplayName("Manejo de archivo nulo y sin opción --code directamente en ExecuteCommand")
    fun manejoDeArchivoNuloEnExecuteCommandDirecto() {
        val cmd = ExecuteCommand()
        cmd.file = null
        cmd.inlineCode = null

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo con versión inválida directamente en ExecuteCommand")
    fun manejoDeVersionInvalidaEnExecuteCommandDirecto(
        @TempDir tempDir: File,
    ) {
        val scriptFile = File(tempDir, "sample.ps").apply { writeText("let x: number = 42;") }
        val cmd = ExecuteCommand()
        cmd.file = scriptFile
        cmd.versionStr = "9.9"

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo no existente directamente en ExecuteCommand")
    fun manejoDeArchivoInexistenteEnExecuteCommandDirecto() {
        val cmd = ExecuteCommand()
        cmd.file = File("non_existent_file.ps")

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Ejecución soporta variables de entorno inline mediante el flag -e")
    fun ejecucionSoportaVariablesDeEntornoInline() {
        val exitCode =
            commandLine.execute(
                "execute",
                "--version",
                "1.1",
                "-e",
                "CUSTOM_KEY=custom_value",
                "--code",
                "println(readEnv(\"CUSTOM_KEY\"));",
            )
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("custom_value"))
    }
}
