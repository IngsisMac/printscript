package com.printscript.interpreter

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.common.Version
import com.printscript.interpreter.evaluator.AssignmentEvaluator
import com.printscript.interpreter.evaluator.DeclarationEvaluator
import com.printscript.interpreter.evaluator.IfStatementEvaluator
import com.printscript.interpreter.evaluator.PrintStatementEvaluator
import com.printscript.interpreter.evaluator.StatementEvaluatorFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StatementEvaluatorFactoryTest {
    private lateinit var version10: Version
    private lateinit var version11: Version

    @BeforeEach
    fun setUp() {
        version10 = Version.V1_0
        version11 = Version.V1_1
    }

    @Test
    @DisplayName("Fábrica de evaluadores de sentencias para versión 1.0 incluye sentencias base")
    fun fabricaVersion10IncluyeSentenciasBase() {
        val evaluators = StatementEvaluatorFactory.create(version10)

        assertTrue(evaluators.containsKey(Declaration::class))
        assertTrue(evaluators.containsKey(Assignment::class))
        assertTrue(evaluators.containsKey(PrintStatement::class))
        assertFalse(evaluators.containsKey(IfStatement::class))

        assertInstanceOf(DeclarationEvaluator::class.java, evaluators[Declaration::class])
        assertInstanceOf(AssignmentEvaluator::class.java, evaluators[Assignment::class])
        assertInstanceOf(PrintStatementEvaluator::class.java, evaluators[PrintStatement::class])
    }

    @Test
    @DisplayName("Fábrica de evaluadores de sentencias para versión 1.1 incluye sentencias condicionales")
    fun fabricaVersion11IncluyeSentenciasCondicionales() {
        val evaluators = StatementEvaluatorFactory.create(version11)

        assertTrue(evaluators.containsKey(Declaration::class))
        assertTrue(evaluators.containsKey(Assignment::class))
        assertTrue(evaluators.containsKey(PrintStatement::class))
        assertTrue(evaluators.containsKey(IfStatement::class))

        assertInstanceOf(IfStatementEvaluator::class.java, evaluators[IfStatement::class])
    }
}
