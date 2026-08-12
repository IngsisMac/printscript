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
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("Pipeline completo v1.0 - aritmética y concatenación de cadenas")
    fun fullPipelineV10ArithmeticAndStringConcatenation() {
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
    @DisplayName("Pipeline completo v1.1 - interacción con readInput y condicionales")
    fun fullPipelineV11ReadInputInteractionAndConditionals() {
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
    @DisplayName("Pipeline completo v1.1 - lógica booleana y scopes anidados")
    fun fullPipelineV11BooleanLogicAndNestedScopes() {
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
    @DisplayName("Pipeline completo - referencia a variable no inicializada produce error")
    fun fullPipelineUninitializedVariableReferenceError() {
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
    @DisplayName("Pipeline completo - operaciones decimales sin notación científica")
    fun fullPipelineDecimalOperationsWithoutScientificNotation() {
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
    @DisplayName("Pipeline completo - integración con el formateador de PrintScriptRunner")
    fun fullPipelinePrintScriptRunnerFormatIntegration() {
        val code = "let x: number = 5;"
        val writer = StringWriter()
        val result = PrintScriptRunner.format(StringReader(code), Version.V1_0, emptyMap(), writer)

        assertTrue(result.errors.isEmpty())
        assertTrue(writer.toString().isNotEmpty())
    }

    @Test
    @DisplayName("Pipeline completo - formateador maneja error de sintaxis correctamente")
    fun fullPipelinePrintScriptRunnerFormatSyntaxErrorHandling() {
        val code = "let x: number = ;"
        val writer = StringWriter()
        val result = PrintScriptRunner.format(StringReader(code), Version.V1_0, emptyMap(), writer)

        assertTrue(result.errors.isNotEmpty())
    }
}
