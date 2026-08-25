package com.printscript.formatter.rule

import com.printscript.ast.Assignment
import com.printscript.ast.Statement
import com.printscript.formatter.FormatterContext
import java.io.Writer

class AssignmentFormattingRule : FormattingRule {
    override fun appliesTo(statement: Statement): Boolean = statement is Assignment

    override fun format(
        statement: Statement,
        writer: Writer,
        context: FormatterContext,
    ) {
        val assignment = statement as Assignment
        val config = context.config
        val indent = context.indentString
        val eq =
            FormattingUtils.getEqualsSpacing(
                config.enforceSpacingAroundEquals,
                assignment.spaceAroundEquals,
                config,
            )
        val valueStr = context.expressionFormatter.format(assignment.value, config)
        writer.write("$indent${assignment.name}$eq$valueStr${FormattingUtils.getSemicolon(config)}")
    }
}
