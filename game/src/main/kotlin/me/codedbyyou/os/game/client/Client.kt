package me.codedbyyou.os.game.client

import dev.dejvokep.boostedyaml.YamlDocument
import me.codedbyyou.os.game.data.User
import me.codedbyyou.os.game.ui.manager.ConnectionManager
import java.io.File
import java.net.NetworkInterface


object Client {
    val macAddress =
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .firstNotNullOfOrNull { it.hardwareAddress?.joinToString("-") { byte -> String.format("%02X", byte) } }
    val homeDirectory : File = File(System.getProperty("user.home"), ".os").also { it.mkdirs() }
    val config = YamlDocument.create(File(homeDirectory, "config.yml"), this::class.java.classLoader.getResourceAsStream("config.yml"))
    val connectionManager = ConnectionManager()
    var user: User? = null
    var roomID : String? = null
}

