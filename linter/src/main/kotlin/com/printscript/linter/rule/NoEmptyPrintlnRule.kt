package com.printscript.linter.rule

import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.common.PrintScriptError
import com.printscript.linter.LinterConfig

class NoEmptyPrintlnRule : LinterRule {
    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        if (!config.noEmptyPrintln) return emptyList()
        if (statement !is PrintStatement) return emptyList()

        val expr = statement.expression
        if (expr is StringLiteral && expr.value.isEmpty()) {
            return listOf(
                PrintScriptError(
                    "Empty println expression is not allowed",
                    statement.span,
                ),
            )
        }
        return emptyList()
    }
}
