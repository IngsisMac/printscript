package com.printscript.interpreter.evaluator

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Expression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.UnaryOp
import com.printscript.ast.Variable
import com.printscript.common.Version
import kotlin.reflect.KClass

object ExpressionEvaluatorFactory {
    fun create(version: Version): Map<KClass<out Expression>, ExpressionEvaluator<*>> =
        when (version) {
            Version.V1_0 -> v10Evaluators()
            Version.V1_1 -> v11Evaluators()
        }

    private fun v10Evaluators() =
        mapOf(
            NumberLiteral::class to LiteralEvaluator(),
            StringLiteral::class to LiteralEvaluator(),
            Variable::class to VariableEvaluator(),
            UnaryOp::class to UnaryOpEvaluator(),
            BinaryOp::class to BinaryOpEvaluator(),
        )

    private fun v11Evaluators() =
        mapOf(
            NumberLiteral::class to LiteralEvaluator(),
            StringLiteral::class to LiteralEvaluator(),
            BooleanLiteral::class to LiteralEvaluator(),
            Variable::class to VariableEvaluator(),
            UnaryOp::class to UnaryOpEvaluator(),
            BinaryOp::class to BinaryOpEvaluator(),
            CallExpression::class to CallExpressionEvaluator(),
        )
}
