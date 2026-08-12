package com.printscript.lexer

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.Reader

class LexerLazyTest {
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        version = Version.V1_0
    }

    private class TrackedReader(
        private val content: String,
    ) : Reader() {
        var charactersRead = 0
            private set

        override fun read(
            cbuf: CharArray,
            off: Int,
            len: Int,
        ): Int {
            if (charactersRead >= content.length) return -1
            var count = 0
            for (i in 0 until len) {
                if (charactersRead >= content.length) break
                cbuf[off + i] = content[charactersRead++]
                count++
            }
            return if (count == 0) -1 else count
        }

        override fun read(): Int {
            if (charactersRead >= content.length) return -1
            return content[charactersRead++].code
        }

        override fun close() {
            // No-op for tracked reader
        }
    }

    @Test
    @DisplayName("El lexer es perezoso y no consume el reader más allá de los tokens solicitados")
    fun lexerEsPerezosoYNoConsumeReaderMasAllaDeLoNecesario() {
        val largeSource = (1..32768).joinToString("\n") { "let x: number = $it;" }
        val reader = TrackedReader(largeSource)
        val lexer = Lexer(reader, version)

        val firstFiveTokens = (1..5).map { lexer.next() }

        assertEquals(5, firstFiveTokens.size)
        assertEquals(true, reader.charactersRead < 200)
    }
}
