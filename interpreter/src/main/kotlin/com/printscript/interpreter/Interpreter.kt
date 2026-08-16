package com.printscript.interpreter

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.Expression
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Position
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.AssignmentEvaluator
import com.printscript.interpreter.evaluator.BinaryOpEvaluator
import com.printscript.interpreter.evaluator.CallExpressionEvaluator
import com.printscript.interpreter.evaluator.DeclarationEvaluator
import com.printscript.interpreter.evaluator.IfStatementEvaluator
import com.printscript.interpreter.evaluator.LiteralEvaluator
import com.printscript.interpreter.evaluator.PrintStatementEvaluator
import com.printscript.interpreter.evaluator.VariableEvaluator

class Interpreter(
    override val version: Version,
    override val output: OutputEmitter = OutputEmitter { },
    override val input: InputSource = InputSource { "" },
    override val isValidationMode: Boolean = false,
) : InterpreterContext {
    private val globalEnv = Environment()
    private val errors = mutableListOf<PrintScriptError>()

    private val declarationEvaluator = DeclarationEvaluator()
    private val assignmentEvaluator = AssignmentEvaluator()
    private val printEvaluator = PrintStatementEvaluator()
    private val ifEvaluator = IfStatementEvaluator()

    private val literalEvaluator = LiteralEvaluator()
    private val variableEvaluator = VariableEvaluator()
    private val binaryOpEvaluator = BinaryOpEvaluator()
    private val callExprEvaluator = CallExpressionEvaluator()

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

    private fun extractError(e: Exception): PrintScriptError {
        val span =
            (
                e.javaClass.methods
                    .firstOrNull { it.name == "getSpan" }
                    ?.invoke(e) as? Span
            )
                ?: Span(Position(1, 1), Position(1, 1))
        val rawMsg =
            (
                e.javaClass.methods
                    .firstOrNull { it.name == "getRawMessage" }
                    ?.invoke(e) as? String
            )
                ?: e.message ?: "Error"
        return PrintScriptError(rawMsg, span)
    }

    override fun executeStatement(
        stmt: Statement,
        env: Environment,
    ) {
        when (stmt) {
            is Declaration -> declarationEvaluator.evaluate(stmt, env, this)
            is Assignment -> assignmentEvaluator.evaluate(stmt, env, this)
            is PrintStatement -> printEvaluator.evaluate(stmt, env, this)
            is IfStatement -> ifEvaluator.evaluate(stmt, env, this)
        }
    }

    override fun evaluateExpression(
        expr: Expression,
        env: Environment,
    ): Value =
        when (expr) {
            is NumberLiteral, is StringLiteral, is BooleanLiteral -> literalEvaluator.evaluate(expr, env, this)
            is Variable -> variableEvaluator.evaluate(expr, env, this)
            is BinaryOp -> binaryOpEvaluator.evaluate(expr, env, this)
            is CallExpression -> callExprEvaluator.evaluate(expr, env, this)
        }
}
