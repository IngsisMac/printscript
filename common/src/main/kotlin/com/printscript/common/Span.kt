package com.printscript.common

data class Span(val start: Position, val end: Position) {
    init {
        require(
            start.line < end.line ||
            (start.line == end.line && start.column <= end.column)
        ) { "Invalid span: start $start must be before end $end" }
    }

    override fun toString() = "$start-$end"
}
