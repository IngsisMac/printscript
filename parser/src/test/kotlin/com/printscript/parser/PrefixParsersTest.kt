package com.printscript.parser

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.Declaration
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PrefixParsersTest {
    private lateinit var version11: Version

    @BeforeEach
    fun setUp() {
        version11 = Version.V1_1
    }

    private fun parseString(source: String): List<com.printscript.ast.Statement> =
        Parser(com.printscript.lexer.Lexer(java.io.StringReader(source), version11), version11)
            .parse()
            .asSequence()
            .toList()

    @Test
    @DisplayName("Parse de operador unario menos genera operación binaria con cero")
    fun parseUnaryMinusOperator() {
        val decl = parseString("let x: number = -5;")[0] as Declaration

        val op = decl.value as BinaryOp
        assertEquals("-", op.operator)
    }

    @Test
    @DisplayName("Parse de operador unario más genera operación binaria con cero")
    fun parseUnaryPlusOperator() {
        val decl = parseString("let x: number = +5;")[0] as Declaration

        val op = decl.value as BinaryOp
        assertEquals("+", op.operator)
    }

    @Test
    @DisplayName("Parse de llamada a función sin argumentos produce CallExpression con argumento nulo")
    fun parseFunctionCallWithoutArguments() {
        val decl = parseString("let x: number = foo();")[0] as Declaration

        val call = decl.value as CallExpression
        assertEquals("foo", call.name)
        assertNull(call.argument)
    }

    @Test
    @DisplayName("Parse de readInput sin argumentos produce CallExpression con argumento nulo")
    fun parseReadInputWithoutArguments() {
        val decl = parseString("let x: string = readInput();")[0] as Declaration

        val call = decl.value as CallExpression
        assertEquals("readInput", call.name)
        assertNull(call.argument)
    }

    @Test
    @DisplayName("Parse de literal booleano false")
    fun parseFalseBooleanLiteral() {
        val decl = parseString("let flag: boolean = false;")[0] as Declaration

        val boolVal = decl.value as BooleanLiteral
        assertFalse(boolVal.value)
    }
}
