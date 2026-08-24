package com.printscript.cli

import com.printscript.cli.commands.FormatCommand
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
    @DisplayName("Formateo por defecto sobrescribe el archivo original in-place")
    fun formateoPorDefectoModificaElMismoArchivoInPlace(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "unformatted.ps").apply {
                writeText("let x:number=10;")
            }
        val configFile =
            File(tempDir, "config.json").apply {
                writeText(
                    "{\"enforce-spacing-around-equals\": true, \"enforce-spacing-after-colon-in-declaration\": true}"
                )
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--config", configFile.absolutePath)

        assertEquals(0, exitCode)
        assertEquals("let x: number = 10;", scriptFile.readText())
    }

    @Test
    @DisplayName("Formateo de código directo inline emite el resultado a consola")
    fun formateoDeCodigoInline(
        @TempDir tempDir: File,
    ) {
        val configFile =
            File(tempDir, "config.json").apply {
                writeText(
                    "{\"enforce-spacing-around-equals\": true, \"enforce-spacing-after-colon-in-declaration\": true}"
                )
            }
        val exitCode = commandLine.execute("format", "--code", "let x:number=10;", "-c", configFile.absolutePath)
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("let x: number = 10;"))
    }

    @Test
    @DisplayName("Formateo con flag --output escribe en un nuevo archivo y no modifica el original")
    fun formateoConFlagOutputGuardaEnNuevoArchivo(
        @TempDir tempDir: File,
    ) {
        val originalText = "let a: number = 5;"
        val scriptFile =
            File(tempDir, "source.ps").apply {
                writeText(originalText)
            }
        val outputFile = File(tempDir, "formatted_output.ps")

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "-o", outputFile.absolutePath)

        assertEquals(0, exitCode)
        assertEquals(originalText, scriptFile.readText())
        assertEquals("let a: number = 5;", outputFile.readText())
    }

    @Test
    @DisplayName("Formateo con flag --preview emite el resultado a consola y no modifica el archivo en disco")
    fun formateoConFlagPreviewMuestraEnConsola(
        @TempDir tempDir: File,
    ) {
        val originalText = "let a: number = 99;"
        val scriptFile =
            File(tempDir, "preview_source.ps").apply {
                writeText(originalText)
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--preview")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertEquals(originalText, scriptFile.readText())
        assertTrue(output.contains("let a: number = 99;"))
    }

    @Test
    @DisplayName("Formateo funciona mediante el alias formatting de la consigna")
    fun formateoFuncionaMedianteAliasFormatting(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "alias_format.ps").apply {
                writeText("let z: number = 1;")
            }

        val exitCode = commandLine.execute("formatting", scriptFile.absolutePath)

        assertEquals(0, exitCode)
        assertEquals("let z: number = 1;", scriptFile.readText())
    }

    @Test
    @DisplayName("Formateo con flag --progress emite indicador de progreso en consola")
    fun formateoConFlagProgressEmiteProgreso(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "progress_format.ps").apply {
                writeText("let x: number = 1;\nprintln(x);")
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--progress")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("[Progreso] Sentencia #1 parseada"))
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
    @DisplayName("Retorna código de error cuando el archivo en modo preview contiene errores de sintaxis")
    fun retornaErrorAlFormatearConPreviewScriptInvalido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid_preview.ps").apply {
                writeText("let x: number = ;")
            }

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "--preview")

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo en modo output contiene errores de sintaxis")
    fun retornaErrorAlFormatearConOutputScriptInvalido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "invalid_out.ps").apply {
                writeText("let x: number = ;")
            }
        val outFile = File(tempDir, "out.ps")

        val exitCode = commandLine.execute("format", scriptFile.absolutePath, "-o", outFile.absolutePath)

        assertEquals(1, exitCode)
    }

    @Test
    @DisplayName("Manejo de archivo nulo y sin opción --code directamente en FormatCommand")
    fun manejoDeArchivoNuloEnFormatCommandDirecto() {
        val cmd = FormatCommand()
        cmd.file = null
        cmd.inlineCode = null

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo con versión inválida directamente en FormatCommand")
    fun manejoDeVersionInvalidaEnFormatCommandDirecto(
        @TempDir tempDir: File,
    ) {
        val scriptFile = File(tempDir, "sample.ps").apply { writeText("let x: number = 42;") }
        val cmd = FormatCommand()
        cmd.file = scriptFile
        cmd.versionStr = "9.9"

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo no existente directamente en FormatCommand")
    fun manejoDeArchivoInexistenteEnFormatCommandDirecto() {
        val cmd = FormatCommand()
        cmd.file = File("non_existent_file.ps")

        val code = cmd.call()

        assertEquals(2, code)
    }
}
