package com.printscript.ast

import com.printscript.common.Span

sealed interface AstNode {
    val span: Span
}
