package com.printscript.cli

import com.printscript.cli.commands.AnalyzeCommand
import com.printscript.cli.commands.DemoCommand
import com.printscript.cli.commands.ExecuteCommand
import com.printscript.cli.commands.FormatCommand
import com.printscript.cli.commands.ValidateCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import java.util.concurrent.Callable

@Command(
    name = "printscript",
    description = ["CLI oficial de PrintScript para validar, ejecutar, formatear y analizar código."],
    mixinStandardHelpOptions = true,
    version = ["PrintScript 1.0.0"],
    subcommands = [
        ValidateCommand::class,
        ExecuteCommand::class,
        FormatCommand::class,
        AnalyzeCommand::class,
        DemoCommand::class,
    ],
)
class PrintScriptCli : Callable<Int> {
    @Spec
    var spec: CommandSpec? = null

    override fun call(): Int {
        val demo = DemoCommand()
        demo.spec = spec
        return demo.call()
    }
}
