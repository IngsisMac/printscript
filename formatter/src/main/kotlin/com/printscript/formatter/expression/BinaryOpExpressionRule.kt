package com.printscript.formatter.expression

import com.printscript.ast.BinaryOp
import com.printscript.ast.Expression
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

class BinaryOpExpressionRule : ExpressionRule {
    override fun appliesTo(expression: Expression): Boolean = expression is BinaryOp

    override fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String {
        val op = expression as BinaryOp
        val space = if (config.mandatorySpaceSurroundingOperations) " " else ""
        val leftStr = formatChildExpression(op.left, op.operator, isLeft = true, config = config, formatter = formatter)
        val rightStr =
            formatChildExpression(op.right, op.operator, isLeft = false, config = config, formatter = formatter)

        return "$leftStr$space${op.operator}$space$rightStr"
    }

    private fun formatChildExpression(
        child: Expression,
        parentOp: String,
        isLeft: Boolean,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String {
        val childFormatted = formatter.format(child, config)
        if (child is BinaryOp && needsParentheses(child.operator, parentOp, isLeft)) {
            return "($childFormatted)"
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
}
