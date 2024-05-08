package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.player.GamePlayer

@Command("kick","k")
@Description("Kick a player from the server")
class KickCommand {
    @MainCommand
    @Description("Kick a player from the server")
    @Usage("/kick <player> <reason>")
    fun execute(sender: CommandSender, player: GamePlayer?) {
//        sender.sendMessage("[Usage] /kick <player> <reason>")

        if(player == null) {
            sender.sendMessage("Player not found")
            return
        }
        Server.gameManager.getRoomByPlayer(player.uniqueName)?.removePlayer(player)
        player.kick(sender.getName()+" has kicked you from the server", true)
    }

}