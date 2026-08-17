package com.printscript.parser.statement

import com.printscript.ast.Statement
import com.printscript.parser.Parser
import com.printscript.parser.TokenStream

interface StatementParser {
    fun matches(stream: TokenStream): Boolean

    fun parse(
        stream: TokenStream,
        parser: Parser,
    ): Statement
}
