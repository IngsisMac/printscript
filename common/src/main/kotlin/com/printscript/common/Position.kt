package com.printscript.common

data class Position(val line: Int, val column: Int) {
    init {
        require(line >= 1) { "Line must be >= 1, got $line" }
        require(column >= 1) { "Column must be >= 1, got $column" }
    }

    override fun toString() = "[$line:$column]"
}
