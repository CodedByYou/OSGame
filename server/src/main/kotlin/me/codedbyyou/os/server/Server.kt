package me.codedbyyou.os.server

import dev.dejvokep.boostedyaml.YamlDocument
import me.codedbyyou.os.core.interfaces.OSGameServer
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.ServerStatus
import me.codedbyyou.os.server.events.manager.EventManager
import me.codedbyyou.os.server.player.manager.PlayerManager
import me.codedbyyou.os.server.player.GamePlayerClientHandler
import java.io.File
import java.net.ServerSocket
import java.util.Scanner
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.system.exitProcess


object Server : OSGameServer {

    val workingDirectory = File(getCurrentWorkingDirectory())
    private var doesConfigExist = File(workingDirectory, "config.yml").exists()
    var config = YamlDocument.create(File(workingDirectory, "config.yml"), this::class.java.classLoader.getResourceAsStream("config.yml"))

    private var status: ServerStatus = ServerStatus.STARTING
    private var socketServer: ServerSocket? = null
    private val threadExecutorPool = Executors.newFixedThreadPool(serverMaxPlayers)

    val eventsManager = EventManager()
    val logger = Logger.getLogger(Server::class.java.name)


    init {
        status = ServerStatus.STARTING
        logger.info("Server initialized")
        if (doesConfigExist) {
            logger.info("Config file already exists")
        } else {
            logger.info("Config file created")
            logger.info("Please fill the config file with the server information and restart the server.")
            exitProcess(0)
        }
        // Calling the object so it gets initialized properly
        PlayerManager
        val connectedIPs = mutableListOf<String>()
        socketServer = ServerSocket(serverPort);
        Executors.newFixedThreadPool(1).submit {
            while (true) {
                try {
                    val socket = socketServer!!.accept()
                    println("Client connected from ${socket.inetAddress.hostAddress}")
                    connectedIPs.add(socket.inetAddress.hostAddress)
                    threadExecutorPool.submit(GamePlayerClientHandler(socket))
                } catch (e: Exception) {
                    connectedIPs.remove(socketServer!!.inetAddress.hostAddress)

                    logger.severe("Failed to accept connection")
                }
            }
        }

        Executors.newSingleThreadExecutor().submit {
            println("Type 'stop' to stop the server")
            val scanner = Scanner(System.`in`)
            while (true) {
                val command = scanner.nextLine()
                if (command == "stop") {
                    stop()
                    break
                }
            }
        }
    }
    override val serverName: String
        get() = config.getString("server.name")
    override val serverIP: String
        get() = config.getString("server.ip")
    override val serverPort: Int
        get() = config.getInt("server.port")
    override val serverDescription: String
        get() = config.getString("server.description")
    override val serverStatus: ServerStatus
        get() = status
    override val serverVersion: String
        get() = "1.0.0"
    override val serverMaxPlayers: Int
        get() = config.getInt("server.max_players")

    override fun start() {
        logger.info("Server started listening on $serverIP port $serverPort")
        status = ServerStatus.ONLINE
    }


    override fun getOnlinePlayers(): List<Player> {
        return PlayerManager.getPlayers()
    }

    override fun getOfflinePlayers(): List<Player> {
        return PlayerManager.getPlayers()
    }

    override fun broadcast(message: String) {
        getOnlinePlayers().forEach { it.sendMessage(message) }
    }

    override fun getOnlinePlayerCount(): Int {
        return getOnlinePlayers().size
    }

    override fun getOfflinePlayerCount(): Int {
        return getOfflinePlayers().size
    }

    override fun getPlayerCount(): Int {
        return getOnlinePlayerCount() + getOfflinePlayerCount()
    }

    override fun getPlayer(name: String): Player? {
        return PlayerManager.getPlayer(name)
    }

    override fun stop() {
        logger.info("Server stopped")
        exitProcess(0)
    }
}

fun getCurrentWorkingDirectory(): String {
    return System.getProperty("user.dir")
}