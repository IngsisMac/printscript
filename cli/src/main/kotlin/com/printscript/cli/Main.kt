package com.printscript.cli

import picocli.CommandLine
import kotlin.system.exitProcess

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    val exitCode = CommandLine(PrintScriptCli()).execute(*args)
    exitProcess(exitCode)
}
