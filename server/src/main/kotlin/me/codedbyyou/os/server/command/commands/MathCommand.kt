package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.player.GamePlayer


/**
 * Example of Command System
 */
@Command("math", "m")
@Description("A command to perform math operations.")
class MathCommand : ICommand {

    @MainCommand
    @Usage("/math")
    @Permission("math")
    @Description("Main command for math operations.")
    fun main(player: GamePlayer) {
        player.sendMessage("Math command.")
    }

    @Usage("/math multiply <a> <b>")
    @SubCommand("multiply", "mul", "m")
    @Description("Multiply two numbers.")
    fun multiply(player: GamePlayer, a: Int, b: Int) {
        player.sendMessage("Multiplying $a * $b")
        player.sendMessage("Result: ${a * b}")
    }

    @SubCommand("divide", "div", "d")
    @Description("Divide two numbers.")
    @Usage("/math divide <a> <b>")
    fun divide(player: GamePlayer, a: Int, b: Int) {
        player.sendMessage("Dividing $a / $b")
        player.sendMessage("Result: ${a / b}")
    }

    @Description("Subtract two numbers.")
    @Usage("/math subtract <a> <b>")
    @SubCommand("subtract", "sub", "sb")
    fun subtract(player: GamePlayer, a: Int, b: Int) {
        player.sendMessage("Subtracting $a - $b")
        player.sendMessage("Result: ${a - b}")
    }

}