package com.printscript.cli

import com.printscript.cli.commands.AnalyzeCommand
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

class AnalyzeCommandTest {
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
    @DisplayName("Análisis estático exitoso sin violaciones de linter")
    fun analisisExitosoSinViolaciones(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "valid.ps").apply {
                writeText("let myVar: number = 10;")
            }

        val exitCode = commandLine.execute("analyze", scriptFile.absolutePath)

        assertEquals(0, exitCode)
    }

    @Test
    @DisplayName("Análisis estático exitoso pasando código directo mediante la opción --code")
    fun analisisExitosoDeCodigoInline() {
        val exitCode = commandLine.execute("analyze", "--code", "let myVar: number = 10;")

        assertEquals(0, exitCode)
    }

    @Test
    @DisplayName("Análisis estático funciona mediante el alias analyzing de la consigna")
    fun analisisFuncionaMedianteAliasAnalyzing(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "alias_test.ps").apply {
                writeText("let myVar: number = 10;")
            }

        val exitCode = commandLine.execute("analyzing", scriptFile.absolutePath)

        assertEquals(0, exitCode)
    }

    @Test
    @DisplayName("Análisis estático con flag --progress emite indicador de progreso en consola")
    fun analisisConFlagProgressEmiteProgreso(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "progress_test.ps").apply {
                writeText("let x: number = 10;\nprintln(x);")
            }

        val exitCode = commandLine.execute("analyze", scriptFile.absolutePath, "--progress")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("[Progreso] Sentencia #1 parseada"))
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a analizar no existe")
    fun retornaErrorAlAnalizarArchivoInexistente() {
        val exitCode = commandLine.execute("analyze", "missing.ps")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error al especificar una versión del lenguaje no soportada al analizar")
    fun retornaErrorAlAnalizarConVersionInvalida(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "sample.ps").apply {
                writeText("let x: number = 42;")
            }

        val exitCode = commandLine.execute("analyze", scriptFile.absolutePath, "--version", "9.9")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Análisis estático detecta y reporta violaciones de linter")
    fun analisisReportaViolacionesDeLinter(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid_linter.ps").apply {
                writeText("let my_var: number = 10;")
            }
        val configFile =
            File(tempDir, "linter_config.json").apply {
                writeText("{\"identifier_format\": \"camelCase\"}")
            }

        val exitCode = commandLine.execute("analyze", scriptFile.absolutePath, "--config", configFile.absolutePath)

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a analizar contiene errores de sintaxis")
    fun retornaErrorAlAnalizarScriptConErroresSintacticos(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid.ps").apply {
                writeText("let x: number = ;")
            }

        val exitCode = commandLine.execute("analyze", scriptFile.absolutePath)

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Manejo de archivo nulo y sin opción --code directamente en AnalyzeCommand")
    fun manejoDeArchivoNuloEnAnalyzeCommandDirecto() {
        val cmd = AnalyzeCommand()
        cmd.file = null
        cmd.inlineCode = null

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo con versión inválida directamente en AnalyzeCommand")
    fun manejoDeVersionInvalidaEnAnalyzeCommandDirecto(
        @TempDir tempDir: File,
    ) {
        val scriptFile = File(tempDir, "sample.ps").apply { writeText("let x: number = 42;") }
        val cmd = AnalyzeCommand()
        cmd.file = scriptFile
        cmd.versionStr = "9.9"

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo no existente directamente en AnalyzeCommand")
    fun manejoDeArchivoInexistenteEnAnalyzeCommandDirecto() {
        val cmd = AnalyzeCommand()
        cmd.file = File("non_existent_file.ps")

        val code = cmd.call()

        assertEquals(2, code)
    }
}
