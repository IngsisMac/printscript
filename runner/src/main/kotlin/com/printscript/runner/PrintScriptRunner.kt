package com.printscript.runner

import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.PrintScriptError
import com.printscript.common.Version
import com.printscript.formatter.DefaultFormatter
import com.printscript.formatter.FormatterConfig
import com.printscript.interpreter.Interpreter
import com.printscript.lexer.Lexer
import com.printscript.lexer.LexerException
import com.printscript.linter.DefaultLinter
import com.printscript.linter.LinterConfig
import com.printscript.parser.ParseException
import com.printscript.parser.Parser
import java.io.Reader
import java.io.Writer

data class ExecutionResult(
    val errors: List<PrintScriptError> = emptyList(),
)

object PrintScriptRunner {
    @JvmOverloads
    fun execute(
        source: Reader,
        version: Version,
        output: OutputEmitter,
        input: InputSource,
        env: EnvSource = EnvSource { System.getenv(it) },
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val statements = trackProgress(Parser(Lexer(source, version), version).parse(), onProgress)
            val interpreter = Interpreter(version, output, input, env, isValidationMode = false)
            ExecutionResult(interpreter.execute(statements))
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        }

    @JvmOverloads
    fun validate(
        source: Reader,
        version: Version,
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val statements = trackProgress(Parser(Lexer(source, version), version).parse(), onProgress)
            val interpreter = Interpreter(version, isValidationMode = true)
            ExecutionResult(interpreter.execute(statements))
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        }

    @JvmOverloads
    fun format(
        source: Reader,
        version: Version,
        config: Map<String, Any?> = emptyMap(),
        writer: Writer,
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val statements = trackProgress(Parser(Lexer(source, version), version).parse(), onProgress)
            val formatterConfig = FormatterConfig.fromMap(config)
            formatStatements(statements, writer, formatterConfig)
            ExecutionResult(emptyList())
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        }

    private fun formatStatements(
        statements: Iterator<Statement>,
        writer: Writer,
        config: FormatterConfig,
    ) {
        val formatter = DefaultFormatter()
        var isFirst = true
        var prevStmt: Statement? = null
        for (statement in statements) {
            if (!isFirst) {
                val extraBreaks = if (prevStmt is PrintStatement) config.lineBreaksAfterPrintln else 0
                repeat(1 + extraBreaks) { writer.write("\n") }
            }
            formatter.format(statement, writer, config)
            isFirst = false
            prevStmt = statement
        }
    }

    @JvmOverloads
    fun analyze(
        source: Reader,
        version: Version,
        config: Map<String, Any?> = emptyMap(),
        onError: (PrintScriptError) -> Unit = {},
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult =
        try {
            val statements = trackProgress(Parser(Lexer(source, version), version).parse(), onProgress)
            val linterConfig = LinterConfig.fromMap(config)
            val errors = DefaultLinter().analyze(statements, linterConfig, onError)
            ExecutionResult(errors)
        } catch (e: LexerException) {
            ExecutionResult(listOf(e.toError()))
        } catch (e: ParseException) {
            ExecutionResult(listOf(PrintScriptError(e.rawMessage, e.span)))
        }

    @JvmOverloads
    fun format(
        source: Reader,
        version: Version,
        config: Reader,
        writer: Writer,
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult = format(source, version, ConfigLoader.parseJsonToMap(config), writer, onProgress)

    @JvmOverloads
    fun analyze(
        source: Reader,
        version: Version,
        config: Reader,
        onError: (PrintScriptError) -> Unit = {},
        onProgress: (Int, Statement) -> Unit = { _, _ -> },
    ): ExecutionResult = analyze(source, version, ConfigLoader.parseJsonToMap(config), onError, onProgress)

    private fun trackProgress(
        statements: Iterator<Statement>,
        onProgress: (Int, Statement) -> Unit,
    ): Iterator<Statement> {
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
