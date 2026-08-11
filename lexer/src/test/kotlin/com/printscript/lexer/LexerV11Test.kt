package com.printscript.lexer

import com.printscript.common.Version
import com.printscript.token.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

class LexerV11Test {
    private val version = Version.V1_1

    private fun createLexer(source: String): Lexer = Lexer(StringReader(source), version)

    @Test
    @DisplayName("En versión 1.1 const, boolean, if, else, llaves y literales booleanos son keywords")
    fun enVersion11ConstIfElseYBooleanSonKeywords() {
        val code =
            """
            const flag: boolean = true;
            if (flag) { println("si"); } else { println("no"); }
            """.trimIndent()

        val lexer = createLexer(code)
        val tokens = lexer.asSequence().toList()

        val types = tokens.map { it.type }
        val expectedKeywords =
            listOf(
                TokenType.CONST,
                TokenType.BOOLEAN,
                TokenType.TRUE,
                TokenType.IF,
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.ELSE,
            )

        expectedKeywords.forEach { expected ->
            assert(types.contains(expected)) { "Expected token type $expected to be present in V1.1 lexer output" }
        }
    }

    @Test
    @DisplayName("En versión 1.1 readInput y readEnv son reconocidos como keywords")
    fun reconoceReadInputYReadEnvEnVersion11() {
        val code =
            """
            let a: string = readInput("Ingrese un valor");
            let b: string = readEnv("HOME");
            """.trimIndent()

        val lexer = createLexer(code)
        val types = lexer.asSequence().map { it.type }.toList()

        assertEquals(true, types.contains(TokenType.READ_INPUT))
        assertEquals(true, types.contains(TokenType.READ_ENV))
    }
}
