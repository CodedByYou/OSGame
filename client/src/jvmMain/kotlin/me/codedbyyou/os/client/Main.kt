package me.codedbyyou.os.client

import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graphics.HdpiMode
import kotlinx.coroutines.awaitAll
import me.codedbyyou.os.client.game.Game
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.dialog.Server
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.sendPacket
import java.net.Socket
import java.util.logging.Logger

fun main(args: Array<String>) {
    val size = Config.VSIZE()
    Client
    createLittleKtApp {
        this.width = size.first.toInt()
        this.height = size.second.toInt()
        vSync = true
        resizeable = true
        title = "OSGame"
        val isMac = System.getProperty("os.name").toLowerCase().contains("mac")
        if (isMac) {
            hdpiMode = HdpiMode.PIXELS
        }
    }.start {context ->
        Game(context)
    }

}

fun Server.ping(){
    val logger = Logger.getLogger("ConnectionHandler")
    try {
        val socket = Socket(this.ip, this.port)
        socket.soTimeout = 1500
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        logger.info("Connected to server "+ this.ip)
        PacketType.INFO_PING.sendPacket(output)
        val buffer = ByteArray(1024)
        logger.info("Pinging server for server information")
        val read = input.read(buffer)
        logger.info("Received data from server")
        val data = String(buffer, 0, read)
        logger.info("Received data: $data")
        val packet = Packet.fromPacket(data)
        val packetType = packet.packetType
        val packetData = packet.packetData

        logger.info("Received packet type: $packetType")
        logger.info("Received packet data: $packetData")

        this.name = (packetData["ServerName"] ?: "Unknown").toString()
        this.description = (packetData["ServerDescription"] ?: "Unknown").toString()
        this.status = (packetData["ServerStatus"] ?: "Unknown").toString()
        this.maxPlayers = packetData["ServerMaxPlayers"]?.toString()?.toInt() ?: 0
        this.onlinePlayers = packetData["Online Players"]?.toString()?.toInt() ?: 0
        socket.close()
    } catch (e: Exception) {
        logger.warning("Failed to ping server ${this.ip}")
    }
}
