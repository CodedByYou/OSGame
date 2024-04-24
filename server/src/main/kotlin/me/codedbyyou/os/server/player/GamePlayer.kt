package me.codedbyyou.os.server.player

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.player.manager.PlayerManager
import java.util.concurrent.ConcurrentLinkedQueue



class GamePlayer(
    override val pseudoName: String,
    override val ticket: String,
    override val isOnline: Boolean,
    override val isPlayerBanned: Boolean,
    override val isPlayerMuted: Boolean,
    override var isPlayerOp: Boolean,
    override val isPlayerWhitelisted: Boolean,
    override val ip: String,
    override val macAddress: String
) : Player, CommandSender {

    private val playerManager = PlayerManager
    private val permissions = mutableListOf<String>()

    override fun sendMessage(message: String) {
        addPacket(Packet(PacketType.MESSAGE, mapOf("message" to message)))
        println("Message sent to $pseudoName: $message")
    }

    override fun sendTitle(title: String, subtitle: String) {
        addPacket(Packet(PacketType.TITLE, mapOf("title" to title, "subtitle" to subtitle)))
        println("Title sent to $pseudoName: $title, $subtitle")
    }

    override fun sendActionBar(message: String) {
        addPacket(Packet(PacketType.ACTION_BAR, mapOf("message" to message)))
        println("Action bar sent to $pseudoName: $message")
    }

    override fun kick(reason: String) {
        addPacket(Packet(PacketType.KICK, mapOf("reason" to reason)))
        println("Player $pseudoName has been kicked for $reason")
    }

    private fun addPacket(packet: Packet) {
       playerManager.addPacket("$pseudoName#$ticket", packet)
    }

    override fun getPermissions(): List<String> {
        return permissions
    }

    override fun hasPermission(permission: String): Boolean {
        if (isPlayerOp) return true
        val hasPermission = permissions.any {
            it == permission || it == "*" || it == ""
        }
        if (!hasPermission) {
            val permissionParts = permission.split(".")
            for (i in permissionParts.indices) {
                val permissionPart = permissionParts.subList(0, i).joinToString(".")
                if (permissions.contains("$permissionPart.*")) {
                    return true
                }
            }
        }
        return hasPermission
    }

    override fun getName(): String {
        return uniqueName
    }

    override fun isConsole(): Boolean {
        return false
    }


}