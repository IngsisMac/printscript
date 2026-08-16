package com.printscript.formatter

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable

object ExpressionFormatter {
    fun format(
        expression: Expression,
        config: FormatterConfig,
    ): String =
        when (expression) {
            is NumberLiteral -> expression.value
            is StringLiteral -> "\"${expression.value}\""
            is BooleanLiteral -> expression.value.toString()
            is Variable -> expression.name
            is BinaryOp -> formatBinaryOp(expression, config)
            is CallExpression -> formatCallExpression(expression, config)
        }

    private fun formatBinaryOp(
        op: BinaryOp,
        config: FormatterConfig,
    ): String {
        val space = if (config.mandatorySpaceSurroundingOperations) " " else ""
        val leftStr = formatChildExpression(op.left, op.operator, isLeft = true, config = config)
        val rightStr = formatChildExpression(op.right, op.operator, isLeft = false, config = config)

        return "$leftStr$space${op.operator}$space$rightStr"
    }

    private fun formatChildExpression(
        child: Expression,
        parentOp: String,
        isLeft: Boolean,
        config: FormatterConfig,
    ): String {
        val childFormatted = format(child, config)
        if (child is BinaryOp) {
            if (needsParentheses(child.operator, parentOp, isLeft)) {
                return "($childFormatted)"
            }
        }
        return childFormatted
    }

    private fun needsParentheses(
        childOp: String,
        parentOp: String,
        isLeft: Boolean,
    ): Boolean {
        val childPrec = precedence(childOp)
        val parentPrec = precedence(parentOp)

        if (childPrec < parentPrec) return true
        if (childPrec == parentPrec && !isLeft) {
            return parentOp == "-" || parentOp == "/"
        }
        return false
    }

    private fun precedence(op: String): Int =
        when (op) {
            "*", "/" -> 2
            "+", "-" -> 1
            else -> 0
        }

    private fun formatCallExpression(
        call: CallExpression,
        config: FormatterConfig,
    ): String {
        val argStr = call.argument?.let { format(it, config) } ?: ""
        return "${call.name}($argStr)"
    }
}
