package com.printscript.interpreter.evaluator

import com.printscript.ast.Assignment
import com.printscript.ast.Declaration
import com.printscript.ast.IfStatement
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.common.Version
import kotlin.reflect.KClass

object StatementEvaluatorFactory {
    fun create(version: Version): Map<KClass<out Statement>, StatementEvaluator<*>> =
        when (version) {
            Version.V1_0 ->
                mapOf(
                    Declaration::class to DeclarationEvaluator(),
                    Assignment::class to AssignmentEvaluator(),
                    PrintStatement::class to PrintStatementEvaluator(),
                )
            Version.V1_1 ->
                mapOf(
                    Declaration::class to DeclarationEvaluator(),
                    Assignment::class to AssignmentEvaluator(),
                    PrintStatement::class to PrintStatementEvaluator(),
                    IfStatement::class to IfStatementEvaluator(),
                )
        }
}
