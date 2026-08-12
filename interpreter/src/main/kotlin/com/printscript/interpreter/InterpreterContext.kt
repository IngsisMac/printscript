package com.printscript.interpreter

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version

interface InterpreterContext {
    val version: Version
    val output: OutputEmitter
    val input: InputSource
    val isValidationMode: Boolean

    fun executeStatement(
        stmt: Statement,
        env: Environment,
    )

    fun evaluateExpression(
        expr: Expression,
        env: Environment,
    ): Value
}
