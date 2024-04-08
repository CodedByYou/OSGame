package me.codedbyyou.os.server.player

import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.PacketPrefix
import me.codedbyyou.os.server.Server
import java.net.Socket

class GamePlayerClientHandler(val socket: Socket) : Runnable {

    private var nickname: String = ""
    private var ticket: String = ""
    private var player: Player? = null

    init {

    }
    override fun run() {
        println("Client connected from ${socket.inetAddress.hostAddress}")
        if (socket.isConnected){
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val buffer = ByteArray(1024)
            var read : Int
            while (input.read(buffer).also { read = it } != -1) {
                val data = String(buffer, 0, read)
                val packetType = getPacketType(data)
                if (packetType == PacketPrefix.SERVER_INFO_PING) {
                    output.write("ServerName: ${Server.serverName}\nServerIP: ${Server.serverIP}\nServerPort: ${Server.serverPort}\nServerDescription: ${Server.serverDescription}\nServerStatus: ${Server.serverStatus}\nServerVersion: ${Server.serverVersion}\nServerMaxPlayers: ${Server.serverMaxPlayers}\nOnline Players: ${Server.getOnlinePlayerCount()}".toByteArray())
                }
            }
        }
        println("Client disconnected from ${socket.inetAddress.hostAddress}") // this will never be reached

    }

}

fun getPacketType(string: String): PacketPrefix {
    val prefix = string.substring(1, string.indexOf("]"))
    return PacketPrefix.valueOf(prefix)
}