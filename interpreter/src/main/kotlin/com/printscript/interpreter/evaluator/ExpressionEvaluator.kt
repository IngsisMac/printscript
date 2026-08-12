package com.printscript.interpreter.evaluator

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Span
import com.printscript.interpreter.Environment
import com.printscript.interpreter.InterpreterContext
import com.printscript.interpreter.InterpreterException
import com.printscript.interpreter.NumberValue
import com.printscript.interpreter.StringValue
import com.printscript.interpreter.Value
import java.math.BigDecimal
import java.math.MathContext

fun interface ExpressionEvaluator<T : Expression> {
    fun evaluate(
        expr: T,
        env: Environment,
        context: InterpreterContext,
    ): Value
}

class LiteralEvaluator : ExpressionEvaluator<Expression> {
    override fun evaluate(
        expr: Expression,
        env: Environment,
        context: InterpreterContext,
    ): Value =
        when (expr) {
            is NumberLiteral -> NumberValue(BigDecimal(expr.value))
            is StringLiteral -> StringValue(expr.value)
            is BooleanLiteral -> com.printscript.interpreter.BooleanValue(expr.value)
            else -> throw InterpreterException("Unknown literal expression", expr.span)
        }
}

class VariableEvaluator : ExpressionEvaluator<Variable> {
    override fun evaluate(
        expr: Variable,
        env: Environment,
        context: InterpreterContext,
    ): Value = env.get(expr.name, expr.span)
}

class BinaryOpEvaluator : ExpressionEvaluator<BinaryOp> {
    override fun evaluate(
        expr: BinaryOp,
        env: Environment,
        context: InterpreterContext,
    ): Value {
        val left = context.evaluateExpression(expr.left, env)
        val right = context.evaluateExpression(expr.right, env)

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
}

class CallExpressionEvaluator : ExpressionEvaluator<CallExpression> {
    override fun evaluate(
        expr: CallExpression,
        env: Environment,
        context: InterpreterContext,
    ): Value {
        val arg = expr.argument
        return when (expr.name) {
            "readInput" -> evaluateReadInputCall(arg, env, context)
            "readEnv" -> evaluateReadEnvCall(arg, env, context, expr.span)
            else -> throw InterpreterException("Unknown function: ${expr.name}", expr.span)
        }
    }

    private fun evaluateReadInputCall(
        arg: Expression?,
        env: Environment,
        context: InterpreterContext,
    ): StringValue {
        val prompt = arg?.let { context.evaluateExpression(it, env).toString() } ?: ""
        if (context.isValidationMode) {
            return StringValue("")
        }
        if (prompt.isNotEmpty()) {
            context.output.print(prompt)
        }
        return StringValue(context.input.input(prompt))
    }

    private fun evaluateReadEnvCall(
        arg: Expression?,
        env: Environment,
        context: InterpreterContext,
        span: Span,
    ): StringValue {
        if (arg == null) {
            throw InterpreterException("readEnv requires a variable name argument", span)
        }
        val varName = context.evaluateExpression(arg, env).toString()
        if (context.isValidationMode) {
            return StringValue("")
        }
        return StringValue(System.getenv(varName) ?: "")
    }
}
