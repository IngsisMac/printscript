package com.printscript.linter.rule

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError
import com.printscript.common.Span
import com.printscript.linter.IdentifierFormat
import com.printscript.linter.LinterConfig

class IdentifierFormatRule : LinterRule {
    override fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError> {
        val format = config.identifierFormat
        if (format == IdentifierFormat.NONE) return emptyList()

        val (name, span) = extractNameAndSpan(statement) ?: return emptyList()
        val error = validateIdentifier(name, span, format)
        return if (error != null) listOf(error) else emptyList()
    }

    private fun extractNameAndSpan(statement: Statement): Pair<String, Span>? =
        when (statement) {
            is Declaration -> Pair(statement.name, statement.span)
            is Assignment -> Pair(statement.name, statement.span)
            else -> null
        }

    private fun validateIdentifier(
        name: String,
        span: Span,
        format: IdentifierFormat,
    ): PrintScriptError? {
        if (format.matches(name)) return null
        val formatName = if (format == IdentifierFormat.CAMEL_CASE) "camelCase" else "snake_case"
        return PrintScriptError(
            "Identifier '$name' does not conform to $formatName format",
            span,
        )
    }
}
