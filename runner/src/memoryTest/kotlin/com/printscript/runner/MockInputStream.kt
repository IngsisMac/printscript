package com.printscript.runner

import java.io.InputStream

/**
 * InputStream de prueba que genera la misma línea de PrintScript N veces,
 * devolviendo un carácter por llamada a `read()` para testear el comportamiento
 * de streaming con heap restringido (7 MB).
 *
 * TODO: Deshardcodear datos y parametrizar en el futuro para agregar más casos y subir la cobertura de tests.
 */
class MockInputStream(
    private val line: String = LINE,
    private val numberOfLines: Int = NUMBER_OF_LINES,
) : InputStream() {
    private var currentLineIndex = 0
    private var currentCharIndex = 0

    override fun read(): Int {
        if (currentLineIndex >= numberOfLines) {
            return -1
        }
        val char = line[currentCharIndex].code
        currentCharIndex++
        if (currentCharIndex >= line.length) {
            currentCharIndex = 0
            currentLineIndex++
        }
        return char
    }

    companion object {
        const val MESSAGE = "This is a text"
        const val LINE = "println(\"$MESSAGE\");\n"
        const val NUMBER_OF_LINES = 32 * 1024 // 32,768 líneas ≈ 884 KB
    }
}
