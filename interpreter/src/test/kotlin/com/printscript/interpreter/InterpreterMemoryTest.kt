package com.printscript.interpreter

import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.Span
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterpreterMemoryTest {
    @Test
    fun `PS-INT-019 - interpreter does not accumulate statements`() {
        val totalStatements = 32768
        var count = 0
        val emitter = OutputEmitter { count++ }
        val input = InputSource { "" }
        val dummySpan = Span(Position(1, 1), Position(1, 1))

        val statementIterator =
            object : Iterator<Statement> {
                private var current = 0

                override fun hasNext(): Boolean = current < totalStatements

                override fun next(): Statement {
                    if (!hasNext()) throw NoSuchElementException()
                    current++
                    return PrintStatement(
                        NumberLiteral("42", dummySpan),
                        dummySpan,
                    )
                }
            }

        val interpreter = Interpreter(Version.V1_0, emitter, input)
        val errors = interpreter.execute(statementIterator)

        assertTrue(errors.isEmpty(), "Expected 0 errors but got: $errors")
        assertEquals(totalStatements, count)
    }
}
