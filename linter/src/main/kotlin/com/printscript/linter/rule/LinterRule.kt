package com.printscript.linter.rule

import com.printscript.ast.Statement
import com.printscript.common.PrintScriptError
import com.printscript.linter.LinterConfig

interface LinterRule {
    fun check(
        statement: Statement,
        config: LinterConfig,
    ): List<PrintScriptError>

    fun finish(config: LinterConfig): List<PrintScriptError> = emptyList()

    fun reset() {}
}
