package com.printscript.ast

import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StatementTest {
    private lateinit var dummySpan: Span
    private lateinit var sampleExpr: Expression

    @BeforeEach
    fun setUp() {
        dummySpan = Span(Position(1, 1), Position(1, 20))
        sampleExpr = NumberLiteral("100", dummySpan)
    }

    @Test
    @DisplayName("Declaration soporta inicialización opcional y modificadores mutable/const")
    fun declarationSoportaInicializacionYConst() {
        val letDecl = Declaration("x", "number", sampleExpr, dummySpan, isConst = false)
        val constDecl = Declaration("MAX", "number", sampleExpr, dummySpan, isConst = true)
        val uninitDecl = Declaration("y", "string", null, dummySpan, isConst = false)

        assertFalse(letDecl.isConst)
        assertTrue(constDecl.isConst)
        assertNull(uninitDecl.value)
        assertEquals("x", letDecl.name)
        assertEquals("number", letDecl.type)
    }

    @Test
    @DisplayName("Assignment conserva el nombre del destino y la expresión asignada")
    fun assignmentConservaNombreYValor() {
        val assignment = Assignment("contador", sampleExpr, dummySpan)

        val targetName = assignment.name
        val assignedValue = assignment.value

        assertEquals("contador", targetName)
        assertEquals(sampleExpr, assignedValue)
    }

    @Test
    @DisplayName("PrintStatement encapsula la expresión a imprimir")
    fun printStatementEncapsulaExpresion() {
        val printStmt = PrintStatement(sampleExpr, dummySpan)

        val expr = printStmt.expression

        assertEquals(sampleExpr, expr)
    }

    @Test
    @DisplayName("IfStatement admite rama else cuando se especifica")
    fun ifStatementAdmiteRamaElse() {
        val thenStmt = PrintStatement(StringLiteral("si", dummySpan), dummySpan)
        val elseStmt = PrintStatement(StringLiteral("no", dummySpan), dummySpan)

        val ifWithElse =
            IfStatement(
                condition = BooleanLiteral(true, dummySpan),
                thenBranch = listOf(thenStmt),
                elseBranch = listOf(elseStmt),
                span = dummySpan,
            )

        assertEquals(1, ifWithElse.thenBranch.size)
        assertEquals(1, ifWithElse.elseBranch?.size)
    }

    @Test
    @DisplayName("IfStatement admite rama else nula cuando no se especifica")
    fun ifStatementAdmiteRamaElseNula() {
        val thenStmt = PrintStatement(StringLiteral("si", dummySpan), dummySpan)

        val ifWithoutElse =
            IfStatement(
                condition = BooleanLiteral(false, dummySpan),
                thenBranch = listOf(thenStmt),
                elseBranch = null,
                span = dummySpan,
            )

        assertNull(ifWithoutElse.elseBranch)
    }

    @Test
    @DisplayName("Instancias de Statement comparan su estructura correctamente")
    fun statementsTienenEqualsYHashCodeCoherente() {
        val stmt1 = Assignment("a", sampleExpr, dummySpan)
        val stmt2 = Assignment("a", sampleExpr, dummySpan)
        val stmt3 = Assignment("b", sampleExpr, dummySpan)

        assertEquals(stmt1, stmt2)
        assertEquals(stmt1.hashCode(), stmt2.hashCode())
        assertNotEquals(stmt1, stmt3)
    }
}
