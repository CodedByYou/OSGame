package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KT
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.resource.InputEvent
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.Fonts
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.util.signal
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import me.codedbyyou.os.client.game.manager.ConnectionManager
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.game.scenes.GameScene
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton
import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.sendPacket
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.core.models.GameRoomInfo
import me.codedbyyou.os.core.models.deserialized
import java.awt.Container
import javax.naming.ldap.Control
import kotlin.reflect.KClass

fun Node.serverInfoDialog(callback: ServerInfoDialog.() -> Unit = {}) = node (ServerInfoDialog(), callback)


class ServerInfoDialog() : PaddedContainer() {
    private val serverNameLabel: Label
    private val playersContainer: ScrollContainer
    val toggleList = signal()
    val updatePlayers = signal()
    init {

        anchor(layout = AnchorLayout.CENTER_TOP)
        padding(10)
        column {
            separation = 10
            serverNameLabel = label {
                text = Client.connectionManager.connectedToIP ?: "No Server"
                horizontalAlign = HAlign.CENTER
            }
            playersContainer = scrollContainer {
                separation = 20
                minWidth = 200f
                minHeight = 200f
                column {
                    separation = 10
                }
            }
        }

        visible = false
        toggleList += {
            if (!visible) {
                updatePlayers.emit()
                visible = !visible
                serverNameLabel.text = Client.connectionManager.connectedToIP ?: "No Server"
                if (serverNameLabel.hasFocus) {
                    serverNameLabel.focusMode = FocusMode.NONE
                    playersContainer.focusMode = FocusMode.ALL
                } else {
                    playersContainer.focusMode = FocusMode.NONE
                    serverNameLabel.focusMode = FocusMode.ALL
                }
            } else {
                visible = false
            }
        }
        updatePlayers += {
            Client.connectionManager.sendPacket(
                Packet(
                    PacketType.SERVER_PLAYER_LIST
                )
            )
            KtScope.launch {
                withContext(newSingleThreadAsyncContext()) {
                    val packet = Client.connectionManager.tabChannel.receive()
                    if (packet.packetType == PacketType.SERVER_PLAYER_LIST) {
                        val playerListData =
                            packet.packetData["players"].toString().split("+")
                        playersContainer!!.apply {
                            destroyAllChildren()
                            column {
                                separation = 10
                                playerListData.forEach { player ->
                                    column {
                                        label {
                                            text = player
                                            horizontalAlign = HAlign.CENTER
                                            color = Color.GREEN
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}

fun Node.chatBox(callback: ChatBox.() -> Unit = {}) = node(ChatBox(), callback)

class ChatBox() : PaddedContainer() {
    private val chatBox: ScrollContainer
    private val chatInput: LineEdit
    private var chatBoxContent: VBoxContainer
    private var chatSend: Button? = null
    private var job: Job? = null
    init {
        anchor(layout = AnchorLayout.BOTTOM_LEFT)
        padding(10)
        column {
            separation = 10
            chatBox = scrollContainer {
                visible = false
                separation = 20
                minWidth = 200f
                minHeight = 200f
                chatBoxContent = column {
                    separation = 10
                }
            }
            row {
                chatInput = lineEdit {
                    minWidth = 200f
                    text = ""
                    placeholderText = "Server Chat.. "
//                    onInput += {
//                        if (it.key == Key.ENTER) {
//                            if (chatSend != null)
//                                chatSend!!.onPressed.emit()
//                        }
//                    }
                    onFocus+={
                        chatBox.visible = true
                    }

                    onFocusLost+={
                        chatBox.visible = true
                    }
                }
                chatSend = button {
                    text = "Send"
                    minWidth = 50f
                    onPressed += {
                        chatBoxContent.apply{
                            column {
                                label {
                                    text = "${Client.user?.nickTicket ?: "No User"}: ${chatInput.text}"
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.LIME
                                }
                            }
                        }
                        chatBox.position = Vec2f(0f,
                            chatBoxContent.children.size.toFloat() * chatBoxContent.children[0].viewport().virtualHeight)
                        if (chatInput.text.startsWith("/")){
                            Client.connectionManager.sendPacket(
                                Packet(
                                    PacketType.MESSAGE,
                                    mapOf("message" to chatInput.text)
                                )
                            )
                        } else {
                            Client.connectionManager.sendPacket(
                                Packet(
                                    PacketType.MESSAGE,
                                    mapOf("message" to chatInput.text)
                                )
                            )
                            // what might go wrong here?
                        }
                    }
                }
            }
        }
        job  = KtScope.launch {
            withContext(newSingleThreadContext("ChatBoxThread")){
                while (true){
                    delay(500)
                    val packet = Client.connectionManager.chatChannel.receive()
                    if (packet.packetType == PacketType.MESSAGE) {
                        val message = packet.packetData["message"].toString()
                        chatBoxContent.apply{
                            column {
                                label {
                                    text = message
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.GREEN
                                }
                            }
                        }
                    }
                }
            }
        }
        job!!.start()
    }
}


fun Node.roomInfoDialog(onSelection: suspend (KClass<out Scene>) -> Unit,  context: Context, callback: RoomInfoDialog.() -> Unit = {}) = node(RoomInfoDialog(onSelection, context), callback)

class RoomInfoDialog(
    private val onSelection: suspend (KClass<out Scene>) -> Unit,
    val our: Context,
) : PaddedContainer(){
    private val roomNameLabel: Label
    private val roomInfoContainer: ScrollContainer
    private var roomList: VBoxContainer
    val updateRooms = signal()
    private val renderRooms = signal()
    private val thread = newSingleThreadAsyncContext()
    private val rooms: MutableList<GameRoomInfo> = mutableListOf()
    private val isRoomInfoDialogVisible = false

    init {
        anchor(layout = AnchorLayout.CENTER_RIGHT)
        padding(10)
        paddingRight = (our.graphics.width * 1/20f).toInt()
        panelContainer {
            minWidth = our.graphics.width * 3/10f
            minHeight = our.graphics.height * 0.9f

            column {
                separation = 10
                roomNameLabel = label {
                    text = "Room Name"
                    fontScale = Vec2f(1.5f, 1.5f)
                    horizontalAlign = HAlign.LEFT
                }

                column {
                    separation = 20

                    roomList =column {
                        separation = 10
                        column {
                            label {
                                text = "No Rooms Available.."
                                horizontalAlign = HAlign.LEFT
                                color = Color.RED
                            }
                        }
                    }
                }
            }
        }

        roomInfoContainer = scrollContainer {
            visible = false
            minWidth = 200f
            minHeight = 200f
            column {
                separation = 10
            }
        }
        updateRooms += {
            Client.connectionManager.sendPacket(
                Packet(
                    PacketType.GAMES_LIST
                )
            )
            KtScope.launch {
                withContext(newSingleThreadAsyncContext()) {
                    val packet = Client.connectionManager.gameChannel.receive()
                    if (packet.packetType == PacketType.GAMES_LIST) {
                        val roomListData =
                            packet.packetData["games"].toString().deserialized()
                        rooms.clear()
                        rooms += roomListData
                        renderRooms.emit()
                    }
                }
            }
        }
        renderRooms += {
            KtScope.launch {
                withContext(thread) {
                    roomList.children.forEach {
                        roomList.removeChild(it)
                    }
                    roomList.destroyAllChildren()
                    val colWidth = our.graphics.width * 3/10f
                    roomList.apply {
                        column {
                            row {
                                label {
                                    text = "Room Name"
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.GREEN
                                    minWidth = colWidth * 0.6f
                                }
                                label {
                                    text = "Players"
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.GREEN
                                    minWidth = colWidth * 0.2f
                                }
                                label {
                                    text = "Status"
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.GREEN
                                    minWidth = colWidth * 0.2f
                                }
                                label {
                                    text = "Join"
                                    horizontalAlign = HAlign.RIGHT
                                    color = Color.GREEN
                                    minWidth = colWidth * 0.1f
                                }
                            }
                        }
                        rooms.forEach { room ->
                            column {
                                row {
                                    label {
                                        text = "${room.roomName}"
                                        horizontalAlign = HAlign.LEFT
                                        color = Color.GREEN
                                        minWidth = colWidth*0.6f
                                    }
                                    label{
                                        text = "${room.roomPlayerCount}/${room.roomMaxPlayers}"
                                        horizontalAlign = HAlign.LEFT
                                        color = Color.GREEN
                                        minWidth = colWidth*0.2f
                                    }
                                    label{
                                        text = "${room.roomStatus}"
                                        horizontalAlign = HAlign.LEFT
                                        color = when(room.roomStatus){
                                            RoomStatus.STARTING -> Color.GREEN
                                            RoomStatus.STARTED -> Color.BLUE
                                            RoomStatus.ENDED -> Color.RED
                                            RoomStatus.NOT_STARTED -> Color.LIME
                                        }
                                        minWidth = colWidth*0.2f
                                    }
                                    soundButton {
                                        text = "Join"
                                        disabled = room.roomStatus != RoomStatus.NOT_STARTED
                                        onPressed += {

                                            KtScope.launch {
                                                println("Joining room ${room.roomNumber}")
                                                Client.connectionManager.sendPacket(
                                                    PacketType.GAME_JOIN.toPacket(
                                                        mapOf("room" to room.roomNumber)
                                                    )
                                                )
                                                withContext(newSingleThreadAsyncContext()) {
                                                    println("Waiting for response..")
                                                    val packet = Client.connectionManager.gameChannel.receive()
                                                    println("Received response..")
                                                    println(packet.packetType.name)
                                                    if (packet.packetType == PacketType.GAME_JOIN) {
                                                        println("Joining room ${room.roomNumber}")
                                                        KtScope.launch {
                                                            onSelection.invoke(GameScene::class)
                                                        }
                                                    } else if (packet.packetType == PacketType.ROOM_FULL){
                                                        println("Room is full") // better handling
                                                        Client.connectionManager.sendPacket(
                                                            PacketType.MESSAGE.toPacket(
                                                                mapOf("message" to "Room is full")
                                                            )
                                                        )
                                                    } else if (packet.packetType == PacketType.NO_SUCH_ROOM){
                                                        println("No such room")
                                                        Client.connectionManager.sendPacket(
                                                            PacketType.MESSAGE.toPacket(
                                                                mapOf("message" to "No such room")
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        KtScope.launch {
            withContext(newSingleThreadAsyncContext()){
                while (true){
                    if (isRoomInfoDialogVisible) {
                        if (Client.connectionManager.isConnected())
                            updateRooms.emit()
                    }
                    delay(5000)
                }
            }
        }
    }

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}


// mute and unmute box

fun Node.muteBox(callback: MuteBox.() -> Unit = {}) = node(MuteBox(), callback)


class MuteBox() : PaddedContainer() {
    private val muteButton: Button
    private var unmuteButton: Button? = null
    private var previousVolume = 0f
    init {
        anchor(layout = AnchorLayout.BOTTOM_RIGHT)
        padding(10)
        row {
            muteButton = button {
                visible = false
                text = "Mute"
                minWidth = 50f
                onPressed += {
                    previousVolume = Config.musicMultiplier
                    Config.musicMultiplier = 0f
                    this.visible = false
                    unmuteButton?.visible = true
                }
            }
            unmuteButton = button {
                visible = false
                text = "Unmute"
                minWidth = 50f
                onPressed += {
                    Config.musicMultiplier = previousVolume
                    this.visible = false
                    muteButton.visible = true
                }
            }
            if (Config.musicMultiplier == 0f) {
                unmuteButton!!.visible = true
            } else {
                muteButton.visible = true
            }
        }
    }
}