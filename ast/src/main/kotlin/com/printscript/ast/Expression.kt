package com.printscript.ast

import com.printscript.common.Span

sealed class Expression : AstNode {
    abstract override val span: Span

    abstract fun <R> accept(visitor: AstVisitor<R>): R
}

data class NumberLiteral(
    val value: String,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class StringLiteral(
    val value: String,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class BooleanLiteral(
    val value: Boolean,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class Variable(
    val name: String,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class BinaryOp(
    val left: Expression,
    val operator: String, // "+", "-", "*", "/"
    val right: Expression,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class CallExpression(
    val name: String,
    val argument: Expression?,
    override val span: Span,
) : Expression() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}
