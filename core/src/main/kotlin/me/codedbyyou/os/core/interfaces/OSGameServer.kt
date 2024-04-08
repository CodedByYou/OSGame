package me.codedbyyou.os.core.interfaces

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.ServerStatus

interface OSGameServer {

    fun start()

    val serverName: String

    val serverIP: String

    val serverPort: Int

    val serverDescription: String

    val serverStatus: ServerStatus

    val serverVersion: String

    val serverMaxPlayers: Int

    fun getOnlinePlayers(): List<Player>

    fun getOfflinePlayers(): List<Player>

    fun broadcast(message: String)

    fun getOnlinePlayerCount(): Int

    fun getOfflinePlayerCount(): Int

    fun getPlayerCount(): Int

    fun getPlayer(name: String): Player?

    fun stop()

}