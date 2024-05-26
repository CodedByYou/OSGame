package me.codedbyyou.os.game.ui.manager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import com.software.project.ui.controller.NavigationController
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType.*
import me.codedbyyou.os.core.interfaces.server.sendPacket
import me.codedbyyou.os.core.models.Title
import me.codedbyyou.os.core.models.deserialized
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.*
import me.codedbyyou.os.game.resource.AudioPlayer
import me.codedbyyou.os.game.resource.Config
import java.io.OutputStream
import java.net.Socket
import java.util.logging.Logger

class ConnectionManager {
    private val logger = Logger.getLogger("ConnectionManager")
    private var server : Server? = null
    private var connection: Socket? = null
    private var connectionJob: Job? = null
    private var output: OutputStream? = null

    val connectedToIP : String?
        get() = server?.ip

    val channel: Channel<Packet> = Channel()

    /**
     * channel will be used for async communication between the UI player list and the server
     * this is such that the UI can be updated in real-time when a player joins or leaves the server
     * or when a player is kicked or banned
     * this will make sure other channels are not blocked by the UI traffic
     */
    val tabChannel: Channel<Packet> by lazy { Channel() }

    /**
     * channel will be used to handle game-related packets, such as game start, game end, round start, round end, etc.
     * this will be used to update the UI in real-time when a game starts, ends, or when a player wins or loses
     * this will make sure other channels are not blocked by the game traffic
     */
    val gameChannel : Channel<Packet>  = Channel()

    /**
     * GameScene Channel
     */
    val gameSceneChannel : Channel<Packet> = Channel()

    /**
     * Leaderboard Channel
     */
    val leaderboard : Channel<Packet> = Channel()

    /**
     * This function will be used to connect to the server
     * @param _server the server to connect to
     * @return ConnectionStatus.CONNECTED if the connection was successful
     * @return ConnectionStatus.ALREADY_CONNECTED if the client is already connected to the server
     * @return ConnectionStatus.DISCONNECTED if the client is disconnected from the server
     */
    fun connectTo(_server: Server) : ConnectionStatus{
        if (server != null ) {
            logger.info("Disconnecting from server ${server!!.ip}")
            disconnect()
        }
        if (server == _server && connection != null && connection!!.isConnected) {
            logger.info("Already connected to server ${server!!.ip}")
            return ConnectionStatus.ALREADY_CONNECTED
        }
        server = _server
        logger.info("Connecting to server ${_server.ip}")
        connectionJob = GlobalScope.launch {
            connection = Socket(_server.ip, _server.port)
            handleConnection(connection!!)
        }
        logger.info("Connected to server ${_server.ip}")

        return ConnectionStatus.CONNECTED
    }

    /**
     * disconnects the client from the server
     */
    fun disconnect() {
        connectionJob?.cancel()
        connection?.let {
            if (!it.isClosed)
                it.close()
        }
        if (server != null)
            logger.info("Disconnected from server ${server!!.ip}")
        server = null
        NavigationController.navigateTo("mainMenu")
    }

