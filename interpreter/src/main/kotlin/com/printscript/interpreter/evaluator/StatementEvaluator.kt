package com.printscript.interpreter.evaluator

import com.printscript.ast.Assignment
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.Span
import com.printscript.interpreter.BooleanValue
import com.printscript.interpreter.Environment
import com.printscript.interpreter.InterpreterContext
import com.printscript.interpreter.InterpreterException
import com.printscript.interpreter.NumberValue
import com.printscript.interpreter.StringValue
import com.printscript.interpreter.Value
import java.math.BigDecimal

fun interface StatementEvaluator<T : Statement> {
    fun evaluate(
        stmt: T,
        env: Environment,
        context: InterpreterContext,
    )
}

class DeclarationEvaluator : StatementEvaluator<Declaration> {
    override fun evaluate(
        stmt: Declaration,
        env: Environment,
        context: InterpreterContext,
    ) {
        val stmtVal = stmt.value
        var value = stmtVal?.let { context.evaluateExpression(it, env) }

        if (isReadInputCall(stmtVal) && value is StringValue && stmt.type != "string") {
            value = coerceStringValue(value.value, stmt.type, stmt.span)
        }

        env.define(stmt.name, stmt.type, stmt.isConst, value, stmt.span)
    }
}

class AssignmentEvaluator : StatementEvaluator<Assignment> {
    override fun evaluate(
        stmt: Assignment,
        env: Environment,
        context: InterpreterContext,
    ) {
        val stmtVal = stmt.value
        var value = context.evaluateExpression(stmtVal, env)
        val targetSymbol = env.getSymbol(stmt.name)
        val needsCoerce =
            isReadInputCall(stmtVal) &&
                targetSymbol != null &&
                value is StringValue &&
                targetSymbol.type != "string"

        if (needsCoerce) {
            value = coerceStringValue((value as StringValue).value, targetSymbol!!.type, stmt.span)
        }

        env.set(stmt.name, value, stmt.span)
    }
}

class PrintStatementEvaluator : StatementEvaluator<PrintStatement> {
    override fun evaluate(
        stmt: PrintStatement,
        env: Environment,
        context: InterpreterContext,
    ) {
        val value = context.evaluateExpression(stmt.expression, env)
        if (!context.isValidationMode) {
            context.output.print(value.toString())
        }
    }
}

class IfStatementEvaluator : StatementEvaluator<IfStatement> {
    override fun evaluate(
        stmt: IfStatement,
        env: Environment,
        context: InterpreterContext,
    ) {
        val condition = context.evaluateExpression(stmt.condition, env)
        if (condition !is BooleanValue) {
            throw InterpreterException("If condition must be a boolean expression", stmt.span)
        }

        if (context.isValidationMode) {
            validateBranches(stmt, env, context)
        } else {
            runBranch(stmt, env, context, condition.value)
        }
    }

    private fun validateBranches(
        stmt: IfStatement,
        env: Environment,
        context: InterpreterContext,
    ) {
        val thenEnv = env.child()
        stmt.thenBranch.forEach { context.executeStatement(it, thenEnv) }
        stmt.elseBranch?.let { elseBranch ->
            val elseEnv = env.child()
            elseBranch.forEach { context.executeStatement(it, elseEnv) }
        }
    }

    private fun runBranch(
        stmt: IfStatement,
        env: Environment,
        context: InterpreterContext,
        conditionValue: Boolean,
    ) {
        val childEnv = env.child()
        if (conditionValue) {
            stmt.thenBranch.forEach { context.executeStatement(it, childEnv) }
        } else {
            stmt.elseBranch?.forEach { context.executeStatement(it, childEnv) }
        }
    }
}

private fun isReadInputCall(expr: com.printscript.ast.Expression?): Boolean = expr is CallExpression && expr.name == "readInput"

private fun coerceStringValue(
    strValue: String,
    targetType: String,
    span: Span,
): Value =
    when (targetType) {
        "number" -> parseNumberValue(strValue, span)
        "boolean" -> parseBooleanValue(strValue, span)
        else -> StringValue(strValue)
    }

private fun parseNumberValue(
    strValue: String,
    span: Span,
): NumberValue =
    try {
        NumberValue(BigDecimal(strValue))
    } catch (e: Exception) {
        throw InterpreterException("Cannot convert input '$strValue' to number", span)
    }

private fun parseBooleanValue(
    strValue: String,
    span: Span,
): BooleanValue =
    when (strValue.lowercase()) {
        "true" -> BooleanValue(true)
        "false" -> BooleanValue(false)
        else -> throw InterpreterException("Cannot convert input '$strValue' to boolean", span)
    }
