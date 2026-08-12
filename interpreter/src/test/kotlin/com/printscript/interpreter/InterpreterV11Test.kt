package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterpreterV11Test : BaseInterpreterTest(Version.V1_1) {
    @Test
    fun `PS-INT-013 - reassigning a const is an error`() {
        val src =
            """
            const x: number = 1;
            x = 2;
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("constant"), "Expected error message about constant re-assignment")
    }

    @Test
    fun `PS-INT-014 - if executes the correct branch`() {
        val src =
            """
            let flag: boolean = true;
            if (flag) {
                println("si");
            } else {
                println("no");
            }
            """.trimIndent()

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("si"), outputs)
    }

    @Test
    fun `PS-INT-015 - if condition must be a boolean`() {
        val src =
            """
            let n: number = 1;
            if (n) {
                println("si");
            }
            """.trimIndent()

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("boolean"), "Expected error about non-boolean if condition")
    }

    @Test
    fun `PS-INT-016 - readInput takes value from provider`() {
        val src =
            """
            let n: number = readInput("Ingresá un número: ");
            println(n);
            """.trimIndent()
        inputs["Ingresá un número: "] = "42"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(listOf("Ingresá un número: ", "42"), outputs)
    }

    @Test
    fun `PS-INT-017 - readInput fails if type conversion fails`() {
        val src = "let flag: boolean = readInput(\"dame un boolean: \");"
        inputs["dame un boolean: "] = "Hola"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("boolean"), "Expected type conversion error")
    }

    @Test
    fun `PS-INT-018 - readEnv reads environment variable`() {
        val src = "println(readEnv(\"PATH\"));"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(1, outputs.size)
        assertTrue(outputs[0].isNotEmpty())
    }
}
