package com.printscript.formatter

import com.printscript.ast.Statement
import java.io.Writer

interface Formatter {
    fun format(
        statement: Statement,
        writer: Writer,
        config: FormatterConfig = FormatterConfig(),
        indentLevel: Int = 0,
    )
}
