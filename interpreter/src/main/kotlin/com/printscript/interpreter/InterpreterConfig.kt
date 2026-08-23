package com.printscript.interpreter

import com.printscript.ast.Expression
import com.printscript.ast.Statement
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.ExpressionEvaluator
import com.printscript.interpreter.evaluator.ExpressionEvaluatorFactory
import com.printscript.interpreter.evaluator.StatementEvaluator
import com.printscript.interpreter.evaluator.StatementEvaluatorFactory
import kotlin.reflect.KClass

class InterpreterConfig(
    val statementEvaluators: Map<KClass<out Statement>, StatementEvaluator<*>>,
    val expressionEvaluators: Map<KClass<out Expression>, ExpressionEvaluator<*>>,
    val version: Version,
) {
    companion object {
        fun from(version: Version): InterpreterConfig {
            val statementEvaluators = StatementEvaluatorFactory.create(version)
            val expressionEvaluators = ExpressionEvaluatorFactory.create(version)
            return InterpreterConfig(statementEvaluators, expressionEvaluators, version)
        }
    }
}
