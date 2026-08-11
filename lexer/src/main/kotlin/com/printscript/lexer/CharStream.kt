package com.printscript.lexer

import com.printscript.common.Position
import java.io.Reader

class CharStream(
    private val source: Reader,
) {
    var line: Int = 1
        private set
    var column: Int = 1
        private set

    private var currentChar: Int? = source.read()

    init {
        if (currentChar == -1) {
            currentChar = null
        }
    }

    fun peek(): Char? = currentChar?.toChar()

    fun hasMore(): Boolean = currentChar != null

    fun advance(): Char? {
        val prev = peek()
        if (currentChar != null) {
            currentChar = source.read()
            if (currentChar == -1) {
                currentChar = null
            }
            if (prev == '\n') {
                line++
                column = 1
            } else {
                column++
            }
        }
        return prev
    }

    fun getPosition(): Position = Position(line, column)

    fun skipWhitespace() {
        while (hasMore()) {
            when (peek()) {
                ' ', '\t', '\r', '\n' -> advance()
                else -> break
            }
        }
    }
}
