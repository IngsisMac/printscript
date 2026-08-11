package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.io.FileReader
import java.util.stream.Stream

class GoldenFilesTest {
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
    fun `run golden validation test case`(testCase: GoldenTestCase) {
        val output = StringBuilder()
        val emitter = OutputEmitter { output.append(it) }
        val input = InputSource { "" }

        val reader = FileReader(testCase.inputPath)
        val result = PrintScriptRunner.execute(reader, Version.V1_0, emitter, input)
        val expectedError = testCase.expectedErrorsPath.readText().trim()

        if (expectedError == "# NONE" || expectedError.isEmpty()) {
            assertTrue(
                result.errors.isEmpty(),
                "Case '${testCase.name}' expected 0 errors but got: ${result.errors}",
            )
        } else {
            assertTrue(
                result.errors.isNotEmpty(),
                "Case '${testCase.name}' expected error matching '$expectedError' but got 0 errors",
            )
            val matches = result.errors.any { it.message.contains(expectedError, ignoreCase = true) }
            assertTrue(
                matches,
                "Case '${testCase.name}' expected error message containing '$expectedError' but got: ${result.errors}",
            )
        }
    }
}
