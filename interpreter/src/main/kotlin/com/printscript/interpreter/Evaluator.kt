package com.printscript.interpreter

interface Evaluator<in T, out R> {
    fun evaluate(
        node: T,
        env: Environment,
        context: InterpreterContext,
    ): R
}
