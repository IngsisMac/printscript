package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.Interpreter
import com.printscript.lexer.Lexer
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import java.io.Reader

data class ExecutionResult(
    val errors: List<PrintScriptError>,
)

private val OOM_RESULT =
    ExecutionResult(
        listOf(
            PrintScriptError(
                "Java heap space",
                Span(Position(1, 1), Position(1, 1)),
            ),
        ),
    )

object PrintScriptRunner {
    fun execute(
        source: Reader,
        version: Version,
        output: OutputEmitter,
        input: InputSource,
    ): ExecutionResult =
        try {
            val lexer = Lexer(source, version)
            val parser = Parser(lexer, version)
            val statements = parser.parse()
            val interpreter = Interpreter(version, output, input)
            val errors = interpreter.execute(statements)
            ExecutionResult(errors)
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        } catch (e: OutOfMemoryError) {
            OOM_RESULT
        }
}
