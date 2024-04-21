package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.player.GamePlayer

@Command("operator", "op")
@Description("A command to add operators.")
@Permission("*")
class OperatorCommand : ICommand{

    @MainCommand
    @Description("Main command used to add operators.")
    @Usage("/operator")
    fun main(sender: CommandSender, player: GamePlayer? = null){
        if (player == null) {
            sender.sendMessage("Usage: /operator <add|remove> <player>")
            return
        }
        player.isPlayerOp = true
        sender.sendMessage("${player.uniqueName} is now an operator.")
        player.sendMessage("[Server] You are now an operator.")
    }

}