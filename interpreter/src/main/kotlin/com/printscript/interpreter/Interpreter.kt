package com.printscript.interpreter

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.PrintScriptError
import com.printscript.common.PrintScriptFailure
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.ExpressionEvaluator
import com.printscript.interpreter.evaluator.StatementEvaluator

class Interpreter(
    override val version: Version,
    override val output: OutputEmitter = OutputEmitter { },
    override val input: InputSource = InputSource { "" },
    override val env: EnvSource = EnvSource.DENY,
    override val isValidationMode: Boolean = false,
    private val config: InterpreterConfig = InterpreterConfig.from(version),
) : InterpreterContext {
    private val globalEnv = Environment()
    private val errors = mutableListOf<PrintScriptError>()

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    fun execute(statements: Iterator<Statement>): List<PrintScriptError> {
        while (statements.hasNext()) {
            val stmt = tryNextStatement(statements) ?: break
            try {
                executeStatement(stmt, globalEnv)
            } catch (e: RuntimeException) {
                val error =
                    if (e is PrintScriptFailure) {
                        PrintScriptError(e.rawMessage, e.span)
                    } else {
                        PrintScriptError(e.message ?: "Unknown error", Span(Position(1, 1), Position(1, 1)))
                    }
                errors.add(error)
            }
        }
        return errors
    }

    @Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
    private fun tryNextStatement(statements: Iterator<Statement>): Statement? =
        try {
            statements.next()
        } catch (e: Exception) {
            val error =
                if (e is PrintScriptFailure) {
                    PrintScriptError(e.rawMessage, e.span)
                } else {
                    PrintScriptError(e.message ?: "Unknown error", Span(Position(1, 1), Position(1, 1)))
                }
            errors.add(error)
            null
        }

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
