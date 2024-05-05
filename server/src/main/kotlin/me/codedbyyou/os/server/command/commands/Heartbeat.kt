package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand

@Command("heartbeat","hb")
@Description("Starts or stops the server heartbeat AFK kicking system")
class HeartbeatCommand : ICommand {
    @MainCommand
    @Usage("/heartbeat")
    @Description("Starts or stops the server heartbeat AFK kicking system")
    @Permission("os.command.heartbeat")
    fun onHeartbeat(sender: CommandSender) {
        println("Heartbeat command executed by ${sender.getName()}")
        println("Heartbeat is now ${if(Server.toggleHeartBeat()) "enabled" else "disabled"}")
        println("This is an experimental feature")
        println("Everyone will be kicked haahahahaha")
    }

}