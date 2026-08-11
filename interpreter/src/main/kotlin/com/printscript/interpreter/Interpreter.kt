package com.printscript.interpreter

import com.printscript.ast.Assignment
import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.ast.Expression
import com.printscript.ast.IfStatement
import com.printscript.ast.NumberLiteral
import com.printscript.ast.PrintStatement
import com.printscript.ast.Statement
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.PrintScriptError
import com.printscript.common.Version
import java.math.BigDecimal

class Interpreter(
    private val version: Version,
    private val output: OutputEmitter,
    private val input: InputSource,
) {
    private val globalEnv = Environment()
    private val errors = mutableListOf<PrintScriptError>()

    fun execute(statements: Iterator<Statement>): List<PrintScriptError> {
        while (statements.hasNext()) {
            val stmt =
                try {
                    statements.next()
                } catch (e: Exception) {
                    errors.add(
                        PrintScriptError(
                            e.message ?: "Error",
                            com.printscript.common.Span(
                                com.printscript.common.Position(1, 1),
                                com.printscript.common.Position(1, 1),
                            ),
                        ),
                    )
                    break
                }

            try {
                executeStatement(stmt, globalEnv)
            } catch (e: RuntimeException) {
                errors.add(PrintScriptError(e.message ?: "Unknown error", stmt.span))
            }
        }
        return errors
    }

    private fun executeStatement(
        stmt: Statement,
        env: Environment,
    ) {
        when (stmt) {
            is Declaration -> {
                val stmtValue = stmt.value
                val value =
                    if (stmtValue != null) {
                        evaluateExpression(stmtValue, env)
                    } else {
                        when (stmt.type) {
                            "number" -> NumberValue(BigDecimal.ZERO)
                            "string" -> StringValue("")
                            "boolean" -> BooleanValue(false)
                            else -> throw RuntimeException("Unknown type: ${stmt.type}")
                        }
                    }
                when (stmt.type) {
                    "number" -> if (value !is NumberValue) throw RuntimeException("Type mismatch: expected number but got StringValue")
                    "string" -> if (value !is StringValue) throw RuntimeException("Type mismatch: expected string")
                    "boolean" -> if (value !is BooleanValue) throw RuntimeException("Type mismatch: expected boolean")
                }
                env.define(stmt.name, value)
            }

            is Assignment -> {
                val value = evaluateExpression(stmt.value, env)
                env.set(stmt.name, value)
            }

            is PrintStatement -> {
                val value = evaluateExpression(stmt.expression, env)
                output.print(value.toString())
            }

            is IfStatement -> {
                val condition = evaluateExpression(stmt.condition, env)
                val conditionBool =
                    when (condition) {
                        is BooleanValue -> condition.value
                        else -> throw RuntimeException("If condition must be boolean")
                    }

                val childEnv = env.child()
                if (conditionBool) {
                    for (s in stmt.thenBranch) {
                        executeStatement(s, childEnv)
                    }
                } else if (stmt.elseBranch != null) {
                    for (s in stmt.elseBranch) {
                        executeStatement(s, childEnv)
                    }
                }
            }
        }
    }

    private fun evaluateExpression(
        expr: Expression,
        env: Environment,
    ): Value =
        when (expr) {
            is NumberLiteral -> NumberValue(BigDecimal(expr.value))

            is StringLiteral -> StringValue(expr.value)

            is BooleanLiteral -> BooleanValue(expr.value)

            is Variable ->
                env.get(expr.name)
                    ?: throw RuntimeException("Undefined variable: ${expr.name}")

            is BinaryOp -> {
                val left = evaluateExpression(expr.left, env)
                val right = evaluateExpression(expr.right, env)

                when (expr.operator) {
                    "+" -> {
                        if (left is StringValue || right is StringValue) {
                            StringValue(left.toString() + right.toString())
                        } else {
                            NumberValue(left.toNumber() + right.toNumber())
                        }
                    }
                    "-" -> NumberValue(left.toNumber() - right.toNumber())
                    "*" -> NumberValue(left.toNumber() * right.toNumber())
                    "/" -> {
                        val rightNum = right.toNumber()
                        if (rightNum == BigDecimal.ZERO) {
                            throw RuntimeException("Division by zero")
                        }
                        NumberValue(left.toNumber() / rightNum)
                    }
                    else -> throw RuntimeException("Unknown operator: ${expr.operator}")
                }
            }

            is CallExpression -> {
                when (expr.name) {
                    "readInput" -> {
                        val arg = expr.argument
                        val prompt =
                            if (arg != null) {
                                evaluateExpression(arg, env).toString()
                            } else {
                                ""
                            }
                        StringValue(input.input(prompt))
                    }
                    "readEnv" -> {
                        val arg = expr.argument
                        val varName =
                            if (arg != null) {
                                evaluateExpression(arg, env).toString()
                            } else {
                                throw RuntimeException("readEnv requires a variable name")
                            }
                        val value = System.getenv(varName)
                        StringValue(value ?: "")
                    }
                    else -> throw RuntimeException("Unknown function: ${expr.name}")
                }
            }
        }
}
