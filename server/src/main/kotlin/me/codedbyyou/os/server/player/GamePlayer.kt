package me.codedbyyou.os.server.player

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import java.util.concurrent.ConcurrentLinkedQueue



class GamePlayer(
    override val pseudoName: String,
    override val ticket: String,
    override val isOnline: Boolean,
    override val isPlayerBanned: Boolean,
    override val isPlayerMuted: Boolean,
    override val isPlayerOp: Boolean,
    override val isPlayerWhitelisted: Boolean,
    override val ip: String,
    override val macAddress: String,
    private val dataProcessor : (Packet) -> Unit
) : Player {

    private val dataBuffer = ConcurrentLinkedQueue<Packet>()
    override fun sendMessage(message: String) {
        dataBuffer
            .add(Packet(PacketType.MESSAGE, mapOf("message" to message)))
    }

    override fun sendTitle(title: String, subtitle: String) {
        dataBuffer.add(Packet(PacketType.TITLE, mapOf("title" to title, "subtitle" to subtitle)))
        println("Title sent to $pseudoName: $title, $subtitle")
    }

    override fun sendActionBar(message: String) {
        dataBuffer.add(Packet(PacketType.ACTION_BAR, mapOf("message" to message)))
        println("Action bar sent to $pseudoName: $message")
    }

    override fun kick(reason: String) {
        dataBuffer.add(Packet(PacketType.KICK, mapOf("reason" to reason)))
        println("Player $pseudoName has been kicked for $reason")
    }

    protected fun processData() {
        while(dataBuffer.isNotEmpty()) {
            dataProcessor(dataBuffer.poll())
        }
    }

}