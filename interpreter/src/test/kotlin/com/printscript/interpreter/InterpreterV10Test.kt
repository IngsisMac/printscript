package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class InterpreterV10Test : BaseInterpreterTest(Version.V1_0) {
    @Test
    @DisplayName("Concatenación de cadenas de texto")
    fun stringConcatenation() {
        val src =
            """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("Joe Doe"), outputs)
    }

    @Test
    @DisplayName("División entera exacta")
    fun exactIntegerDivision() {
        val src =
            """
            let a: number = 12;
            let b: number = 4;
            let c: number = a / b;
            println("Result: " + c);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("Result: 3"), outputs)
    }

    @Test
    @DisplayName("Reasignación con resultado de división")
    fun reassignmentWithDivision() {
        val src =
            """
            let a: number = 12;
            let b: number = 4;
            a = a / b;
            println("Result: " + a);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("Result: 3"), outputs)
    }

    @Test
    @DisplayName("El emisor de salida recibe el mensaje sin salto de línea al final")
    fun emitterReceivesMessageWithoutNewline() {
        val src = "println(\"hola\");"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("hola"), outputs)
        assertTrue(!outputs[0].endsWith("\n"))
    }

    @Test
    @DisplayName("Precisión en la aritmética con números decimales")
    fun decimalArithmeticPrecision() {
        val src =
            """
            let a: number = 0.1;
            let b: number = 0.2;
            println(a + b);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("0.3"), outputs)
    }

    @Test
    @DisplayName("La división periódica infinta se calcula con precisión delimitada")
    fun nonTerminatingDivisionDoesNotExplode() {
        val src = "println(1 / 3);"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(1, outputs.size)
        assertTrue(outputs[0].startsWith("0.33333333"))
    }

    @Test
    @DisplayName("Concatenación de cadena y número")
    fun stringAndNumberConcatenation() {
        val src =
            """
            let n: number = 5;
            println("valor: " + n);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("valor: 5"), outputs)
    }

    @Test
    @DisplayName("El uso de una variable no declarada genera error")
    fun usingUndeclaredVariableIsAnError() {
        val src = "println(x);"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("x"), "Expected error message to mention x")
    }

    @Test
    @DisplayName("La asignación de tipo incompatible genera error de incompatibilidad")
    fun assigningIncompatibleTypeIsAnError() {
        val src = "let x: number = \"hola\";"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Type mismatch"), "Expected type mismatch error")
    }

    @Test
    @DisplayName("Operaciones aritméticas sobre cadenas genera error")
    fun arithmeticOperationsOnStringsIsAnError() {
        val src =
            """
            let a: string = "hola";
            let b: string = "chau";
            println(a - b);
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        val hasStrErr = errors[0].message.contains("string", ignoreCase = true)
        val hasNumErr = errors[0].message.contains("number", ignoreCase = true)
        assertTrue(hasStrErr || hasNumErr, "Expected arithmetic on string error")
    }

    @Test
    @DisplayName("Redeclaración de variable en el mismo scope genera error")
    fun redeclaringVariableInSameScopeIsAnError() {
        val src =
            """
            let x: number = 1;
            let x: number = 2;
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("already declared"), "Expected redeclaration error")
    }

    @Test
    @DisplayName("El modo validación no ejecuta efectos secundarios de salida")
    fun validationModeDoesNotExecuteSideEffects() {
        val src = "println(\"hola\");"

        val errors = execute(src, isValidationMode = true)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertTrue(outputs.isEmpty(), "Validation mode should not emit outputs")
    }
}
