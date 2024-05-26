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
import me.codedbyyou.os.server.player.GamePlayer
import me.codedbyyou.os.server.player.manager.PlayerManager
import me.codedbyyou.os.server.player.GamePlayerClientHandler
import me.codedbyyou.os.server.player.listeners.PlayerEventListener
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.util.Scanner
import java.util.concurrent.Executors
import java.util.logging.Logger
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
    private var heartBeat = false
    private val playerThreadExecutorPool = Executors.newFixedThreadPool(serverMaxPlayers)
    val lastPinged = mutableMapOf<String, Long>()
    val consoleCommandSender = ConsoleCommandSender()

    val gameManager = GameRoomManager()
    val eventsManager = EventManager()
    val logger = Logger.getLogger(Server::class.java.name)
    private val leaderboard = mutableListOf<Pair<String, Int>>()

    init {
        CommandManager
        initEvents()
        status = ServerStatus.STARTING

        if (isFirstRun) {
            logger.info("Config file created")
            logger.info("Please fill the config file with the server information and restart the server.")
            exitProcess(0)
        }

        leaderboard.addAll(config.getStringList("leaderboard").map {
            val split = it.split(":")
            split[0] to split[1].toInt()
        })

        PlayerManager
        loadPlayerProfiles()
        val connectedIPs = mutableListOf<String>()
        socketServer = ServerSocket(serverPort)
        // maybe todo: move to a coroutin?
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

        GlobalScope.launch {
            launch {
                while (true) {
                    @OptIn
                    if (heartBeat)
                        lastPinged.filter { System.currentTimeMillis() - it.value > heartBeatInterval }.forEach {
                            Executors.newSingleThreadExecutor().execute {
                            lastPinged.remove(it.key)
                            val player = PlayerManager.getPlayer(it.key) as GamePlayer
                            println("Kicking ${player!!.uniqueName} for being afk")
                            player!!.sendTitle("You have been kicked for being AFK","There can only be one afk",3f)
                            sleep(300)
                            player!!.kick("You have been kicked for being AFK")
                        }
                    }
                    delay(501)
                }
            }
        }

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
                CommandManager.executeCommand(consoleCommandSender, command, args)
            }
        }
        logger.info("Server initialized")
    }


    override fun toggleHeartBeat(): Boolean {
        heartBeat = !heartBeat
        return heartBeat
    }
    /**
     * Initialize the events listeners for the server
     */
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

    fun updateLeaderboard(map: List<Pair<String, Int>>) {
        println(map)
        map.forEach { (name, score) ->
            val player = leaderboard.find { it.first == name }
            if (player != null) {
                leaderboard.remove(player)
                leaderboard.add(name to score + player.second)
            } else {
                leaderboard.add(name to score)
            }
        }
        leaderboard.sortByDescending { it.second }
        GlobalScope.launch {
            config.set("leaderboard", leaderboard.map { "${it.first}:${it.second}" })
            config.save()
        }
    }

    /**
     * Get the leaderboard
     * @return A list of pairs of the player name and their score
     */
    fun getLeaderboard(): List<Pair<String, Int>> {
        return leaderboard
    }
    
    override val serverName: String
        get() = config.getString("server.name")
    override val serverIP: String
        get() = config.getString("server.ip")
    val heartBeatInterval: Int
        get() = config.getInt("server.heartbeat_interval", 6) * 1000
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
    override fun getOnlinePlayers(): List<Player> {return PlayerManager.getOnlinePlayers()}

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