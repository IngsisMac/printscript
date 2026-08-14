package com.printscript.linter.rule

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError
import com.printscript.linter.IdentifierFormat
import com.printscript.linter.LinterConfig

class IdentifierFormatRule : LinterRule {
    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        val format = config.identifierFormat
        if (format == IdentifierFormat.NONE) return emptyList()

        val errors = mutableListOf<PrintScriptError>()

        when (statement) {
            is Declaration -> {
                if (!format.matches(statement.name)) {
                    val formatName = if (format == IdentifierFormat.CAMEL_CASE) "camelCase" else "snake_case"
                    errors.add(
                        PrintScriptError(
                            "Identifier '${statement.name}' does not conform to $formatName format",
                            statement.span,
                        ),
                    )
                }
            }
            is Assignment -> {
                if (!format.matches(statement.name)) {
                    val formatName = if (format == IdentifierFormat.CAMEL_CASE) "camelCase" else "snake_case"
                    errors.add(
                        PrintScriptError(
                            "Identifier '${statement.name}' does not conform to $formatName format",
                            statement.span,
                        ),
                    )
                }
            }
            else -> {}
        }

        return errors
    }
}
