package com.printscript.cli

import com.printscript.cli.commands.ValidateCommand
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

class ValidateCommandTest {
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
    @DisplayName("Validación exitosa de un archivo PrintScript sintáctica y semánticamente correcto")
    fun validacionExitosaDeArchivoValido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "valid.ps").apply {
                writeText("let x: number = 42;\nprintln(x);")
            }

        val exitCode = commandLine.execute("validate", scriptFile.absolutePath, "--version", "1.0")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("Validación completada sin errores."))
    }

    @Test
    @DisplayName("Validación funciona mediante el alias validation de la consigna")
    fun validacionFuncionaMedianteAliasValidation(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "valid_alias.ps").apply {
                writeText("let a: string = \"Hello\";\nprintln(a);")
            }

        val exitCode = commandLine.execute("validation", scriptFile.absolutePath)
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("Validación completada sin errores."))
    }

    @Test
    @DisplayName("Validación con flag --progress emite indicador de progreso en consola")
    fun validacionConFlagProgressEmiteProgreso(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "progress.ps").apply {
                writeText("let a: number = 1;\nlet b: number = 2;\nprintln(a + b);")
            }

        val exitCode = commandLine.execute("validate", scriptFile.absolutePath, "--progress")
        outWriter.flush()
        val output = outContent.toString()

        assertEquals(0, exitCode)
        assertTrue(output.contains("[Progreso] Sentencia #1 parseada"))
        assertTrue(output.contains("[Progreso] Sentencia #2 parseada"))
        assertTrue(output.contains("[Progreso] Sentencia #3 parseada"))
    }

    @Test
    @DisplayName("Retorna código de error cuando el script contiene errores de sintaxis")
    fun retornaErrorAlValidarScriptInvalido(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "syntax_error.ps").apply {
                writeText("let x: number = ;")
            }

        val exitCode = commandLine.execute("validate", scriptFile.absolutePath)
        errWriter.flush()
        val errOutput = errContent.toString()

        assertEquals(1, exitCode)
        assertTrue(errOutput.contains("Error:"))
    }

    @Test
    @DisplayName("Retorna código de error cuando el archivo a validar no existe")
    fun retornaErrorAlValidarArchivoInexistente() {
        val exitCode = commandLine.execute("validate", "non_existent_validate.ps")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Retorna código de error cuando se especifica una versión no soportada")
    fun retornaErrorAlValidarConVersionInvalida(
        @TempDir tempDir: File,
    ) {
        val scriptFile =
            File(tempDir, "sample.ps").apply {
                writeText("let x: number = 10;")
            }

        val exitCode = commandLine.execute("validate", scriptFile.absolutePath, "--version", "99.0")

        assertEquals(2, exitCode)
    }

    @Test
    @DisplayName("Manejo de archivo nulo directamente en ValidateCommand")
    fun manejoDeArchivoNuloEnValidateCommandDirecto() {
        val cmd = ValidateCommand()
        cmd.file = null

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo con versión inválida directamente en ValidateCommand")
    fun manejoDeVersionInvalidaEnValidateCommandDirecto(
        @TempDir tempDir: File,
    ) {
        val scriptFile = File(tempDir, "sample.ps").apply { writeText("let x: number = 1;") }
        val cmd = ValidateCommand()
        cmd.file = scriptFile
        cmd.versionStr = "invalid_version"

        val code = cmd.call()

        assertEquals(2, code)
    }

    @Test
    @DisplayName("Manejo de archivo inexistente directamente en ValidateCommand")
    fun manejoDeArchivoInexistenteEnValidateCommandDirecto() {
        val cmd = ValidateCommand()
        cmd.file = File("missing_file.ps")

        val code = cmd.call()

        assertEquals(2, code)
    }
}
