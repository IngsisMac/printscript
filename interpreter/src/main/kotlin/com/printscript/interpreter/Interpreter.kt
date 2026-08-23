package com.printscript.interpreter

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.ExpressionEvaluator
import com.printscript.interpreter.evaluator.StatementEvaluator

class Interpreter(
    override val version: Version,
    override val output: OutputEmitter = OutputEmitter { },
    override val input: InputSource = InputSource { "" },
    override val env: EnvSource = EnvSource { System.getenv(it) },
    override val isValidationMode: Boolean = false,
    private val config: InterpreterConfig = InterpreterConfig.from(version),
) : InterpreterContext {
    private val globalEnv = Environment()
    private val errors = mutableListOf<PrintScriptError>()

    fun execute(statements: Iterator<Statement>): List<PrintScriptError> {
        while (statements.hasNext()) {
            val stmt = tryNextStatement(statements) ?: break
            try {
                executeStatement(stmt, globalEnv)
            } catch (e: InterpreterException) {
                errors.add(PrintScriptError(e.message, e.span))
            } catch (e: RuntimeException) {
                errors.add(PrintScriptError(e.message ?: "Unknown error", stmt.span))
            }
        }
        return errors
    }

    private fun tryNextStatement(statements: Iterator<Statement>): Statement? =
        try {
            statements.next()
        } catch (e: Exception) {
            errors.add(extractError(e))
            null
        }

    private fun extractError(e: Exception): PrintScriptError = PrintScriptError(extractRawMessage(e), extractSpan(e))

    private fun extractSpan(e: Exception): Span =
        (
            e.javaClass.methods
                .firstOrNull { it.name == "getSpan" }
                ?.invoke(e) as? Span
        )
            ?: Span(Position(1, 1), Position(1, 1))

    private fun extractRawMessage(e: Exception): String =
        (
            e.javaClass.methods
                .firstOrNull { it.name == "getRawMessage" }
                ?.invoke(e) as? String
        )
            ?: e.message ?: "Error"

    @Suppress("UNCHECKED_CAST")
    override fun executeStatement(
        stmt: Statement,
        env: Environment,
    ) {
        val evaluator =
            config.statementEvaluators[stmt::class] as? StatementEvaluator<Statement>
                ?: throw InterpreterException(
                    "Statement type '${stmt::class.simpleName}' is not supported in version $version",
                    stmt.span,
                )
        evaluator.evaluate(stmt, env, this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun evaluateExpression(
        expr: Expression,
        env: Environment,
    ): Value {
        val evaluator =
            config.expressionEvaluators[expr::class] as? ExpressionEvaluator<Expression>
                ?: throw InterpreterException(
                    "Expression type '${expr::class.simpleName}' is not supported in version $version",
                    expr.span,
                )
        return evaluator.evaluate(expr, env, this)
    }
}
