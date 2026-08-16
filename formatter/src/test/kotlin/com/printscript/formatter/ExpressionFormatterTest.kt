package com.printscript.formatter

import com.printscript.ast.BinaryOp
import com.printscript.ast.BooleanLiteral
import com.printscript.ast.CallExpression
import com.printscript.ast.NumberLiteral
import com.printscript.ast.StringLiteral
import com.printscript.ast.Variable
import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ExpressionFormatterTest {
    private lateinit var dummySpan: Span
    private lateinit var defaultConfig: FormatterConfig
    private lateinit var noSpaceConfig: FormatterConfig

    @BeforeEach
    fun setUp() {
        dummySpan = Span(Position(1, 1), Position(1, 10))
        defaultConfig = FormatterConfig(mandatorySpaceSurroundingOperations = true)
        noSpaceConfig = FormatterConfig(mandatorySpaceSurroundingOperations = false)
    }

    @Test
    @DisplayName("Formateo de literales numéricos, de texto y booleanos")
    fun formateoDeLiterales() {
        val numberExpr = NumberLiteral("42", dummySpan)
        val stringExpr = StringLiteral("Hola Mundo", dummySpan)
        val booleanExpr = BooleanLiteral(true, dummySpan)

        val numberResult = ExpressionFormatter.format(numberExpr, defaultConfig)
        val stringResult = ExpressionFormatter.format(stringExpr, defaultConfig)
        val booleanResult = ExpressionFormatter.format(booleanExpr, defaultConfig)

        assertEquals("42", numberResult)
        assertEquals("\"Hola Mundo\"", stringResult)
        assertEquals("true", booleanResult)
    }

    @Test
    @DisplayName("Formateo de variables simples")
    fun formateoDeVariables() {
        val variableExpr = Variable("totalSum", dummySpan)

        val result = ExpressionFormatter.format(variableExpr, defaultConfig)

        assertEquals("totalSum", result)
    }

    @Test
    @DisplayName("Formateo de llamadas a funciones con y sin argumento")
    fun formateoDeLlamadasAFunciones() {
        val callWithArg = CallExpression("readInput", StringLiteral("Ingrese valor:", dummySpan), dummySpan)
        val callWithoutArg = CallExpression("readEnv", null, dummySpan)

        val resultWithArg = ExpressionFormatter.format(callWithArg, defaultConfig)
        val resultWithoutArg = ExpressionFormatter.format(callWithoutArg, defaultConfig)

        assertEquals("readInput(\"Ingrese valor:\")", resultWithArg)
        assertEquals("readEnv()", resultWithoutArg)
    }

    @Test
    @DisplayName("Formateo de operaciones binarias con espacios alrededor de operadores")
    fun formateoDeOperacionesBinariasConEspacios() {
        val binaryOp = BinaryOp(Variable("a", dummySpan), "+", NumberLiteral("10", dummySpan), dummySpan)

        val result = ExpressionFormatter.format(binaryOp, defaultConfig)

        assertEquals("a + 10", result)
    }

    @Test
    @DisplayName("Formateo de operaciones binarias sin espacios alrededor de operadores")
    fun formateoDeOperacionesBinariasSinEspacios() {
        val binaryOp = BinaryOp(Variable("a", dummySpan), "*", NumberLiteral("5", dummySpan), dummySpan)

        val result = ExpressionFormatter.format(binaryOp, noSpaceConfig)

        assertEquals("a*5", result)
    }

    @Test
    @DisplayName("Precedencia de operadores agrega paréntesis en expresiones compuestas")
    fun precedenciaDeOperadoresAgregaParentesis() {
        val addition = BinaryOp(NumberLiteral("1", dummySpan), "+", NumberLiteral("2", dummySpan), dummySpan)
        val multiplication = BinaryOp(addition, "*", NumberLiteral("3", dummySpan), dummySpan)

        val result = ExpressionFormatter.format(multiplication, defaultConfig)

        assertEquals("(1 + 2) * 3", result)
    }

    @Test
    @DisplayName("Asociatividad por la derecha en resta requiere paréntesis")
    fun asociatividadPorLaDerechaEnRestaRequiereParentesis() {
        val rightSub = BinaryOp(NumberLiteral("5", dummySpan), "-", NumberLiteral("2", dummySpan), dummySpan)
        val leftSub = BinaryOp(NumberLiteral("10", dummySpan), "-", rightSub, dummySpan)

        val result = ExpressionFormatter.format(leftSub, defaultConfig)

        assertEquals("10 - (5 - 2)", result)
    }

    @Test
    @DisplayName("Operaciones con misma precedencia por la izquierda no requieren paréntesis innecesarios")
    fun asociatividadPorLaIzquierdaSinParentesisInnecesarios() {
        val leftAdd = BinaryOp(NumberLiteral("1", dummySpan), "+", NumberLiteral("2", dummySpan), dummySpan)
        val totalAdd = BinaryOp(leftAdd, "+", NumberLiteral("3", dummySpan), dummySpan)

        val result = ExpressionFormatter.format(totalAdd, defaultConfig)

        assertEquals("1 + 2 + 3", result)
    }
}
