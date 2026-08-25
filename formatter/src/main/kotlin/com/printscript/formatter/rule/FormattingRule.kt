package com.printscript.formatter.rule

import com.printscript.ast.Statement
import com.printscript.formatter.FormatterContext
import java.io.Writer

interface FormattingRule {
    fun appliesTo(statement: Statement): Boolean

    fun format(
        statement: Statement,
        writer: Writer,
        context: FormatterContext,
    )
}
