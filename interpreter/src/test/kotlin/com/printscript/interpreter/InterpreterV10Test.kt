package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterpreterV10Test : BaseInterpreterTest(Version.V1_0) {
    @Test
    fun `PS-INT-001 - string concatenation`() {
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
    fun `PS-INT-002 - exact integer division`() {
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
    fun `PS-INT-003 - reassignment with division`() {
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
    fun `PS-INT-004 - emitter receives message without newline`() {
        val src = "println(\"hola\");"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("hola"), outputs)
        assertTrue(!outputs[0].endsWith("\n"))
    }

    @Test
    fun `PS-INT-005 - decimal arithmetic precision`() {
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
    fun `PS-INT-006 - non-terminating division does not explode`() {
        val src = "println(1 / 3);"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(1, outputs.size)
        assertTrue(outputs[0].startsWith("0.33333333"))
    }

    @Test
    fun `PS-INT-007 - string and number concatenation`() {
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
    fun `PS-INT-008 - using undeclared variable is an error`() {
        val src = "println(x);"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("x"), "Expected error message to mention x")
    }

    @Test
    fun `PS-INT-009 - assigning incompatible type is an error`() {
        val src = "let x: number = \"hola\";"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Type mismatch"), "Expected type mismatch error")
    }

    @Test
    fun `PS-INT-010 - arithmetic operations on strings is an error`() {
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
    fun `PS-INT-011 - redeclaring variable in same scope is an error`() {
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
    fun `PS-INT-012 - validation mode does not execute side effects`() {
        val src = "println(\"hola\");"

        val errors = execute(src, isValidationMode = true)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertTrue(outputs.isEmpty(), "Validation mode should not emit outputs")
    }
}
