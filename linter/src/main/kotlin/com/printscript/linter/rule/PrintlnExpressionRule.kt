package com.printscript.linter.rule

import com.printscript.ast.BooleanLiteral
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.PrintScriptError
import com.printscript.linter.LinterConfig

class PrintlnExpressionRule : LinterRule {
    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        if (!config.mandatoryVariableOrLiteralInPrintln) return emptyList()
        if (statement !is PrintStatement) return emptyList()

        if (!isSimpleExpression(statement.expression)) {
            return listOf(
                PrintScriptError(
                    "println argument must be a simple variable or literal",
                    statement.span,
                ),
            )
        }
        return emptyList()
    }

    private fun isSimpleExpression(expression: Expression): Boolean =
        when (expression) {
            is Variable, is StringLiteral, is NumberLiteral, is BooleanLiteral -> true
            else -> false
        }
}
