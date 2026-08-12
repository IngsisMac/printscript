package com.printscript.interpreter

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.PrintScriptError
import com.printscript.common.Version
import com.printscript.lexer.Lexer
import com.printscript.parser.Parser
import org.junit.jupiter.api.BeforeEach
import java.io.StringReader

abstract class BaseInterpreterTest(
    private val defaultVersion: Version = Version.V1_0,
) {
    protected val outputs = mutableListOf<String>()
    protected val inputs = mutableMapOf<String, String>()
    protected lateinit var emitter: OutputEmitter
    protected lateinit var inputSource: InputSource

    @BeforeEach
    fun setUp() {
        outputs.clear()
        inputs.clear()
        emitter = OutputEmitter { outputs.add(it) }
        inputSource = InputSource { prompt -> inputs[prompt] ?: "" }
    }

    protected fun execute(
        source: String,
        version: Version = defaultVersion,
        isValidationMode: Boolean = false,
    ): List<PrintScriptError> {
        val lexer = Lexer(StringReader(source), version)
        val parser = Parser(lexer, version)
        val statements = parser.parse()
        val interpreter = Interpreter(version, emitter, inputSource, isValidationMode = isValidationMode)
        return interpreter.execute(statements)
    }
}
