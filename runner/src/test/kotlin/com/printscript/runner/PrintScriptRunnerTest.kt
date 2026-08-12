package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.StringReader

class PrintScriptRunnerTest {
    @Test
    fun `example 1 - simple declaration and println`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

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
    fun `example 2 - arithmetic operations`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

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
    fun `example 3 - string concatenation`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

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
    fun `validate - valid code returns no errors`() {
        val source = StringReader("let x: number = 5;")
        val result = PrintScriptRunner.validate(source, Version.V1_0)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `execute - syntax error returns parse exception error`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }
        val source = StringReader("let x: = 5;")
        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `validate - syntax error returns parse exception error`() {
        val source = StringReader("let x: = 5;")
        val result = PrintScriptRunner.validate(source, Version.V1_0)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `execute - version 1_1 execution`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }
        val source = StringReader("const x: boolean = true;\nprintln(x);")
        val result = PrintScriptRunner.execute(source, Version.V1_1, emitter, input)
        assertTrue(result.errors.isEmpty())
        assertEquals("true", output.toString())
    }

    @Test
    fun `validate - version 1_1 validation`() {
        val source = StringReader("const x: boolean = true;\nprintln(x);")
        val result = PrintScriptRunner.validate(source, Version.V1_1)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `execute - handles OutOfMemoryError`() {
        val failingReader =
            object : java.io.Reader() {
                override fun read(
                    cbuf: CharArray,
                    off: Int,
                    len: Int,
                ): Int {
                    throw OutOfMemoryError("Java heap space")
                }

                override fun close() {
                    // No-op for mock reader
                }
            }
        val emitter = OutputEmitter { }
        val input = InputSource { "" }
        val result = PrintScriptRunner.execute(failingReader, Version.V1_0, emitter, input)
        assertEquals(1, result.errors.size)
        assertEquals("Java heap space", result.errors[0].message)
    }

    @Test
    fun `validate - handles OutOfMemoryError`() {
        val failingReader =
            object : java.io.Reader() {
                override fun read(
                    cbuf: CharArray,
                    off: Int,
                    len: Int,
                ): Int {
                    throw OutOfMemoryError("Java heap space")
                }

                override fun close() {
                    // No-op for mock reader
                }
            }
        val result = PrintScriptRunner.validate(failingReader, Version.V1_0)
        assertEquals(1, result.errors.size)
        assertEquals("Java heap space", result.errors[0].message)
    }
}
