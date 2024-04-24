package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.player.manager.PlayerManager

@Command(name = "broadcast", aliases = ["bc"])
@Description(description = "Broadcast a message to all connected clients.")
internal class BroadcastCommand : ICommand {
    @MainCommand
    @Description(description = "Broadcast a message to all connected clients.")
    @Usage(usage = "/broadcast <message>")
    fun broadcast(sender: CommandSender?, @UnparsedArguments args: List<String>) {
        if (args.size < 2) {
            println("Usage: broadcast <message>")
            return
        }
        PlayerManager.broadcastMessage(args.joinToString(" "))
    }
}
