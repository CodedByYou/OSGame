package me.codedbyyou.os.server.player

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.player.manager.PlayerManager
import java.time.Duration

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

    /**
     * Sends a message to the player
     * @param message the message to send
     */
    override fun sendMessage(message: String) {
        addPacket(Packet(PacketType.MESSAGE, mapOf("message" to message)))
        println("Message sent to $pseudoName: $message")
    }

    /**
     * Sends a title to the player
     * @param title the title to send
     * @param subtitle the subtitle to send
     * @see Packet
     */
    override fun sendTitle(title: String, subtitle: String, duration: Float) {
        addPacket(Packet(PacketType.TITLE, mapOf("title" to title, "subtitle" to subtitle, "duration" to duration)))
        println("Title sent to $pseudoName: $title, $subtitle")
    }

    /**
     * Sends an action bar to the player
     * @param message the message to send
     * @see Packet
     */
    override fun sendActionBar(message: String) {
        addPacket(Packet(PacketType.ACTION_BAR, mapOf("message" to message)))
        println("Action bar sent to $pseudoName: $message")
    }

    /**
     * Sends a sound to the player
     * The sound is played client-side, and the player can't hear it if they have sounds disabled or in a context where the sound can't be played
     * @param sound the sound to send
     * @see Packet
     */
    override fun sendSound(sound: String) {
        addPacket(Packet(PacketType.SOUND, mapOf("sound" to sound)))
        println("Sound sent to $pseudoName: $sound")
    }

    /**
     * Kicks the player from the server
     * @param reason the reason for the kick
     * @see Packet
     */
    override fun kick(reason: String, disconnect: Boolean){
        GlobalScope.launch {
            addPacket(Packet(PacketType.KICK, mapOf("reason" to reason)))
            println("Player $pseudoName has been kicked for $reason")
            Server.gameManager.getRoomByPlayer(uniqueName)?.removePlayer(this@GamePlayer, false)
            if (disconnect) {
                delay(1200)
                playerManager.disconnect("$pseudoName#$ticket")
            }
        }

    }

    /**
     * Sends a packet to the player
     * @param packet the packet to send
     * @see Packet
     * @deprecated("Will be removed in future versions, use the specific methods instead, will be made private in future versions and used only internally for sending packets selectively")
     */
    @Deprecated("Will be removed in future versions, use the specific methods instead")
    fun addPacket(packet: Packet) {
       playerManager.addPacket("$pseudoName#$ticket", packet)
    }

    /**
     * returns all the permissions the player has
     * @return a list of all the permissions the player has
     */
    override fun getPermissions(): List<String> {
        return permissions
    }

    /**
     * Checks if the player has a permission
     * @param permission the permission to check
     * @return true if the player has the permission, false otherwise
     * @see Player
     */
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