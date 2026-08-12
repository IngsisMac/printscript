package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class PrintScriptRunnerTest {
    private lateinit var output: StringBuilder
    private lateinit var emitter: OutputEmitter
    private lateinit var input: InputSource

    @BeforeEach
    fun setUp() {
        output = StringBuilder()
        emitter = OutputEmitter { output.append(it) }
        input = InputSource { "" }
    }

    @Test
    @DisplayName("Ejecución simple de declaración y println en v1.0")
    fun example1SimpleDeclarationAndPrintln() {
        val source =
            StringReader(
                """
                let x: number = 5;
                println(x);
                """.trimIndent(),
            )

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("5", output.toString())
    }

    @Test
    @DisplayName("Ejecución de operaciones aritméticas en v1.0")
    fun example2ArithmeticOperations() {
        val source =
            StringReader(
                """
                let x: number = 5;
                let y: number = 10;
                println(x + y);
                """.trimIndent(),
            )

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("15", output.toString())
    }

    @Test
    @DisplayName("Ejecución de concatenación de cadenas en v1.0")
    fun example3StringConcatenation() {
        val source =
            StringReader(
                """
                let message: string = "Hello";
                let name: string = "World";
                println(message + " " + name);
                """.trimIndent(),
            )

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("Hello World", output.toString())
    }

    @Test
    @DisplayName("Validación de código válido sin errores en v1.0")
    fun validateValidCodeReturnsNoErrors() {
        val source = StringReader("let x: number = 5;")

        val result = PrintScriptRunner.validate(source, Version.V1_0)

        assertTrue(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Ejecución con error de sintaxis retorna error de parseo")
    fun executeSyntaxErrorReturnsParseExceptionError() {
        val source = StringReader("let x: = 5;")

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    @DisplayName("Validación con error de sintaxis retorna error de parseo")
    fun validateSyntaxErrorReturnsParseExceptionError() {
        val source = StringReader("let x: = 5;")

        val result = PrintScriptRunner.validate(source, Version.V1_0)

        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    @DisplayName("Ejecución exitosa en versión 1.1")
    fun executeVersion11Execution() {
        val source = StringReader("const x: boolean = true;\nprintln(x);")

        val result = PrintScriptRunner.execute(source, Version.V1_1, emitter, input)

        assertTrue(result.errors.isEmpty())
        assertEquals("true", output.toString())
    }

    @Test
    @DisplayName("Validación exitosa en versión 1.1")
    fun validateVersion11Validation() {
        val source = StringReader("const x: boolean = true;\nprintln(x);")

        val result = PrintScriptRunner.validate(source, Version.V1_1)

        assertTrue(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Manejo de OutOfMemoryError durante la ejecución")
    fun executeHandlesOutOfMemoryError() {
        val failingReader =
            object : java.io.Reader() {
                override fun read(
                    cbuf: CharArray,
                    off: Int,
                    len: Int,
                ): Int = throw OutOfMemoryError("Java heap space")

                override fun close() {
                    // No-op for mock reader
                }
            }

        val result = PrintScriptRunner.execute(failingReader, Version.V1_0, emitter, input)

        assertEquals(1, result.errors.size)
        assertEquals("Java heap space", result.errors[0].message)
    }

    @Test
    @DisplayName("Manejo de OutOfMemoryError durante la validación")
    fun validateHandlesOutOfMemoryError() {
        val failingReader =
            object : java.io.Reader() {
                override fun read(
                    cbuf: CharArray,
                    off: Int,
                    len: Int,
                ): Int = throw OutOfMemoryError("Java heap space")

                override fun close() {
                    // No-op for mock reader
                }
            }

        val result = PrintScriptRunner.validate(failingReader, Version.V1_0)

        assertEquals(1, result.errors.size)
        assertEquals("Java heap space", result.errors[0].message)
    }
}
