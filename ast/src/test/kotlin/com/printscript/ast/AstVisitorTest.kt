package com.printscript.ast

import com.printscript.common.Position
import com.printscript.common.Span
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AstVisitorTest {
    private lateinit var dummySpan: Span
    private lateinit var testVisitor: TestVisitor

    private class TestVisitor : AstVisitor<String> {
        override fun visit(node: Declaration): String = "Declaration:${node.name}"
        override fun visit(node: Assignment): String = "Assignment:${node.name}"
        override fun visit(node: PrintStatement): String = "PrintStatement"
        override fun visit(node: IfStatement): String = "IfStatement"
        override fun visit(node: NumberLiteral): String = "NumberLiteral:${node.value}"
        override fun visit(node: StringLiteral): String = "StringLiteral:${node.value}"
        override fun visit(node: BooleanLiteral): String = "BooleanLiteral:${node.value}"
        override fun visit(node: Variable): String = "Variable:${node.name}"
        override fun visit(node: BinaryOp): String = "BinaryOp:${node.operator}"
        override fun visit(node: CallExpression): String = "CallExpression:${node.name}"
    }

    @BeforeEach
    fun setUp() {
        dummySpan = Span(Position(1, 1), Position(1, 10))
        testVisitor = TestVisitor()
    }

    @Test
    @DisplayName("accept en Declaration invoca visit(Declaration)")
    fun acceptEnDeclarationInvocaVisitDeclaration() {
        val decl = Declaration("total", "number", null, dummySpan)

        val result = decl.accept(testVisitor)

        assertEquals("Declaration:total", result)
    }

    @Test
    @DisplayName("accept en Assignment invoca visit(Assignment)")
    fun acceptEnAssignmentInvocaVisitAssignment() {
        val assign = Assignment("a", NumberLiteral("1", dummySpan), dummySpan)

        val result = assign.accept(testVisitor)

        assertEquals("Assignment:a", result)
    }

    @Test
    @DisplayName("accept en PrintStatement invoca visit(PrintStatement)")
    fun acceptEnPrintStatementInvocaVisitPrintStatement() {
        val print = PrintStatement(StringLiteral("hi", dummySpan), dummySpan)

        val result = print.accept(testVisitor)

        assertEquals("PrintStatement", result)
    }

    @Test
    @DisplayName("accept en IfStatement invoca visit(IfStatement)")
    fun acceptEnIfStatementInvocaVisitIfStatement() {
        val ifNode = IfStatement(BooleanLiteral(true, dummySpan), emptyList(), null, dummySpan)

        val result = ifNode.accept(testVisitor)

        assertEquals("IfStatement", result)
    }

    @Test
    @DisplayName("accept en NumberLiteral invoca visit(NumberLiteral)")
    fun acceptEnNumberLiteralInvocaVisitNumberLiteral() {
        val num = NumberLiteral("123", dummySpan)

        val result = num.accept(testVisitor)

        assertEquals("NumberLiteral:123", result)
    }

    @Test
    @DisplayName("accept en StringLiteral invoca visit(StringLiteral)")
    fun acceptEnStringLiteralInvocaVisitStringLiteral() {
        val str = StringLiteral("text", dummySpan)

        val result = str.accept(testVisitor)

        assertEquals("StringLiteral:text", result)
    }

    @Test
    @DisplayName("accept en BooleanLiteral invoca visit(BooleanLiteral)")
    fun acceptEnBooleanLiteralInvocaVisitBooleanLiteral() {
        val bool = BooleanLiteral(false, dummySpan)

        val result = bool.accept(testVisitor)

        assertEquals("BooleanLiteral:false", result)
    }

    @Test
    @DisplayName("accept en Variable invoca visit(Variable)")
    fun acceptEnVariableInvocaVisitVariable() {
        val variable = Variable("myVar", dummySpan)

        val result = variable.accept(testVisitor)

        assertEquals("Variable:myVar", result)
    }

    @Test
    @DisplayName("accept en BinaryOp invoca visit(BinaryOp)")
    fun acceptEnBinaryOpInvocaVisitBinaryOp() {
        val binary = BinaryOp(NumberLiteral("1", dummySpan), "+", NumberLiteral("2", dummySpan), dummySpan)

        val result = binary.accept(testVisitor)

        assertEquals("BinaryOp:+", result)
    }

    @Test
    @DisplayName("accept en CallExpression invoca visit(CallExpression)")
    fun acceptEnCallExpressionInvocaVisitCallExpression() {
        val call = CallExpression("readEnv", StringLiteral("KEY", dummySpan), dummySpan)

        val result = call.accept(testVisitor)

        assertEquals("CallExpression:readEnv", result)
    }
}
