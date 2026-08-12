package com.printscript.runner.integration

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import com.printscript.interpreter.Interpreter
import com.printscript.lexer.Lexer
import com.printscript.parser.Parser
import com.printscript.runner.PrintScriptRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.io.StringWriter

class PipelineIntegrationTest {
    private lateinit var outputBuffer: MutableList<String>
    private lateinit var outputEmitter: OutputEmitter

    @BeforeEach
    fun setUp() {
        outputBuffer = mutableListOf()
        outputEmitter = OutputEmitter { outputBuffer.add(it) }
    }

    @Test
    fun `full pipeline v1_0 - arithmetic and string concatenation`() {
        val code = """
            let a: number = 10;
            let b: number = 20;
            let sum: number = a + b;
            println("Sum: " + sum);
        """.trimIndent()

        val input = InputSource { "" }
        val lexer = Lexer(StringReader(code), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse()
        val interpreter = Interpreter(Version.V1_0, outputEmitter, input)
        val errors = interpreter.execute(statements)

        assertTrue(errors.isEmpty(), "Expected clean execution but got: $errors")
        assertEquals(listOf("Sum: 30"), outputBuffer)
    }

    @Test
    fun `full pipeline v1_1 - readInput interaction and conditionals`() {
        val code = """
            let userAgeStr: string = readInput("Age?");
            let isAdult: boolean = true;
            if (isAdult) {
                println("User input received: " + userAgeStr);
            } else {
                println("No input");
            }
        """.trimIndent()

        val input = InputSource { "25" }
        val lexer = Lexer(StringReader(code), Version.V1_1)
        val parser = Parser(lexer, Version.V1_1)
        val statements = parser.parse()
        val interpreter = Interpreter(Version.V1_1, outputEmitter, input)
        val errors = interpreter.execute(statements)

        assertTrue(errors.isEmpty(), "Expected clean execution but got: $errors")
        assertEquals(listOf("Age?", "User input received: 25"), outputBuffer)
    }

    @Test
    fun `full pipeline v1_1 - boolean logic and nested scopes`() {
        val code = """
            const active: boolean = true;
            let result: string = "initial";
            if (active) {
                let temp: string = "inside block";
                result = temp;
            } else {
                result = "inactive";
            }
            println(result);
        """.trimIndent()

        val input = InputSource { "" }
        val lexer = Lexer(StringReader(code), Version.V1_1)
        val parser = Parser(lexer, Version.V1_1)
        val statements = parser.parse()
        val interpreter = Interpreter(Version.V1_1, outputEmitter, input)
        val errors = interpreter.execute(statements)

        assertTrue(errors.isEmpty(), "Expected clean execution but got: $errors")
        assertEquals(listOf("inside block"), outputBuffer)
    }

    @Test
    fun `full pipeline - edge case uninitialized variable reference error`() {
        val code = """
            let x: number;
            println(x);
        """.trimIndent()

        val input = InputSource { "" }
        val lexer = Lexer(StringReader(code), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse()
        val interpreter = Interpreter(Version.V1_0, outputEmitter, input)
        val errors = interpreter.execute(statements)

        assertTrue(errors.isNotEmpty(), "Expected runtime error for uninitialized variable access")
    }

    @Test
    fun `full pipeline - edge case decimal floating point operations format without scientific notation`() {
        val code = """
            let x: number = 0.1 + 0.2;
            println(x);
        """.trimIndent()

        val input = InputSource { "" }
        val lexer = Lexer(StringReader(code), Version.V1_0)
        val parser = Parser(lexer, Version.V1_0)
        val statements = parser.parse()
        val interpreter = Interpreter(Version.V1_0, outputEmitter, input)
        val errors = interpreter.execute(statements)

        assertTrue(errors.isEmpty(), "Expected clean execution but got: $errors")
        assertEquals(listOf("0.3"), outputBuffer)
    }

    @Test
    fun `full pipeline - PrintScriptRunner facade format integration`() {
        val code = "let x: number = 5;"
        val writer = StringWriter()
        val result = PrintScriptRunner.format(StringReader(code), Version.V1_0, emptyMap(), writer)
        assertTrue(result.errors.isEmpty())
        assertTrue(writer.toString().isNotEmpty())
    }

    @Test
    fun `full pipeline - PrintScriptRunner facade format syntax error handling`() {
        val code = "let x: number = ;"
        val writer = StringWriter()
        val result = PrintScriptRunner.format(StringReader(code), Version.V1_0, emptyMap(), writer)
        assertTrue(result.errors.isNotEmpty())
    }
}
