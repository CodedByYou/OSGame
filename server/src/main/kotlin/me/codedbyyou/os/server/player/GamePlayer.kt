package me.codedbyyou.os.server.player

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.PacketPrefix
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
    private val dataProcessor : (String) -> Unit
) : Player {

    private val dataBuffer = ConcurrentLinkedQueue<String>()
    override fun sendMessage(message: String) {
        dataBuffer.add("[${PacketPrefix.MESSAGE}]$message")
    }

    override fun sendTitle(title: String, subtitle: String) {
        dataBuffer.add("[${PacketPrefix.TITLE}]$title, $subtitle")
        println("Title sent to $pseudoName: $title, $subtitle")
    }

    override fun sendActionBar(message: String) {
        dataBuffer.add("[${PacketPrefix.ACTION_BAR}]$message")
        println("Action bar sent to $pseudoName: $message")
    }

    override fun kick(reason: String) {
        dataBuffer.add("[${PacketPrefix.KICK}]$reason")
        println("Player $pseudoName has been kicked for $reason")
    }

    protected fun processData() {
        while(dataBuffer.isNotEmpty()) {
            dataProcessor(dataBuffer.poll())
        }
    }

}