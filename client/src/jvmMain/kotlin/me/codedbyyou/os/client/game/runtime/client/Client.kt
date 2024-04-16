package me.codedbyyou.os.client.game.runtime.client

import dev.dejvokep.boostedyaml.YamlDocument
import me.codedbyyou.os.client.game.enums.GameMode
import me.codedbyyou.os.client.game.enums.GameState
import me.codedbyyou.os.client.game.manager.ConnectionManager
import java.io.File
import java.net.NetworkInterface

object Client {
    val macAddress = NetworkInterface.getNetworkInterfaces().toList().flatMap { it.hardwareAddress?.toList() ?: emptyList() }.joinToString("-") { String.format("%02X", it) }
    val homeDirectory : File = File(System.getProperty("user.home"), ".os").also { it.mkdirs() }
    val config = YamlDocument.create(File(homeDirectory, "config.yml"), this::class.java.classLoader.getResourceAsStream("config.yml"))
    val connectionManager = ConnectionManager()
    var user: User? = null
    var gameState = GameState.MENU
    var gameMode = GameMode.SINGLE_PLAYER
}


data class User(
    val psuedoName: String,
    val ticket: String,
    var inputLastMoved: Long = System.currentTimeMillis()
){
    fun isAFK() = System.currentTimeMillis() - inputLastMoved > 1000 * 60 * 5

    val nickTicket
        get() = "$psuedoName#$ticket"
}