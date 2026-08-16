package com.printscript.linter.rule

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
import com.printscript.common.PrintScriptError
import com.printscript.linter.LinterConfig

class ReadInputExpressionRule : LinterRule {
    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        if (!config.mandatoryVariableOrLiteralInReadInput) return emptyList()
        return extractExpressions(statement).flatMap { checkExpression(it) }
    }

    private fun checkExpression(expr: Expression): List<PrintScriptError> =
        findReadInputCalls(expr).mapNotNull { call ->
            val arg = call.argument
            if (arg != null && !isSimpleExpression(arg)) {
                PrintScriptError(
                    "readInput argument must be a simple variable or literal",
                    call.span,
                )
            } else {
                null
            }
        }

    private fun extractExpressions(statement: Statement): List<Expression> {
        val list = mutableListOf<Expression>()
        when (statement) {
            is Declaration -> statement.value?.let { list.add(it) }
            is Assignment -> list.add(statement.value)
            is PrintStatement -> list.add(statement.expression)
            is IfStatement -> list.add(statement.condition)
        }
        return list
    }

    private fun findReadInputCalls(expression: Expression): List<CallExpression> {
        val calls = mutableListOf<CallExpression>()

        fun traverse(expr: Expression) {
            when (expr) {
                is CallExpression -> {
                    if (expr.name == "readInput") {
                        calls.add(expr)
                    }
                    expr.argument?.let { traverse(it) }
                }
                is BinaryOp -> {
                    traverse(expr.left)
                    traverse(expr.right)
                }
                else -> {}
            }
        }
        traverse(expression)
        return calls
    }

    private fun isSimpleExpression(expression: Expression): Boolean =
        when (expression) {
            is Variable, is StringLiteral, is NumberLiteral, is BooleanLiteral -> true
            else -> false
        }
}
