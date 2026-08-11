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
import java.math.BigDecimal
import java.math.MathContext

class Interpreter(
    private val version: Version,
    private val output: OutputEmitter = OutputEmitter { },
    private val input: InputSource = InputSource { "" },
    private val isValidationMode: Boolean = false,
) {
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
            errors.add(
                PrintScriptError(
                    e.message ?: "Error",
                    Span(Position(1, 1), Position(1, 1)),
                ),
            )
            null
        }

    private fun executeStatement(
        stmt: Statement,
        env: Environment,
    ) {
        when (stmt) {
            is Declaration -> executeDeclaration(stmt, env)
            is Assignment -> executeAssignment(stmt, env)
            is PrintStatement -> executePrint(stmt, env)
            is IfStatement -> executeIf(stmt, env)
        }
    }

    private fun executeDeclaration(
        stmt: Declaration,
        env: Environment,
    ) {
        val stmtVal = stmt.value
        var value = stmtVal?.let { evaluateExpression(it, env) }

        if (isReadInput(stmtVal) && value is StringValue && stmt.type != "string") {
            value = coerceStringValue(value.value, stmt.type, stmt.span)
        }

        env.define(stmt.name, stmt.type, stmt.isConst, value, stmt.span)
    }

    private fun executeAssignment(
        stmt: Assignment,
        env: Environment,
    ) {
        val stmtVal = stmt.value
        var value = evaluateExpression(stmtVal, env)
        val targetSymbol = env.getSymbol(stmt.name)
        val needsCoerce = isReadInput(stmtVal) && targetSymbol != null && value is StringValue && targetSymbol.type != "string"
        if (needsCoerce && targetSymbol != null) {
            value = coerceStringValue((value as StringValue).value, targetSymbol.type, stmt.span)
        }
        env.set(stmt.name, value, stmt.span)
    }

    private fun executePrint(
        stmt: PrintStatement,
        env: Environment,
    ) {
        val value = evaluateExpression(stmt.expression, env)
        if (!isValidationMode) {
            output.print(value.toString())
        }
    }

    private fun executeIf(
        stmt: IfStatement,
        env: Environment,
    ) {
        val condition = evaluateExpression(stmt.condition, env)
        if (condition !is BooleanValue) {
            throw InterpreterException("If condition must be a boolean expression", stmt.span)
        }

        if (isValidationMode) {
            validateIfBranches(stmt, env)
        } else {
            runIfBranches(stmt, env, condition.value)
        }
    }

    private fun validateIfBranches(
        stmt: IfStatement,
        env: Environment,
    ) {
        val thenEnv = env.child()
        stmt.thenBranch.forEach { executeStatement(it, thenEnv) }
        stmt.elseBranch?.let { elseBranch ->
            val elseEnv = env.child()
            elseBranch.forEach { executeStatement(it, elseEnv) }
        }
    }

    private fun runIfBranches(
        stmt: IfStatement,
        env: Environment,
        conditionValue: Boolean,
    ) {
        val childEnv = env.child()
        if (conditionValue) {
            stmt.thenBranch.forEach { executeStatement(it, childEnv) }
        } else {
            stmt.elseBranch?.forEach { executeStatement(it, childEnv) }
        }
    }

    private fun isReadInput(expr: Expression?): Boolean = expr is CallExpression && expr.name == "readInput"

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

    private fun evaluateExpression(
        expr: Expression,
        env: Environment,
    ): Value =
        when (expr) {
            is NumberLiteral -> NumberValue(BigDecimal(expr.value))
            is StringLiteral -> StringValue(expr.value)
            is BooleanLiteral -> BooleanValue(expr.value)
            is Variable -> env.get(expr.name, expr.span)
            is BinaryOp -> evaluateBinaryOp(expr, env)
            is CallExpression -> evaluateCallExpression(expr, env)
        }

    private fun evaluateBinaryOp(
        expr: BinaryOp,
        env: Environment,
    ): Value {
        val left = evaluateExpression(expr.left, env)
        val right = evaluateExpression(expr.right, env)

        return when (expr.operator) {
            "+" -> evaluateAddOp(left, right, expr.span)
            "-", "*", "/" -> evaluateArithmeticOp(left, right, expr)
            else -> throw InterpreterException("Unknown operator: ${expr.operator}", expr.span)
        }
    }

    private fun evaluateAddOp(
        left: Value,
        right: Value,
        span: Span,
    ): Value =
        if (left is StringValue || right is StringValue) {
            StringValue(left.toString() + right.toString())
        } else if (left is NumberValue && right is NumberValue) {
            NumberValue(left.value + right.value)
        } else {
            throw InterpreterException(
                "Invalid operands for binary operator '+': ${left.typeName} and ${right.typeName}",
                span,
            )
        }

    private fun evaluateArithmeticOp(
        left: Value,
        right: Value,
        expr: BinaryOp,
    ): NumberValue {
        val leftNum = left.toNumber(expr.left.span)
        val rightNum = right.toNumber(expr.right.span)
        return when (expr.operator) {
            "-" -> NumberValue(leftNum - rightNum)
            "*" -> NumberValue(leftNum * rightNum)
            "/" -> {
                if (rightNum.compareTo(BigDecimal.ZERO) == 0) {
                    throw InterpreterException("Division by zero", expr.span)
                }
                NumberValue(leftNum.divide(rightNum, MathContext.DECIMAL64))
            }
            else -> throw InterpreterException("Unknown operator: ${expr.operator}", expr.span)
        }
    }

    private fun evaluateCallExpression(
        expr: CallExpression,
        env: Environment,
    ): Value {
        val arg = expr.argument
        return when (expr.name) {
            "readInput" -> evaluateReadInputCall(arg, env)
            "readEnv" -> evaluateReadEnvCall(arg, env, expr.span)
            else -> throw InterpreterException("Unknown function: ${expr.name}", expr.span)
        }
    }

    private fun evaluateReadInputCall(
        arg: Expression?,
        env: Environment,
    ): StringValue {
        val prompt = arg?.let { evaluateExpression(it, env).toString() } ?: ""
        if (isValidationMode) {
            return StringValue("")
        }
        if (prompt.isNotEmpty()) {
            output.print(prompt)
        }
        return StringValue(input.input(prompt))
    }

    private fun evaluateReadEnvCall(
        arg: Expression?,
        env: Environment,
        span: Span,
    ): StringValue {
        if (arg == null) {
            throw InterpreterException("readEnv requires a variable name argument", span)
        }
        val varName = evaluateExpression(arg, env).toString()
        if (isValidationMode) {
            return StringValue("")
        }
        return StringValue(System.getenv(varName) ?: "")
    }
}
