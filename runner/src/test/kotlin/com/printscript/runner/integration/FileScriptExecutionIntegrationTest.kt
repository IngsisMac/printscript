package com.printscript.runner.integration

import com.printscript.common.EnvSource
import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import com.printscript.runner.PrintScriptRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.InputStreamReader

class FileScriptExecutionIntegrationTest {
    private lateinit var outputs: MutableList<String>
    private lateinit var emitter: OutputEmitter
    private lateinit var inputs: MutableMap<String, String>
    private lateinit var inputSource: InputSource
    private lateinit var envs: MutableMap<String, String>
    private lateinit var envSource: EnvSource

    @BeforeEach
    fun setUp() {
        outputs = mutableListOf()
        emitter = OutputEmitter { outputs.add(it) }
        inputs = mutableMapOf()
        inputSource = InputSource { prompt -> inputs[prompt] ?: "25" }
        envs = mutableMapOf("USER" to "test_user")
        envSource = EnvSource { key -> envs[key] }
    }

    private fun getScriptReader(path: String): InputStreamReader {
        val stream =
            javaClass.getResourceAsStream(path)
                ?: error("Resource not found: $path")
        return InputStreamReader(stream)
    }

    @Test
    @DisplayName("Ejecución de script con declaraciones e impresiones en versión 1.0")
    fun ejecucionDeScriptDeclaraciones10() {
        val reader = getScriptReader("/scripts/1.0/01_declarations.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("10", "Hello PrintScript"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con operaciones aritméticas combinadas en versión 1.0")
    fun ejecucionDeScriptAritmetica10() {
        val reader = getScriptReader("/scripts/1.0/02_arithmetic_ops.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("4"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con concatenación de string y number en versión 1.0")
    fun ejecucionDeScriptConcatenacion10() {
        val reader = getScriptReader("/scripts/1.0/03_string_concat.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("Score: 100"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con reasignaciones sucesivas en versión 1.0")
    fun ejecucionDeScriptReasignacion10() {
        val reader = getScriptReader("/scripts/1.0/04_reassignment.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("6"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con constantes y booleanos en versión 1.1")
    fun ejecucionDeScriptConstantesYBooleanos11() {
        val reader = getScriptReader("/scripts/1.1/01_const_and_booleans.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_1, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("3.14", "true", "false"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con bifurcaciones condicionales if-else en versión 1.1")
    fun ejecucionDeScriptBifurcacionesCondicionales11() {
        val reader = getScriptReader("/scripts/1.1/02_if_else_branches.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_1, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("Branch true executed"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con lectura de entrada y variables de entorno en versión 1.1")
    fun ejecucionDeScriptLecturaDeEntradaYEntorno11() {
        val reader = getScriptReader("/scripts/1.1/03_read_input_env.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_1, emitter, inputSource, envSource)

        assertTrue(result.errors.isEmpty())
        assertEquals(listOf("Enter age: ", "30", "test_user"), outputs)
    }

    @Test
    @DisplayName("Ejecución de script con error de sintaxis reporta error")
    fun ejecucionDeScriptConErrorDeSintaxisReportaError() {
        val reader = getScriptReader("/scripts/invalid/syntax_error.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertFalse(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Ejecución de script con incompatibilidad de tipos reporta error semántico")
    fun ejecucionDeScriptConIncompatibilidadDeTiposReportaError() {
        val reader = getScriptReader("/scripts/invalid/type_mismatch.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, inputSource, envSource)

        assertFalse(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Ejecución de script con reasignación a constante reporta error en versión 1.1")
    fun ejecucionDeScriptConReasignacionAConstanteReportaError() {
        val reader = getScriptReader("/scripts/invalid/const_reassignment.ps")

        val result = PrintScriptRunner.execute(reader, Version.V1_1, emitter, inputSource, envSource)

        assertFalse(result.errors.isEmpty())
    }
}
