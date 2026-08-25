package com.printscript.formatter.rule

import com.printscript.ast.Declaration
import com.printscript.ast.Statement
import com.printscript.formatter.FormatterConfig
import com.printscript.formatter.FormatterContext
import java.io.Writer

class DeclarationFormattingRule : FormattingRule {
    override fun appliesTo(statement: Statement): Boolean = statement is Declaration

    override fun format(
        statement: Statement,
        writer: Writer,
        context: FormatterContext,
    ) {
        val declaration = statement as Declaration
        val head = buildHeader(declaration, context.config, context.indentString)
        val valuePart = buildValuePart(declaration, context)
        writer.write("$head$valuePart${FormattingUtils.getSemicolon(context.config)}")
    }

    private fun buildHeader(
        declaration: Declaration,
        config: FormatterConfig,
        indent: String,
    ): String {
        val kw = if (declaration.isConst) "const" else "let"
        val b =
            FormattingUtils.getColonSpacing(
                config.enforceSpacingBeforeColonInDeclaration,
                declaration.spaceBeforeColon,
                false,
                config,
            )
        val a =
            FormattingUtils.getColonSpacing(
                config.enforceSpacingAfterColonInDeclaration,
                declaration.spaceAfterColon,
                true,
                config,
            )
        return "$indent$kw ${declaration.name}$b:$a${declaration.type}"
    }

    private fun buildValuePart(
        declaration: Declaration,
        context: FormatterContext,
    ): String =
        declaration.value?.let {
            val eq =
                FormattingUtils.getEqualsSpacing(
                    context.config.enforceSpacingAroundEquals,
                    declaration.spaceAroundEquals,
                    context.config,
                )
            "$eq${context.expressionFormatter.format(it, context.config)}"
        } ?: ""
}
