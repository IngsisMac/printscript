package com.printscript.ast

import com.printscript.common.Span

sealed class Statement {
    abstract val span: Span
}

data class Declaration(
    val name: String,
    val type: String, // "number", "string", "boolean"
    val value: Expression?,
    override val span: Span,
    val isConst: Boolean = false,
) : Statement()

data class Assignment(
    val name: String,
    val value: Expression,
    override val span: Span,
) : Statement()

data class PrintStatement(
    val expression: Expression,
    override val span: Span,
) : Statement()

data class IfStatement(
    val condition: Expression,
    val thenBranch: List<Statement>,
    val elseBranch: List<Statement>?,
    override val span: Span,
) : Statement()
