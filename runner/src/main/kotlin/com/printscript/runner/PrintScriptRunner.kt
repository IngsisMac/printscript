package com.printscript.runner

import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.Interpreter
import com.printscript.lexer.Lexer
import com.printscript.lexer.LexerException
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import java.io.Reader

data class ExecutionResult(
    val errors: List<PrintScriptError>,
    val fatalError: String? = null,
) {
    val hasFatalError: Boolean get() = fatalError != null
}

private val OOM_RESULT =
    ExecutionResult(
        errors =
            listOf(
                PrintScriptError(
                    "Java heap space",
                    Span(Position(1, 1), Position(1, 1)),
                ),
            ),
        fatalError = "Java heap space",
    )

object PrintScriptRunner {
    fun execute(
        source: Reader,
        version: Version,
        output: OutputEmitter,
        input: InputSource,
        env: EnvSource = EnvSource { System.getenv(it) },
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val lexer = Lexer(source, version)
            val parser = Parser(lexer, version)
            val statements = trackProgress(parser.parse(), onProgress)
            val interpreter = Interpreter(version, output, input, env, isValidationMode = false)
            val errors = interpreter.execute(statements)
            ExecutionResult(errors)
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        } catch (e: OutOfMemoryError) {
            OOM_RESULT
        }

    fun validate(
        source: Reader,
        version: Version,
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val lexer = Lexer(source, version)
            val parser = Parser(lexer, version)
            val statements = trackProgress(parser.parse(), onProgress)
            val interpreter = Interpreter(version, isValidationMode = true)
            val errors = interpreter.execute(statements)
            ExecutionResult(errors)
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        } catch (e: OutOfMemoryError) {
            OOM_RESULT
        }

    fun format(
        source: Reader,
        version: Version,
        config: Map<String, Any?> = emptyMap(),
        writer: java.io.Writer,
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val lexer = Lexer(source, version)
            val parser = Parser(lexer, version)
            val statements = trackProgress(parser.parse(), onProgress)
            val formatter = com.printscript.formatter.DefaultFormatter()
            val formatterConfig =
                com.printscript.formatter.FormatterConfig
                    .fromMap(config)
            for (statement in statements) {
                formatter.format(statement, writer, formatterConfig)
            }
            ExecutionResult(emptyList())
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        } catch (e: OutOfMemoryError) {
            OOM_RESULT
        }

    fun analyze(
        source: Reader,
        version: Version,
        config: Map<String, Any?> = emptyMap(),
        onError: (PrintScriptError) -> Unit = {},
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val lexer = Lexer(source, version)
            val parser = Parser(lexer, version)
            val statements = trackProgress(parser.parse(), onProgress)
            val linter = com.printscript.linter.DefaultLinter()
            val linterConfig =
                com.printscript.linter.LinterConfig
                    .fromMap(config)
            val errors = linter.analyze(statements, linterConfig, onError)
            ExecutionResult(errors)
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        } catch (e: OutOfMemoryError) {
            OOM_RESULT
        }

    fun format(
        source: Reader,
        version: Version,
        config: Reader,
        writer: java.io.Writer,
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult = format(source, version, ConfigLoader.parseJsonToMap(config), writer, onProgress)

    fun analyze(
        source: Reader,
        version: Version,
        config: Reader,
        onError: (PrintScriptError) -> Unit = {},
        onProgress: (Int, com.printscript.ast.Statement) -> Unit = { _, _ -> },
    ): ExecutionResult = analyze(source, version, ConfigLoader.parseJsonToMap(config), onError, onProgress)

    private fun trackProgress(
        statements: Iterator<com.printscript.ast.Statement>,
        onProgress: (Int, com.printscript.ast.Statement) -> Unit,
    ): Iterator<com.printscript.ast.Statement> {
        var count = 0
        return sequence {
            for (statement in statements) {
                count++
                onProgress(count, statement)
                yield(statement)
            }
        }.iterator()
    }
}
