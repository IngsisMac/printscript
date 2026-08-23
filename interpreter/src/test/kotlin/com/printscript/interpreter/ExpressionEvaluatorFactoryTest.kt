package com.printscript.interpreter

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.BinaryOpEvaluator
import com.printscript.interpreter.evaluator.CallExpressionEvaluator
import com.printscript.interpreter.evaluator.ExpressionEvaluatorFactory
import com.printscript.interpreter.evaluator.LiteralEvaluator
import com.printscript.interpreter.evaluator.VariableEvaluator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ExpressionEvaluatorFactoryTest {
    private lateinit var version10: Version
    private lateinit var version11: Version

    @BeforeEach
    fun setUp() {
        version10 = Version.V1_0
        version11 = Version.V1_1
    }

    @Test
    @DisplayName("Fábrica de evaluadores de expresiones para versión 1.0 incluye expresiones base")
    fun fabricaVersion10IncluyeExpresionesBase() {
        val evaluators = ExpressionEvaluatorFactory.create(version10)

        assertTrue(evaluators.containsKey(NumberLiteral::class))
        assertTrue(evaluators.containsKey(StringLiteral::class))
        assertTrue(evaluators.containsKey(Variable::class))
        assertTrue(evaluators.containsKey(BinaryOp::class))
        assertFalse(evaluators.containsKey(BooleanLiteral::class))
        assertFalse(evaluators.containsKey(CallExpression::class))

        assertInstanceOf(LiteralEvaluator::class.java, evaluators[NumberLiteral::class])
        assertInstanceOf(LiteralEvaluator::class.java, evaluators[StringLiteral::class])
        assertInstanceOf(VariableEvaluator::class.java, evaluators[Variable::class])
        assertInstanceOf(BinaryOpEvaluator::class.java, evaluators[BinaryOp::class])
    }

    @Test
    @DisplayName("Fábrica de evaluadores de expresiones para versión 1.1 incluye booleans y llamadas a funciones")
    fun fabricaVersion11IncluyeExpresionesExtendidas() {
        val evaluators = ExpressionEvaluatorFactory.create(version11)

        assertTrue(evaluators.containsKey(NumberLiteral::class))
        assertTrue(evaluators.containsKey(StringLiteral::class))
        assertTrue(evaluators.containsKey(BooleanLiteral::class))
        assertTrue(evaluators.containsKey(Variable::class))
        assertTrue(evaluators.containsKey(BinaryOp::class))
        assertTrue(evaluators.containsKey(CallExpression::class))

        assertInstanceOf(LiteralEvaluator::class.java, evaluators[BooleanLiteral::class])
        assertInstanceOf(CallExpressionEvaluator::class.java, evaluators[CallExpression::class])
    }
}