    /**
     * This function will be used to handle the connection to the server
     * it is used inside a coroutine to handle the connection in a separate thread
     * this is to make sure the UI is not blocked by the connection and to keep the connection alive
     * @param socket the socket to connect to
     */
    private fun handleConnection(socket: Socket) {
        val input = socket.getInputStream()
        output = socket.getOutputStream()
        val buffer = ByteArray(4096)
        var read : Int
        try {
            while (input.read(buffer).also { read = it } != -1) {
                var data = String(buffer, 0, read)
                val packet = Packet.fromPacket(data)
                val packetType = packet.packetType
                val packetData = packet.packetData

                logger.info("Received packet type: $packetType")
                logger.info("Received ${packetData.size} packet data")

                CoroutineScope(Dispatchers.IO).launch {
                    when (packetType) {
                        INFO_PING -> {
                            if (Client.user != null && !Client.user!!.isAFK()) {
                                INFO_PONG.sendPacket(output!!)
                            }
                        }
                        INFO_PONG -> {
                            logger.info("Server information received")
                        }
                        MESSAGE -> {
                            logger.info("Message received: ${packetData["message"]}")
                            delay(250)
                            ChatViewModel.addMessage(packetData["message"].toString())
                        }
                        TITLE -> {
                            logger.info("Received title")
                            var duartion = try {
                                packetData["duration"].toString().toFloat()
                            } catch (e: Exception) {
                                1f
                            }
                            val title = Title(
                                packetData["title"].toString(),
                                packetData["subtitle"].toString(),
                                duration = duartion
                            )
                            withContext(Dispatchers.Default) {
                                TitleManager.addTitle(title)
                                logger.info("Added title: ${title.text}")
                            }
                            if (title.subtitle.contains("Better luck")) {
                                AudioPlayer.playOneShotSound("lose")
                            }
                        }
                        KICK -> {
                            logger.info("Received kick gift packet")
                            TitleManager.addTitle(
                                Title(
                                    "You have been kicked from the server",
                                    packetData["reason"].toString(),
                                    5f
                                )
                            )
                            delay(500)
                            NavigationController.navigateTo("mainMenu")
                        }
                        GAME_START -> {
                            logger.info("Received game start")
                            GameViewModel.resetGame()
                            GameViewModel.gameStatus.value = "Game Started"
                            AudioPlayer.playOneShotSound("ring")
                        }
                        GAME_ROUND_START -> {
//                            Client.gameState = GameState.PLAYING
                            logger.info("Received game round start")
                            val data = packet.packetData["data"].toString().split(":")
                            println(
                                data
                            )
                            val round = data[0]
                            val chances = data[1]
                            val twoThirds = data[2].toDouble()
                            if (twoThirds > -1)
                                GameViewModel.secondMessage.value = "Last Two Thirds: $twoThirds"
                            GameViewModel.twoThirds.value = twoThirds
                            GameViewModel.gameStatus.value = "Round $round"
                            GameViewModel.chancesLeft.value = chances.toInt()
                            GameViewModel.canGuess.value = true

                        }
                        GAME_ROUND_END -> {
                            logger.info("Received game round end")
                            GameViewModel.gameStatus.value = "Round Ended"
                        }
                        GAME_END -> {
                            logger.info("Received game end")
                            val leaderboardData = packetData["leaderboard"].toString().split(",")
                            val leaderboard = mutableListOf<Pair<String,String>>()
                            leaderboardData.forEach {
                                val split = it.split(":")
                                leaderboard.add(Pair(split[0].trim(), split[1].trim()))
                            }
                            DialogViewModel.leaderboard.value = leaderboard
                            DialogViewModel.showDialog.value = true
                            GameViewModel.resetGame()
                            GameViewModel.gameStatus.value = "Game Ended"
                            GlobalScope.launch {
                                var count = 3
                                while (count > 0) {
                                    TitleManager.addTitle(Title("Game Ended","Returning to Lobby in $count seconds", 1f))
                                    delay(1050)
                                    count--
                                }
                                delay(
                                    4150
                                )

                                NavigationController.navigateTo("game")
                            }
                        }
                        ROOM_INFO -> {
                            logger.info("Received room info")
                            gameChannel.send(packet)
                        }
                        PLAYER_ROOM_JOIN -> {
                            println("Received player room join packet")
//                                Client.gameState = GameState.PLAYING
                        }
                        GAME_PLAYER_LEAVE -> {
                            logger.info("Received player leave packet")
                            gameChannel.send(packet)
                        }
                        GAME_PLAYER_INFO -> TODO()
                        GAME_PLAYER_READY -> TODO()
                        GAME_PLAYER_GUESS -> TODO()
                        GAME_PLAYER_WIN -> {
                            logger.info("Received player win packet")
                            AudioPlayer.playOneShotSound("win")
                        }
                        GAME_PLAYER_LOSE -> {
                            logger.info("Received player lose packet")
                            AudioPlayer.playOneShotSound("lose")
                        }
                        GAMES_LIST -> {
                            logger.info("Received games list")
                            val roomListData =
                                packet.packetData["games"].toString().deserialized()
                            LobbyViewModel.gameRooms.clear()
                            LobbyViewModel.gameRooms.addAll(roomListData)
                        }
                        GAME_JOIN -> {
                            logger.info("Received player game join packet")
                            NavigationController.goTo(
                                "inGame",
                            )
                            // fix ui of inGame, remove guess show room id and everything else etc
                            GameViewModel.gameStatus.value = "Starting Soon"
                            GameViewModel.secondMessage.value = "Ask your friends to join the game ${Client.roomID}"
                            GameViewModel.canGuess.value = false
                        }

                        LEADERBOARD -> {
                            val data = packet.packetData["leaderboard"].toString().split(",")
                            val leaderboardData = mutableListOf<Pair<String, String>>()
                            data.forEach {
                                val split = it.split(":")
                                leaderboardData.add(Pair(split[0], split[1]))
                            }
                            LobbyViewModel.leaderboard.clear()
                            LobbyViewModel.leaderboard.addAll(leaderboardData)
                        }

                        SERVER_AUTH_SUCCESS -> {
                            logger.info("Authenticated with server ${server?.ip}")
                            NavigationController.navigateTo("game")
                        }

                        SERVER_AUTH_FAIL -> {
                            logger.warning("Failed to authenticate with server ${server?.ip}")
                            NavigationController.navigateTo("registerToServer")
                        }

                        SERVER_REGISTER_SUCCESS -> {
                            logger.info("Registered with server ${server?.ip}")
                            val ticket = packetData["ticket"] as String
                            Client.user = User(Client.user!!.psuedoName, ticket)
                            val server: Server = ServersViewModel.servers.find {
                                it.ip  == Client.connectionManager.connectedToIP
                            }!!
                            println(
                                "Before upserting server"
                            )

                            ServersViewModel.servers
                            .forEachIndexed { index, it -> println("- $index $it") }
                            Config.upsertServer(
                                Server(
                                    server.name,
                                    server.ip,
                                    server.port,
                                    Client.user!!.psuedoName,
                                    ticket,
                                    server.description,
                                    server.status,
                                    server.maxPlayers,
                                    server.onlinePlayers
                                )
                            )
                            println(
                                "After upserting server"
                            )
                            ServersViewModel.servers
                                .forEachIndexed { index, it -> println("- $index $it") }

                            NavigationController.navigateTo("game")
                        }

                        SERVER_REGISTER_FAIL -> {
                            logger.warning("Failed to register with server ${server?.ip}")
                            AppSessionData.registerErrorMessage.value = "Failed to register with server, try another name, tickets are limited for this name"
                            Client.user = null
                        }

                        SERVER_PLAYER_LIST -> {
                            logger.info("Received player list")
                            GameViewModel.gamePlayerList.clear()
                            GameViewModel.gamePlayerList.addAll(packetData["players"].toString().split("+"))
                        }

                        NO_SUCH_ROOM -> {
                            logger.warning("No such room")
                            TitleManager.addTitle(Title("No such room", "Please try again later", 2f))
                        }

                        NO_SUCH_PLAYER -> TODO()
                        ROOM_FULL -> {
                            logger.warning("Room is full")
                            TitleManager.addTitle(Title("Room is full", "Please try again later", 5f))
                        }

                        ROOM_ALREADY_STARTED -> {
                            logger.warning("Room already started")
                            TitleManager.addTitle(Title("Room already started", "Please try again later", 5f))
                        }

                        NO_SUCH_PACKET -> TODO()
                        GAME_ROUND_INFO -> {
                            logger.info("Received game round info")
                            println(
                                packetData
                            )
                            println(
                                "HANDLE MEEEEE YES I AM USED!? GAME ROUND INFO PACKET"
                            )
                            gameSceneChannel.send(packet)
                        }

                        else -> {
                            logger.warning("Unhandled packet type: $packetType")
                        }
                    }
                }

            }
        } catch (e: Exception) {
            logger.info("Disconnected")
            disconnect()

        }
    }

    /**
     * This function will be used to send a packet to the server
     * @param packet the packet to send
     */
    fun sendPacket(packet: Packet) {
        packet.sendPacket(output!!)
    }

    /**
     * This function will be used to check if the client is connected to the server
     * @return true if the client is connected to the server
     * @return false if the client is not connected to the server
     */
    fun isConnected(): Boolean {
        return connection != null && connection!!.isConnected
    }

    /**
     * Enum class to represent the connection status
     * CONNECTED if the client is connected to the server
     * ALREADY_CONNECTED if the client is already connected to the server
     * DISCONNECTED if the client is disconnected from the server
     */
    enum class ConnectionStatus {
        CONNECTED,
        ALREADY_CONNECTED,
        DISCONNECTED
    }
}