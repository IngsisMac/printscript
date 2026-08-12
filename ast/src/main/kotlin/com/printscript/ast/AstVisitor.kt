package com.printscript.ast

interface AstVisitor<R> {
    fun visit(node: Declaration): R

    fun visit(node: Assignment): R

    fun visit(node: PrintStatement): R

    fun visit(node: IfStatement): R

    fun visit(node: NumberLiteral): R

    fun visit(node: StringLiteral): R

    fun visit(node: BooleanLiteral): R

    fun visit(node: Variable): R

    fun visit(node: BinaryOp): R

    fun visit(node: CallExpression): R
}
