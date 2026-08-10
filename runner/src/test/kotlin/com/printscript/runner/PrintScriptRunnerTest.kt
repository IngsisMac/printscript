package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Test
import java.io.StringReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class PrintScriptRunnerTest {

    @Test
    fun `example 1 - simple declaration and println`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

        val source = StringReader("""
            let x: number = 5;
            println(x);
        """.trimIndent())

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("5", output.toString())
    }

    @Test
    fun `example 2 - arithmetic operations`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

        val source = StringReader("""
            let x: number = 5;
            let y: number = 10;
            println(x + y);
        """.trimIndent())

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("15", output.toString())
    }

    @Test
    fun `example 3 - string concatenation`() {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

        val source = StringReader("""
            let message: string = "Hello";
            let name: string = "World";
            println(message + " " + name);
        """.trimIndent())

        val result = PrintScriptRunner.execute(source, Version.V1_0, emitter, input)

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals("Hello World", output.toString())
    }
}
