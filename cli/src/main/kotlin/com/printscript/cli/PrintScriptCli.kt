package com.printscript.cli

import com.printscript.cli.commands.AnalyzeCommand
import com.printscript.cli.commands.ExecuteCommand
import com.printscript.cli.commands.FormatCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import java.util.concurrent.Callable

@Command(
    name = "printscript",
    description = ["CLI oficial de PrintScript para ejecutar, formatear y analizar código."],
    mixinStandardHelpOptions = true,
    version = ["PrintScript 1.0.0"],
    subcommands = [
        ExecuteCommand::class,
        FormatCommand::class,
        AnalyzeCommand::class,
    ],
)
class PrintScriptCli : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    override fun call(): Int {
        val out = spec?.commandLine()?.out ?: java.io.PrintWriter(System.out, true)
        out.println("PrintScript CLI - Usar --help para ver los comandos disponibles.")
        return 0
    }
}
