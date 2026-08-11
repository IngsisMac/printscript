package com.printscript.ast

import com.printscript.common.Span

sealed class Expression {
    abstract val span: Span
}

data class NumberLiteral(
    val value: String,
    override val span: Span,
) : Expression()

data class StringLiteral(
    val value: String,
    override val span: Span,
) : Expression()

data class BooleanLiteral(
    val value: Boolean,
    override val span: Span,
) : Expression()

data class Variable(
    val name: String,
    override val span: Span,
) : Expression()

data class BinaryOp(
    val left: Expression,
    val operator: String, // "+", "-", "*", "/"
    val right: Expression,
    override val span: Span,
) : Expression()

data class CallExpression(
    val name: String,
    val argument: Expression?,
    override val span: Span,
) : Expression()
