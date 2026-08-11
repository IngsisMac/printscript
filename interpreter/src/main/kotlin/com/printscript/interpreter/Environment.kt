package com.printscript.interpreter

import com.printscript.common.Span
import java.math.BigDecimal

class InterpreterException(
    override val message: String,
    val span: Span,
) : RuntimeException(message)

data class Symbol(
    val name: String,
    val type: String,
    val isConst: Boolean,
    val isInitialized: Boolean,
    val value: Value?,
)

class Environment(
    private val parent: Environment? = null,
) {
    private val variables = mutableMapOf<String, Symbol>()

    fun define(
        name: String,
        type: String,
        isConst: Boolean,
        value: Value?,
        span: Span,
    ) {
        if (variables.containsKey(name)) {
            throw InterpreterException("Variable '$name' is already declared in this scope", span)
        }
        val isInit = value != null
        if (value != null) {
            checkTypeMatch(type, value, span)
        }
        variables[name] = Symbol(name, type, isConst, isInit, value)
    }

    fun set(
        name: String,
        value: Value,
        span: Span,
    ) {
        val symbol = variables[name]
        if (symbol != null) {
            if (symbol.isConst) {
                throw InterpreterException("Cannot reassign constant variable: $name", span)
            }
            checkTypeMatch(symbol.type, value, span)
            variables[name] = symbol.copy(value = value, isInitialized = true)
        } else if (parent != null) {
            parent.set(name, value, span)
        } else {
            throw InterpreterException("Variable '$name' is not declared", span)
        }
    }

    fun get(
        name: String,
        span: Span,
    ): Value {
        val symbol = variables[name]
        return if (symbol != null) {
            if (!symbol.isInitialized || symbol.value == null) {
                throw InterpreterException("Variable '$name' is not initialized", span)
            }
            symbol.value
        } else if (parent != null) {
            parent.get(name, span)
        } else {
            throw InterpreterException("Variable '$name' is not declared", span)
        }
    }

    fun getSymbol(name: String): Symbol? = variables[name] ?: parent?.getSymbol(name)

    fun child() = Environment(this)

    private fun checkTypeMatch(
        expectedType: String,
        value: Value,
        span: Span,
    ) {
        val actualType = value.typeName
        if (expectedType != actualType) {
            throw InterpreterException("Type mismatch: expected $expectedType but got $actualType", span)
        }
    }
}

sealed class Value {
    abstract val typeName: String

    abstract fun toNumber(span: Span): BigDecimal

    abstract override fun toString(): String
}

data class NumberValue(
    val value: BigDecimal,
) : Value() {
    override val typeName = "number"

    override fun toNumber(span: Span) = value

    override fun toString(): String =
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            value.stripTrailingZeros().toPlainString()
        }
}

data class StringValue(
    val value: String,
) : Value() {
    override val typeName = "string"

    override fun toNumber(span: Span): BigDecimal =
        throw InterpreterException("Cannot convert string to number", span)

    override fun toString() = value
}

data class BooleanValue(
    val value: Boolean,
) : Value() {
    override val typeName = "boolean"

    override fun toNumber(span: Span): BigDecimal =
        if (value) BigDecimal.ONE else BigDecimal.ZERO

    override fun toString() = value.toString()
}
