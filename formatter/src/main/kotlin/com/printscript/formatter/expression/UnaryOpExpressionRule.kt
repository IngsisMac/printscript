package com.printscript.formatter.expression

import com.printscript.ast.BinaryOp
import com.printscript.ast.Expression
import com.printscript.ast.UnaryOp
import com.printscript.formatter.ExpressionFormatter
import com.printscript.formatter.FormatterConfig

class UnaryOpExpressionRule : ExpressionRule {
    override fun appliesTo(expression: Expression): Boolean = expression is UnaryOp

    override fun format(
        expression: Expression,
        config: FormatterConfig,
        formatter: ExpressionFormatter,
    ): String {
        val op = expression as UnaryOp
        val operandStr = formatter.format(op.operand, config)
        return if (op.operand is BinaryOp) {
            "${op.operator}($operandStr)"
        } else {
            "${op.operator}$operandStr"
        }
    }
}
