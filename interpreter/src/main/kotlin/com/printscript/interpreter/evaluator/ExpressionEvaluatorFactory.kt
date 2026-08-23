package com.printscript.interpreter.evaluator

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Version
import kotlin.reflect.KClass

object ExpressionEvaluatorFactory {
    fun create(version: Version): Map<KClass<out Expression>, ExpressionEvaluator<*>> =
        when (version) {
            Version.V1_0 ->
                mapOf(
                    NumberLiteral::class to LiteralEvaluator(),
                    StringLiteral::class to LiteralEvaluator(),
                    Variable::class to VariableEvaluator(),
                    BinaryOp::class to BinaryOpEvaluator(),
                )
            Version.V1_1 ->
                mapOf(
                    NumberLiteral::class to LiteralEvaluator(),
                    StringLiteral::class to LiteralEvaluator(),
                    BooleanLiteral::class to LiteralEvaluator(),
                    Variable::class to VariableEvaluator(),
                    BinaryOp::class to BinaryOpEvaluator(),
                    CallExpression::class to CallExpressionEvaluator(),
                )
        }
}
