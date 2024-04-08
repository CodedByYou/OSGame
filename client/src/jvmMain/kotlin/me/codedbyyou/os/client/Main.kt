package me.codedbyyou.os.client

import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graphics.HdpiMode
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.ServerChoosal
import me.codedbyyou.os.client.ui.dialog.Server
import me.codedbyyou.os.core.interfaces.server.PacketPrefix
import java.net.Socket

fun main(args: Array<String>) {
    val size = Config.VSIZE()
    createLittleKtApp {
        this.width = size.first.toInt()
        this.height = size.second.toInt()
        vSync = false
        resizeable = false
        title = "OSGame"
        val isMac = System.getProperty("os.name").toLowerCase().contains("mac")
        if (isMac) {
            hdpiMode = HdpiMode.PIXELS
        }
    }.start {
        Assets.createInstance(context = it){}
        ServerChoosal(it)
    }

}

fun Server.ping() {
    try {
        val socket = Socket(this.ip, this.port)
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        println("Connected to server")
        output.write("[${PacketPrefix.SERVER_INFO_PING}]".toByteArray())
        val buffer = ByteArray(1024)
        println("Pinging server for server information")
        val read = input.read(buffer)
        println("Received data from server")
        val data = String(buffer, 0, read).split("\n")
        for (line in data) {
            val split = line.split(":")
            when (split[0]) {
                "ServerName" -> this.name = split[1]
                "ServerDescription" -> this.description = split[1]
                "ServerStatus" -> this.status = split[1]
                "ServerMaxPlayers" -> this.maxPlayers = split[1].toInt()
                "Online Players" -> this.onlinePlayers = split[1].toInt()
            }
        }
        socket.close()
    } catch (e: Exception) {
        println("Failed to connect to server")
    }
}
