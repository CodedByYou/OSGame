package me.codedbyyou.os.server.command.interfaces.impl

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.interfaces.CommandSender
import java.util.logging.Logger

class ConsoleCommandSender : CommandSender {

    private val logger = Logger.getLogger("ConsoleCommandSender")
    override fun getPermissions(): List<String> {
        return emptyList()
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }

    override fun getName(): String {
        return "Console"
    }

    override fun isConsole(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        logger.info(message)
    }

}