package com.printscript.interpreter

import java.math.BigDecimal

class Environment(
    private val parent: Environment? = null,
) {
    private val variables = mutableMapOf<String, Value>()

    fun define(
        name: String,
        value: Value,
    ) {
        variables[name] = value
    }

    fun get(name: String): Value? = variables[name] ?: parent?.get(name)

    fun set(
        name: String,
        value: Value,
    ) {
        if (name in variables) {
            variables[name] = value
        } else if (parent != null) {
            parent.set(name, value)
        } else {
            throw RuntimeException("Undefined variable: $name")
        }
    }

    fun child() = Environment(this)
}

sealed class Value {
    abstract fun toNumber(): BigDecimal

    abstract override fun toString(): String
}

data class NumberValue(
    val value: BigDecimal,
) : Value() {
    override fun toNumber() = value

    override fun toString() = if (value.scale() <= 0) value.toBigInteger().toString() else value.toString()
}

data class StringValue(
    val value: String,
) : Value() {
    override fun toNumber() = throw RuntimeException("Cannot convert string to number")

    override fun toString() = value
}

data class BooleanValue(
    val value: Boolean,
) : Value() {
    override fun toNumber() = if (value) BigDecimal.ONE else BigDecimal.ZERO

    override fun toString() = value.toString()
}
