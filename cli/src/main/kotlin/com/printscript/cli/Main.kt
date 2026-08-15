package com.printscript.cli

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val exitCode = CommandLine(PrintScriptCli()).execute(*args)
    exitProcess(exitCode)
}
