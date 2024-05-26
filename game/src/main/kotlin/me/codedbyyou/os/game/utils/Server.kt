package me.codedbyyou.os.game.utils

import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.sendPacket
import me.codedbyyou.os.game.data.Server
import java.net.Socket

fun Server.ping(){
    val logger = Logger.network;
    try {
        val socket = Socket(this.ip, this.port)
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        PacketType.INFO_PING.sendPacket(output)
        val buffer = ByteArray(1024)
        logger.info{"Pinging server ${this.ip} for server information"}
        val read = input.read(buffer)
        val data = String(buffer, 0, read)
        val packet = Packet.fromPacket(data)
        val packetData = packet.packetData
        this.name = (packetData["ServerName"] ?: "Unknown").toString()
        this.description = (packetData["ServerDescription"] ?: "Unknown").toString()
        this.status = (packetData["ServerStatus"] ?: "Unknown").toString()
        this.maxPlayers = packetData["ServerMaxPlayers"]?.toString()?.toInt() ?: 0
        this.onlinePlayers = packetData["Online Players"]?.toString()?.toInt() ?: 0
        socket.close()
    } catch (e: Exception) {
        logger.warn { "Failed to ping server ${this.ip}" }
        this.name = null
    }
}
