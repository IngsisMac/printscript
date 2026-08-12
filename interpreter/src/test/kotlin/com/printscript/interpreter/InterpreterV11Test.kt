package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class InterpreterV11Test : BaseInterpreterTest(Version.V1_1) {
    @Test
    @DisplayName("Reasignar una constante genera error")
    fun reassigningConstIsAnError() {
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
    @DisplayName("Sentencia if ejecuta la rama correcta")
    fun ifExecutesCorrectBranch() {
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
    @DisplayName("Condición de sentencia if debe ser de tipo boolean")
    fun ifConditionMustBeBoolean() {
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
    @DisplayName("readInput toma el valor ingresado por el proveedor de entradas")
    fun readInputTakesValueFromProvider() {
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
    @DisplayName("readInput falla si la conversión de tipo de datos no es válida")
    fun readInputFailsIfTypeConversionFails() {
        val src = "let flag: boolean = readInput(\"dame un boolean: \");"
        inputs["dame un boolean: "] = "Hola"

        val errors = execute(src)

        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("boolean"), "Expected type conversion error")
    }

    @Test
    @DisplayName("readEnv lee correctamente una variable de entorno existente")
    fun readEnvReadsEnvironmentVariable() {
        val src = "println(readEnv(\"PATH\"));"

        val errors = execute(src)

        assertTrue(errors.isEmpty(), "Unexpected errors: ${errors.map { it.message }}")
        assertEquals(1, outputs.size)
        assertTrue(outputs[0].isNotEmpty())
    }
}
