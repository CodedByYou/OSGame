package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.player.manager.PlayerManager

@Command(name = "title")
@Description("Broadcasts a title to all players.")
@Usage("/title <title>")
class TitleCommand : ICommand {

    @MainCommand
    @Usage("/title <title>")
    @Description("Broadcasts a title to all players.")
    fun onCommand(sender: CommandSender, @UnparsedArguments args: List<String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /title <title>")
            return
        }
        val title = args.joinToString(" ")
        sender.sendMessage("[Title Manager] Broadcasting title to all players...")
        PlayerManager.getOnlinePlayers().forEach { player ->
            player.sendTitle(title, "", 1f);
        }
    }

}
