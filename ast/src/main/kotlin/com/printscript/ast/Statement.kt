package com.printscript.ast

import com.printscript.common.Span

sealed class Statement : AstNode {
    abstract override val span: Span

    abstract fun <R> accept(visitor: AstVisitor<R>): R
}

data class Declaration(
    val name: String,
    val type: String, // "number", "string", "boolean"
    val value: Expression?,
    override val span: Span,
    val isConst: Boolean = false,
    val nameSpan: Span = span,
) : Statement() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class Assignment(
    val name: String,
    val value: Expression,
    override val span: Span,
    val nameSpan: Span = span,
) : Statement() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class PrintStatement(
    val expression: Expression,
    override val span: Span,
) : Statement() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}

data class IfStatement(
    val condition: Expression,
    val thenBranch: List<Statement>,
    val elseBranch: List<Statement>?,
    override val span: Span,
) : Statement() {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visit(this)
}
