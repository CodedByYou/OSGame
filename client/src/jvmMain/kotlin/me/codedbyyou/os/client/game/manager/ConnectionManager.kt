package me.codedbyyou.os.client.game.manager

import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.ui.dialog.Server
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType.*
import me.codedbyyou.os.core.interfaces.server.sendPacket
import java.io.OutputStream
import java.net.Socket
import java.util.logging.Logger
import kotlin.reflect.KClass

class ConnectionManager {
    private val logger = Logger.getLogger("ConnectionManager")
    private var server : Server? = null
    private var connection: Socket? = null
    private var connectionJob: Job? = null
    private var output: OutputStream? = null
    private var lastActive = System.currentTimeMillis()

    // i want to change scene
    companion object {
        var serverScreenCallBack : (suspend () -> Unit)? = null
            set(value){
                if (field != null){
                    return
                }
                field = value
            }
    }
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
     * channel will be used to handle chat-related packets, such as chat messages, private messages, public messages, etc.
     * this will be used to update the Chat Box in real-time when a message is sent or received
     * this will make sure other channels are not blocked by the chat traffic.
     */
    val chatChannel : Channel<Packet> = Channel()

    /**
     * Idea:
     * serverInfo , i think for that it, it can use the tabChannel, and for that
     * it will be renamed to serverInfoChannel
     */

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
        serverScreenCallBack?.let {
            GlobalScope.launch {
                withContext(newSingleThreadAsyncContext()){
                    delay(1000)
                    it()
                }
            }
        }
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
        val channelExecutor = newSingleThreadAsyncContext()
        val gameChannelExecutor = newSingleThreadAsyncContext()
        val buffer = ByteArray(1024)
        var read : Int
        try {
            while (input.read(buffer).also { read = it } != -1) {
                var data = String(buffer, 0, read)
                val packet = Packet.fromPacket(data)
                val packetType = packet.packetType
                val packetData = packet.packetData
                KtScope.launch {
                    withContext(channelExecutor){
                        logger.info("Received packet type: $packetType")
                        logger.info("Received ${packetData.size} packet data")
                    }
                }

                when (packetType) {
                    INFO_PING -> {
                        if (Client.user != null && !Client.user!!.isAFK()){
                            INFO_PONG.sendPacket(output!!)
                        }
                    }
                    INFO_PONG -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Server information received")
                            }
                        }
                    }
                    MESSAGE -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Message received: ${packetData["message"]}")
                                chatChannel.send(packet)
                            }
                        }
                    }

                    ACTION_BAR -> TODO("Implement Action Bar Messages in the Game")
                    TITLE -> TODO("Implement Title Messages in the Game")
                    KICK -> TODO("Implement Kick UI in the Client")
                    CLIENT_INFO -> TODO()
                    PLAYER_LEAVE -> TODO("Implement Player Leave in the Game")
                    GAME_START -> TODO()
                    GAME_ROUND_START -> TODO()
                    GAME_ROUND_END -> TODO()
                    GAME_END -> TODO()
                    ROOM_INFO -> TODO()
                    PLAYER_ROOM_JOIN -> TODO()
                    GAME_PLAYER_LEAVE -> TODO()
                    GAME_PLAYER_INFO -> TODO()
                    GAME_PLAYER_READY -> TODO()
                    GAME_PLAYER_GUESS -> TODO()
                    GAME_PLAYER_WIN -> TODO()
                    GAME_PLAYER_LOSE -> TODO()
                    GAMES_LIST -> {
                        logger.info("Recieved games list")
                        KtScope.launch {
                            withContext(gameChannelExecutor){
                                logger.info("Received games list")
                                gameChannel.send(packet)
                            }
                        }
                    }
                    GAME_CREATE -> TODO()
                    GAME_JOIN -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Received player game join packet")
                                gameChannel.send(packet)
                            }
                        }
                    }
                    GAME_LEAVE -> TODO()
                    GAME_CHAT -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Received game chat")
                                gameChannel.send(packet)
                            }
                        }
                    }
                    GAME_CHAT_PRIVATE -> TODO()
                    GAME_CHAT_PUBLIC -> TODO()
                    SERVER_CHAT -> TODO()
                    SERVER_CHAT_PRIVATE -> TODO()
                    SERVER_CHAT_PUBLIC -> TODO()
                    SERVER_AUTH -> {}
                    SERVER_AUTH_SUCCESS -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.info("Authenticated with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_AUTH_FAIL -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.warning("Failed to authenticate with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_REGISTER -> TODO()
                    SERVER_REGISTER_SUCCESS -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.info("Registered with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_REGISTER_FAIL -> {
                        KtScope.launch {
                            withContext(channelExecutor) {
                                logger.warning("Failed to register with server ${server?.ip}")
                                channel.send(packet)
                            }
                        }
                    }
                    SERVER_PLAYER_LIST -> {
                        KtScope.launch {
                            withContext(channelExecutor){
                                logger.info("Received player list")
                                tabChannel.send(packet)
                            }
                        }
                    }
                    NO_SUCH_ROOM -> TODO()
                    NO_SUCH_PLAYER -> TODO()
                    ROOM_FULL -> TODO()
                    ROOM_ALREADY_STARTED -> TODO()
                    NO_SUCH_PACKET -> TODO()
                    GAME_ROUND_INFO -> TODO()
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