package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterpreterValidationTest : BaseInterpreterTest(Version.V1_1) {
    @Test
    fun `valid program in validation mode produces 0 errors and 0 outputs`() {
        val src =
            """
            let name: string = "World";
            let age: number = 20;
            if (true) {
                println("Hello " + name);
            }
            """.trimIndent()

        val errors = execute(src, isValidationMode = true)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertTrue(outputs.isEmpty(), "Validation mode must not emit any output")
    }

    @Test
    fun `invalid type assignment in validation mode reports error`() {
        val src = "let x: number = \"invalid\";"

        val errors = execute(src, isValidationMode = true)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Type mismatch"))
        assertTrue(outputs.isEmpty(), "Validation mode must not emit output on error")
    }

    @Test
    fun `invalid const reassignment in validation mode reports error`() {
        val src =
            """
            const pi: number = 3.14;
            pi = 3.14159;
            """.trimIndent()

        val errors = execute(src, isValidationMode = true)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("constant"))
    }
}
