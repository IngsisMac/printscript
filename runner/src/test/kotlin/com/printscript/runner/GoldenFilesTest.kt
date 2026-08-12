package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.io.FileReader
import java.util.stream.Stream

class GoldenFilesTest {
    private lateinit var output: StringBuilder
    private lateinit var emitter: OutputEmitter
    private lateinit var input: InputSource
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        output = StringBuilder()
        emitter = OutputEmitter { output.append(it) }
        input = InputSource { "" }
        version = Version.V1_0
    }

    data class GoldenTestCase(
        val name: String,
        val inputPath: File,
        val expectedErrorsPath: File,
    ) {
        override fun toString(): String = name
    }

    companion object {
        @JvmStatic
        fun goldenTestCases(): Stream<GoldenTestCase> {
            val goldenDir = File("src/test/resources/golden/1.0")
            val caseDirs = goldenDir.listFiles { file -> file.isDirectory } ?: emptyArray()

            return caseDirs
                .map { dir ->
                    GoldenTestCase(
                        name = dir.name,
                        inputPath = File(dir, "input.ps"),
                        expectedErrorsPath = File(dir, "expected_errors.txt"),
                    )
                }.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("goldenTestCases")
    @DisplayName("Ejecuta caso de prueba golden file y verifica reporte de errores")
    fun runGoldenValidationTestCase(testCase: GoldenTestCase) {
        val reader = FileReader(testCase.inputPath)
        val expectedError = testCase.expectedErrorsPath.readText().trim()

        val result = PrintScriptRunner.execute(reader, version, emitter, input)

        verifyExpectedError(result, expectedError, testCase.name)
    }

    private fun verifyExpectedError(
        result: ExecutionResult,
        expectedError: String,
        name: String,
    ) {
        if (expectedError == "# NONE" || expectedError.isEmpty()) {
            assertTrue(
                result.errors.isEmpty(),
                "Case '$name' expected 0 errors but got: ${result.errors}",
            )
        } else {
            val matches = result.errors.any { it.message.contains(expectedError, ignoreCase = true) }
            assertTrue(
                matches,
                "Case '$name' expected error message containing '$expectedError' but got: ${result.errors}",
            )
        }
    }
}
