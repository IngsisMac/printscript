package com.printscript.ast

import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ExpressionTest {
    private lateinit var dummySpan: Span
    private lateinit var leftNum: NumberLiteral
    private lateinit var rightNum: NumberLiteral

    @BeforeEach
    fun setUp() {
        dummySpan = Span(Position(1, 1), Position(1, 10))
        leftNum = NumberLiteral("10", dummySpan)
        rightNum = NumberLiteral("20", dummySpan)
    }

    @Test
    @DisplayName("NumberLiteral conserva valor numérico como cadena y span correcto")
    fun numberLiteralConservavalorYSpan() {
        val node = NumberLiteral("42.5", dummySpan)

        val value = node.value
        val span = node.span

        assertEquals("42.5", value)
        assertEquals(dummySpan, span)
    }

    @Test
    @DisplayName("StringLiteral conserva el contenido del string y su posición")
    fun stringLiteralConservaContenidoYSpan() {
        val node = StringLiteral("Hola Mundo", dummySpan)

        val value = node.value
        val span = node.span

        assertEquals("Hola Mundo", value)
        assertEquals(dummySpan, span)
    }

    @Test
    @DisplayName("BooleanLiteral conserva valores booleanos verdaderos y falsos")
    fun booleanLiteralConservaValoresBooleanos() {
        val trueNode = BooleanLiteral(true, dummySpan)
        val falseNode = BooleanLiteral(false, dummySpan)

        val trueVal = trueNode.value
        val falseVal = falseNode.value

        assertTrue(trueVal)
        assertEquals(false, falseVal)
    }

    @Test
    @DisplayName("Variable conserva el nombre del identificador y su span")
    fun variableConservaNombreYSpan() {
        val node = Variable("miVariable", dummySpan)

        val name = node.name
        val span = node.span

        assertEquals("miVariable", name)
        assertEquals(dummySpan, span)
    }

    @Test
    @DisplayName("BinaryOp permite construir expresiones binarias compuestas")
    fun binaryOpPermiteConstruirArbolDeExpresiones() {
        val binaryNode = BinaryOp(leftNum, "+", rightNum, dummySpan)

        val left = binaryNode.left
        val op = binaryNode.operator
        val right = binaryNode.right

        assertEquals(leftNum, left)
        assertEquals("+", op)
        assertEquals(rightNum, right)
    }

    @Test
    @DisplayName("CallExpression admite invocaciones con y sin argumentos")
    fun callExpressionAdmiteArgumentoOpcional() {
        val withArg = CallExpression("readInput", StringLiteral("Prompt: ", dummySpan), dummySpan)
        val withoutArg = CallExpression("readInput", null, dummySpan)

        val argPresent = withArg.argument
        val argNull = withoutArg.argument

        assertEquals(StringLiteral("Prompt: ", dummySpan), argPresent)
        assertNull(argNull)
    }

    @Test
    @DisplayName("Instancias de Expression con mismos valores son iguales por data class")
    fun expresionesIgualesTienenMismoEqualsYHashCode() {
        val node1 = Variable("x", dummySpan)
        val node2 = Variable("x", dummySpan)
        val node3 = Variable("y", dummySpan)

        assertEquals(node1, node2)
        assertEquals(node1.hashCode(), node2.hashCode())
        assertNotEquals(node1, node3)
    }
}
