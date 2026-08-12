package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterpreterEdgeCasesTest : BaseInterpreterTest(Version.V1_1) {
    @Test
    fun `division by zero throws positioned runtime error`() {
        val src = "println(10 / 0);"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Division by zero"))
    }

    @Test
    fun `accessing uninitialized variable throws error`() {
        val src =
            """
            let x: number;
            println(x);
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("not initialized"))
    }

    @Test
    fun `variable declared in block scope is not accessible outside`() {
        val src =
            """
            if (true) {
                let inside: string = "secret";
            }
            println(inside);
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("not declared"))
    }

    @Test
    fun `block scope can access and mutate outer scope variable`() {
        val src =
            """
            let counter: number = 1;
            if (true) {
                counter = counter + 5;
            }
            println(counter);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("6"), outputs)
    }

    @Test
    fun `readEnv returns empty string for undefined environment variable`() {
        val src = "println(readEnv(\"NON_EXISTENT_ENV_VAR_12345\"));"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf(""), outputs)
    }

    @Test
    fun `evaluates complex mathematical expressions with correct operator precedence`() {
        val src =
            """
            let a: number = 2;
            let b: number = 3;
            let result: number = (a + b) * 4 / 2;
            println(result);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("10"), outputs)
    }

    @Test
    fun `boolean values convert to string on concatenation`() {
        val src =
            """
            let flag: boolean = true;
            println("status: " + flag);
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("status: true"), outputs)
    }
}
