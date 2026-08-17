package com.printscript.lexer

import com.printscript.common.Position
import com.printscript.common.Version
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class LexerPositionTest {
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        version = Version.V1_0
    }

    private fun createLexer(source: String): Lexer = Lexer(StringReader(source), version)

    @Test
    @DisplayName("Cada token conoce su posición de inicio y de fin (Span)")
    fun cadaTokenConoceSuPosicionDeInicioYFin() {
        val lexer = createLexer("let x: number = 5;")

        val firstToken = lexer.next()

        assertEquals(TokenType.LET, firstToken.type)
        assertEquals(Position(1, 1), firstToken.span.start)
        assertEquals(Position(1, 3), firstToken.span.end)
    }

    @Test
    @DisplayName("Las posiciones de los tokens son correctas en la segunda línea")
    fun posicionesSonCorrectasEnSegundaLinea() {
        val code = "let a: number = 1;\nlet b: number = 2;"
        val lexer = createLexer(code)

        val tokens = lexer.asSequence().toList()
        val secondLetToken = tokens[7] // index 7 is second 'let'

        assertEquals(TokenType.LET, secondLetToken.type)
        assertEquals(Position(2, 1), secondLetToken.span.start)
        assertEquals(Position(2, 3), secondLetToken.span.end)
    }

    @Test
    @DisplayName("Las posiciones de un string multilínea son correctas")
    fun posicionesCorrectasEnStringMultiline() {
        val code = "\"hola\nmundo\""
        val lexer = createLexer(code)

        val token = lexer.next()

        assertEquals(TokenType.STRING_LITERAL, token.type)
        assertEquals(Position(1, 1), token.span.start)
        assertEquals(Position(2, 6), token.span.end)
    }
}
