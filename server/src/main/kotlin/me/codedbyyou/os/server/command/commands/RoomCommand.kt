package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.command.interfaces.impl.ConsoleCommandSender
import me.codedbyyou.os.server.managers.GameRoomManager
import me.codedbyyou.os.server.player.GamePlayer

@Command("room", "r")
@Description("A command to manage rooms and their settings.")
class RoomCommand : ICommand {

    @MainCommand
    @Description("Main command for room management.")
    @Permission("room")
    @Usage("/room")
    fun main(player: CommandSender) {
        player.sendMessage("Room command.")
    }

    @SubCommand("create", "c")
    @Description("Create a new room.")
   // @Permission("room.create")
    @Usage("/room create <name>")
    fun createRoom(player: GamePlayer, name: String) {
        Server.gameManager.addRoom(
            name,
            "dsds",
            4,
            "4",
            5,
            5,
            5,
        )
        player.sendMessage("Room created: $name")
    }


}