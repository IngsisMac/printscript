package com.printscript.runner.integration

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.Interpreter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ParserInterpreterIntegrationTest {
    private val dummySpan = Span(Position(1, 1), Position(1, 10))

    private lateinit var outputBuffer: MutableList<String>
    private lateinit var outputEmitter: OutputEmitter
    private lateinit var defaultInput: InputSource

    @BeforeEach
    fun setUp() {
        outputBuffer = mutableListOf()
        outputEmitter = OutputEmitter { outputBuffer.add(it) }
        defaultInput = InputSource { "" }
    }

    @Test
    fun `parser AST + interpreter - evaluates declaration and assignment`() {
        val statements = listOf(
            Declaration("counter", "number", NumberLiteral("10", dummySpan), dummySpan, isConst = false),
            Assignment("counter", BinaryOp(Variable("counter", dummySpan), "+", NumberLiteral("5", dummySpan), dummySpan), dummySpan),
            PrintStatement(Variable("counter", dummySpan), dummySpan)
        )

        val interpreter = Interpreter(Version.V1_0, outputEmitter, defaultInput)
        val errors = interpreter.execute(statements.iterator())

        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
        assertEquals(listOf("15"), outputBuffer)
    }

    @Test
    fun `parser AST + interpreter - throws runtime error on const re-assignment in v1_1`() {
        val statements = listOf(
            Declaration("MAX", "number", NumberLiteral("100", dummySpan), dummySpan, isConst = true),
            Assignment("MAX", NumberLiteral("200", dummySpan), dummySpan)
        )

        val interpreter = Interpreter(Version.V1_1, outputEmitter, defaultInput)
        val errors = interpreter.execute(statements.iterator())

        assertTrue(errors.isNotEmpty(), "Expected runtime error when reassigning const variable")
        assertTrue(errors.any { it.message.contains("const", ignoreCase = true) || it.message.contains("reassign", ignoreCase = true) || it.message.contains("MAX", ignoreCase = true) })
    }

    @Test
    fun `parser AST + interpreter - executes conditional block in v1_1`() {
        val statements = listOf(
            Declaration("flag", "boolean", BooleanLiteral(true, dummySpan), dummySpan, isConst = false),
            IfStatement(
                condition = Variable("flag", dummySpan),
                thenBranch = listOf(PrintStatement(StringLiteral("Branch True", dummySpan), dummySpan)),
                elseBranch = listOf(PrintStatement(StringLiteral("Branch False", dummySpan), dummySpan)),
                span = dummySpan
            )
        )

        val interpreter = Interpreter(Version.V1_1, outputEmitter, defaultInput)
        val errors = interpreter.execute(statements.iterator())

        assertTrue(errors.isEmpty())
        assertEquals(listOf("Branch True"), outputBuffer)
    }

    @Test
    fun `parser AST + interpreter - edge case division by zero produces error`() {
        val statements = listOf(
            Declaration("a", "number", NumberLiteral("10", dummySpan), dummySpan, isConst = false),
            Declaration("b", "number", NumberLiteral("0", dummySpan), dummySpan, isConst = false),
            PrintStatement(BinaryOp(Variable("a", dummySpan), "/", Variable("b", dummySpan), dummySpan), dummySpan)
        )

        val interpreter = Interpreter(Version.V1_0, outputEmitter, defaultInput)
        val errors = interpreter.execute(statements.iterator())

        assertTrue(errors.isNotEmpty(), "Expected error on division by zero")
    }

    @Test
    fun `parser AST + interpreter - edge case undeclared variable access produces error`() {
        val statements = listOf<Statement>(
            PrintStatement(Variable("nonExistent", dummySpan), dummySpan)
        )

        val interpreter = Interpreter(Version.V1_0, outputEmitter, defaultInput)
        val errors = interpreter.execute(statements.iterator())

        assertTrue(errors.isNotEmpty(), "Expected error for undeclared variable")
    }
}
