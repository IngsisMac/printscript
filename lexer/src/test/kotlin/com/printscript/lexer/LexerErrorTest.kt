package com.printscript.lexer

import com.printscript.common.Position
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class LexerErrorTest {
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        version = Version.V1_0
    }

    private fun createLexer(source: String): Lexer = Lexer(StringReader(source), version)

    @Test
    @DisplayName("Un string sin cerrar es un error posicionado que lanza LexerException")
    fun stringSinCerrarLanzaExcepcionPosicionada() {
        val code = """let a: string = "hola;"""
        val lexer = createLexer(code)

        val exception =
            assertThrows(LexerException::class.java) {
                lexer.asSequence().toList()
            }

        assertEquals("Unterminated string literal", exception.message)
        assertEquals(Position(1, 17), exception.span.start)
    }

    @Test
    @DisplayName("Un carácter no soportado lanza LexerException en la posición exacta")
    fun caracterNoSoportadoLanzaExcepcionPosicionada() {
        val code = "let x: number = 5 @ 3;"
        val lexer = createLexer(code)

        val exception =
            assertThrows(LexerException::class.java) {
                lexer.asSequence().toList()
            }

        assertEquals("Unexpected character '@'", exception.message)
        assertEquals(Position(1, 19), exception.span.start)
    }

    @Test
    @DisplayName("Literal numérico con múltiples puntos decimales lanza LexerException")
    fun numeroConMultiplesPuntosDecimalesLanzaExcepcion() {
        val code = "let x: number = 1.2.3;"
        val lexer = createLexer(code)

        val exception =
            assertThrows(LexerException::class.java) {
                lexer.asSequence().toList()
            }

        assertEquals(true, exception.message.contains("multiple decimal points"))
    }

    @Test
    @DisplayName("Literal numérico con punto decimal al final lanza LexerException")
    fun numeroConPuntoAlFinalLanzaExcepcion() {
        val code = "let x: number = 12.;"
        val lexer = createLexer(code)

        val exception =
            assertThrows(LexerException::class.java) {
                lexer.asSequence().toList()
            }

        assertEquals(true, exception.message.contains("trailing decimal point"))
    }
}
