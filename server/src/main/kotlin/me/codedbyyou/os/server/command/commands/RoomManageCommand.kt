package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand

//@Command("roommanage", "rm")//can be commented out
@SubCommand("manage",  "m")
@SubCommandFor("room")
class RoomManageCommand : ICommand {

    @MainCommand
    @Description("Main command for room management.")
    @Permission("room.manage")
    @Usage("/room manage")
    fun main(sender: CommandSender) {
        sender.sendMessage("Room manage command.")
    }

    @SubCommand("start", "s")
    @Usage("/room manage start")
    @Description("Start the room")
    fun start(sender: CommandSender) {
        sender.sendMessage("Starting room.")
    }

}