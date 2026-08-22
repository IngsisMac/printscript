package com.printscript.linter.rule

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.Expression
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.Variable
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.linter.LinterConfig

class NoUnusedVariablesRule : LinterRule {
    private val declaredVariables = mutableMapOf<String, Span>()
    private val usedVariables = mutableSetOf<String>()

    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        if (!config.noUnusedVariables) return emptyList()

        processStatement(statement)
        return emptyList()
    }

    override fun finish(config: LinterConfig): List<PrintScriptError> {
        if (!config.noUnusedVariables) return emptyList()

        val errors = mutableListOf<PrintScriptError>()
        for ((name, span) in declaredVariables) {
            if (name !in usedVariables) {
                errors.add(
                    PrintScriptError(
                        "Variable '$name' is declared but never used",
                        span,
                    ),
                )
            }
        }
        return errors
    }

    override fun reset() {
        declaredVariables.clear()
        usedVariables.clear()
    }

    private fun processStatement(statement: Statement) {
        when (statement) {
            is Declaration -> {
                declaredVariables[statement.name] = statement.nameSpan
                statement.value?.let { collectUsedVariables(it) }
            }
            is Assignment -> {
                collectUsedVariables(statement.value)
            }
            is PrintStatement -> {
                collectUsedVariables(statement.expression)
            }
            is IfStatement -> {
                collectUsedVariables(statement.condition)
                statement.thenBranch.forEach { processStatement(it) }
                statement.elseBranch?.forEach { processStatement(it) }
            }
        }
    }

    private fun collectUsedVariables(expression: Expression) {
        when (expression) {
            is Variable -> usedVariables.add(expression.name)
            is BinaryOp -> {
                collectUsedVariables(expression.left)
                collectUsedVariables(expression.right)
            }
            is CallExpression -> {
                expression.argument?.let { collectUsedVariables(it) }
            }
            else -> {}
        }
    }
}
