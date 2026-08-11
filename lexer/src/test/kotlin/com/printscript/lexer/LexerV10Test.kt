package com.printscript.lexer

import com.printscript.common.Version
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class LexerV10Test {
    private val version = Version.V1_0

    private fun createLexer(source: String): Lexer = Lexer(StringReader(source), version)

    @Test
    @DisplayName("Declaración simple produce la secuencia de tokens esperada")
    fun declaracionSimpleProduceSecuenciaDeTokensEsperada() {
        val lexer = createLexer("let x: number = 5;")
        val tokens = lexer.asSequence().toList()

        val expectedTypes =
            listOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.NUMBER,
                TokenType.EQUAL,
                TokenType.NUMBER_LITERAL,
                TokenType.SEMICOLON,
            )

        assertEquals(expectedTypes, tokens.map { it.type })
        assertEquals("x", tokens[1].lexeme)
        assertEquals("5", tokens[5].lexeme)
    }

    @Test
    @DisplayName("Los strings aceptan comillas simples y dobles sin incluir comillas en el valor")
    fun stringAceptaComillasDoblesYSimplesSinIncluirComillasEnLiteral() {
        val code =
            """
            let a: string = "hola";
            let b: string = 'chau';
            """.trimIndent()

        val lexer = createLexer(code)
        val stringTokens = lexer.asSequence().filter { it.type == TokenType.STRING_LITERAL }.toList()

        assertEquals(2, stringTokens.size)
        assertEquals("hola", stringTokens[0].lexeme)
        assertEquals("chau", stringTokens[1].lexeme)
    }

    @Test
    @DisplayName("Los números incluyen enteros y decimales")
    fun numerosIncluyenEnterosYDecimales() {
        val code =
            """
            let a: number = 12;
            let b: number = 3.14;
            """.trimIndent()

        val lexer = createLexer(code)
        val numberTokens = lexer.asSequence().filter { it.type == TokenType.NUMBER_LITERAL }.toList()

        assertEquals(2, numberTokens.size)
        assertEquals("12", numberTokens[0].lexeme)
        assertEquals("3.14", numberTokens[1].lexeme)
    }

    @Test
    @DisplayName("Los cuatro operadores aritméticos se reconocen en orden")
    fun cuatroOperadoresAritmeticosSeReconocenEnOrden() {
        val lexer = createLexer("let a: number = 1 + 2 - 3 * 4 / 5;")
        val opTokens =
            lexer
                .asSequence()
                .filter {
                    it.type in listOf(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH)
                }.map { it.type }
                .toList()

        val expectedOps = listOf(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH)
        assertEquals(expectedOps, opTokens)
    }

    @Test
    @DisplayName("El espaciado y formato no alteran la secuencia de tipos de tokens")
    fun espaciadoYFormatoNoAlteranSecuenciaDeTokens() {
        val lexer1 = createLexer("let x:number=5;")
        val lexer2 = createLexer("let   x  :  number  =  5 ;")

        val types1 = lexer1.asSequence().map { it.type }.toList()
        val types2 = lexer2.asSequence().map { it.type }.toList()

        assertEquals(types1, types2)
    }

    @Test
    @DisplayName("println es un token propio de tipo PRINTLN y no un identificador")
    fun printlnEsTokenPropioYNoIdentificador() {
        val lexer = createLexer("println(5);")
        val firstToken = lexer.next()

        assertEquals(TokenType.PRINTLN, firstToken.type)
        assertEquals("println", firstToken.lexeme)
    }

    @Test
    @DisplayName("En versión 1.0 const no es keyword y se reconoce como identificador")
    fun enVersion10ConstSeReconoceComoIdentificador() {
        val lexer = createLexer("const x: number = 5;")
        val firstToken = lexer.next()

        assertEquals(TokenType.IDENTIFIER, firstToken.type)
        assertEquals("const", firstToken.lexeme)
    }
}
