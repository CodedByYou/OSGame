package me.codedbyyou.os.server.command.interfaces

interface CommandSender {

    fun getPermissions(): List<String>

    fun hasPermission(permission: String): Boolean

    fun getName(): String

    fun isConsole(): Boolean

    fun sendMessage(message: String)
}