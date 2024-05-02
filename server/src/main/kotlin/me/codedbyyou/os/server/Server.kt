package me.codedbyyou.os.server

import kotlinx.coroutines.*
import kotlinx.coroutines.internal.synchronized
import me.codedbyyou.os.core.interfaces.impl.OSGameServer
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.ServerStatus
import me.codedbyyou.os.server.command.CommandManager
import me.codedbyyou.os.server.command.interfaces.impl.ConsoleCommandSender
import me.codedbyyou.os.server.events.manager.EventManager
import me.codedbyyou.os.server.managers.GameRoomManager
import me.codedbyyou.os.server.player.manager.PlayerManager
import me.codedbyyou.os.server.player.GamePlayerClientHandler
import me.codedbyyou.os.server.player.listeners.PlayerEventListener
import java.net.ServerSocket
import java.util.Scanner
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess


/**
 * Main Core of the server
 * implements OSGameServer interface to provide the server with the necessary methods
 * to manage the server
 * @see OSGameServer
 * @author Abdollah Kandrani
 * @since 1.0.0
 * @version 1.0.0
 * @property status the status of the server
 * @property socketServer the server socket
 * @property playerThreadExecutorPool the thread pool for the players
 * @property gameManager the game manager
 * @property eventsManager the events manager
 */
@OptIn(InternalCoroutinesApi::class)
object Server : OSGameServer() {

    private var status: ServerStatus = ServerStatus.STARTING
    private var socketServer: ServerSocket? = null
    private val playerThreadExecutorPool = Executors.newFixedThreadPool(serverMaxPlayers)
    private val lastPinged = mutableMapOf<String, Long>()
    val consoleCommandSender = ConsoleCommandSender()

    val gameManager = GameRoomManager()
    val eventsManager = EventManager()
    val logger = Logger.getLogger(Server::class.java.name)


    init {
        CommandManager
        initEvents()
        status = ServerStatus.STARTING

        if (isFirstRun) {
            logger.info("Config file created")
            logger.info("Please fill the config file with the server information and restart the server.")
            exitProcess(0)
        }

        PlayerManager
        val connectedIPs = mutableListOf<String>()
        socketServer = ServerSocket(serverPort)
        // maybe todo: move to a coroutine
        Executors.newFixedThreadPool(1).submit {
            while (true) {
                try {
                    val socket = socketServer!!.accept()
                    logger.info("Client connected from ${socket.inetAddress.hostAddress}")
                    connectedIPs.add(socket.inetAddress.hostAddress)
                    playerThreadExecutorPool.submit(GamePlayerClientHandler(socket))
                } catch (e: Exception) {
                    connectedIPs.remove(socketServer!!.inetAddress.hostAddress)
                    logger.severe("Failed to accept connection")
                }
            }
        }

       // is working but needs completion, till then it is commented out
       // to not kick the players out
       // AFK SECTION
//        GlobalScope.launch {
//            launch {
//                while (true) {
//                    lastPinged.filter { System.currentTimeMillis() - it.value > 5000 }.forEach {
//                        PlayerManager.disconnect(PlayerManager.getPlayer(it.key)!!.uniqueName)
//                        lastPinged.remove(it.key)
//                    }
//                    delay(100)
//                }
//            }
//        }

        Executors.newSingleThreadExecutor().submit {
            println("Type 'stop' to stop the server")
            val scanner = Scanner(System.`in`)
            while (true){
                var args : List<String>
                synchronized(scanner) {
                    args = scanner.nextLine().split(" ")
                }
                val command = args[0]
                args = args.subList(1, args.size)
                if (command.equals("stop", true)) {
                    stop()
                }
//                if (command == "broadcast") {
//                    broadcast(args.joinToString(" "))
//                }
                CommandManager.executeCommand(consoleCommandSender, command, args)
            }
        }
        logger.info("Server initialized")
    }

    fun initEvents() {
        eventsManager.register(PlayerEventListener())
    }

     /**
      * Load the player profiles from the config file
      * @see PlayerManager
      **/
    fun loadPlayerProfiles() {
        val playersSection = config.getSection("players")
        playersSection.keys.forEach { key ->
            val macAddress = key.toString()
            val nickTicket = playersSection.getString(macAddress)
            val nickname = nickTicket.split("#")[0]
            val ticket = nickTicket.split("#")[1]
            PlayerManager.loadPlayer(nickname, ticket, macAddress)
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


    /**
     * Get all online players
     * @return A list of all online players
     * @see PlayerManager.getOnlinePlayers
     */
    override fun getOnlinePlayers(): List<Player> = PlayerManager.getOnlinePlayers()

    /**
     * Get all offline players
     * @return A list of all offline players
     * @see PlayerManager.getOfflinePlayers
     */
    override fun getOfflinePlayers(): List<Player> = PlayerManager.getOfflinePlayers()

    /**
     * Broadcast a message to all players, including the console
     * @param message The message to broadcast
     * @see PlayerManager
     * For more versatility, use the broadcastMessage method in PlayerManager
     * It allows you to exclude players from the broadcast
     * @see PlayerManager.broadcastMessage()
     */
    override fun broadcast(message: String) {
        PlayerManager.broadcastMessage(message, includeConsole = true)
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