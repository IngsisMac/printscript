package com.printscript.linter

import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError

interface Linter {
    fun analyze(
        statements: Iterator<Statement>,
        config: LinterConfig = LinterConfig(),
    ): List<PrintScriptError>
}
