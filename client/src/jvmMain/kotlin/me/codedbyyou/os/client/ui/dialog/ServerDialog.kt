package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.util.Signal
import com.lehaine.littlekt.util.signal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.codedbyyou.os.client.game.manager.TitleManager
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.game.runtime.client.User
import me.codedbyyou.os.client.game.scenes.ServerLobbyScene
import me.codedbyyou.os.client.game.scenes.ServerMenuJoinScene
import me.codedbyyou.os.client.ping
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.models.Title
import java.util.logging.Logger
import kotlin.reflect.KClass

fun Node.serverDialog(onSelection: suspend (KClass<out Scene>) -> Unit, width : Float, height : Float, callback: ServerDialog.() -> Unit = {}, )
= node(ServerDialog(onSelection, width, height), callback)

class ServerDialog( private val onSelection: suspend (KClass<out Scene>) -> Unit, dialogWidth: Float, dialogHeight: Float) : CenterContainer() {
    private val logger = Logger.getLogger("ServerDialog")
    val onBack = signal()
    val onRefresh = signal()
    val onAddServer = signal()
    val onRemoveServer = signal()
    val onEditServer = signal()
    val onServerConnect = signal()
    private var chosenServer: Server? = null
    private var serverListColumn: Node? = null
    init {
        val executor = newSingleThreadAsyncContext()
        anchor(layout = AnchorLayout.CENTER)
        onServerConnect.plusAssign {
            logger.info("Connecting to server: $chosenServer")
            KtScope.launch {
                val status = Client.connectionManager
                    .connectTo(chosenServer!!)
                logger.info("Status: $status")
                delay(50)
                if (chosenServer!!.psuedoName != null ) {
                    logger.info("Sending server auth packet")
                    Client.connectionManager.sendPacket(
                        Packet(
                            PacketType.SERVER_AUTH,
                            mapOf(
                                "nickTicket" to chosenServer!!.psuedoName + "#" + chosenServer!!.ticket,
                                "macAddress" to Client.macAddress
                            )
                        )
                    )
                    logger.info("Sent server auth packet")
                    val packet: Packet = Client.connectionManager.channel.receive()
                    logger.info("Received packet: $packet")
                    if (packet.packetType == PacketType.SERVER_AUTH_SUCCESS) {
                        Client.user =
                            User(
                                chosenServer!!.psuedoName!!,
                                chosenServer!!.ticket!!
                            )
                        onSelection(ServerLobbyScene::class)
                        return@launch
                    }

                }
                onSelection(ServerMenuJoinScene::class)
            }
        }

        onEditServer += {
            TitleManager.addTitle(Title("Soon to be implemented", "This feature is not yet implemented", 1f))
        }

        onRefresh += {
            serverListColumn?.destroyAllChildren()
            for (server in Config.servers){
                KtScope.launch {
                    withContext(newSingleThreadAsyncContext()){
                        server.ping()
                        serverListColumn?.renderServer(server, onEditServer, onServerConnect) {
                            chosenServer = it
                        }
                    }
                }
            }
        }
        onAddServer += {
            chosenServer = null
            this@ServerDialog.parent!!.upsertServerDialog(chosenServer) {
                this@ServerDialog.visible = false
                fun Node.closeDialog(){
                    this@ServerDialog.visible=true
                    this.destroy()
                }
                this.onAddServer += {
                    closeDialog()
                }
                this.onBack += {
                    closeDialog()
                }

            }
        }

        verticalSizeFlags = SizeFlag.FILL
        horizontalSizeFlags = SizeFlag.FILL
        verticalGrowDirection = GrowDirection.BOTH
        horizontalGrowDirection = GrowDirection.BOTH
        panelContainer {
            width = dialogWidth
            height = dialogHeight
            verticalSizeFlags = SizeFlag.EXPAND
            horizontalSizeFlags = SizeFlag.EXPAND
            verticalGrowDirection = GrowDirection.BOTH
            horizontalGrowDirection = GrowDirection.BOTH
            paddedContainer {
                padding(10)
                column {
                    horizontalSizeFlags = SizeFlag.FILL
                    verticalSizeFlags = SizeFlag.FILL
                    separation = 10
                    column {
                        stretchRatio = 1f
                        label {
                            text = "Server List"
                            horizontalAlign = HAlign.LEFT
                            paddingBottom = 10
                        }

                        row {
                            separation = 10
                            label {
                                minWidth = 175f
                                text = "Server Name"
                                color = Color.LIGHT_GREEN
                            }
                            label {
                                minWidth = 480f
                                text = "Description"
                            }
                            label { text = "Server Status" }
                            label { text = "Actions" }
                        }
                    }

                    scrollContainer {
                        stretchRatio = 8f
                        horizontalSizeFlags = SizeFlag.FILL
                        minHeight = 300f
                        minWidth = 800f
                        verticalSizeFlags = SizeFlag.FILL
                        serverListColumn = column {
                            separation = 10
                            if (Config.servers.isEmpty()) {
                                label {
                                    text = "No servers found"
                                    horizontalAlign = HAlign.CENTER
                                    color = Color.RED
                                }
                            } else {
                                onRefresh.emit()
                            }
                        }
                    }
                    row {
                        anchor(layout= AnchorLayout.BOTTOM_WIDE)
                        stretchRatio = 1f
                        minWidth = 800f
                        separation = 700

                        hBoxContainer {
                            anchor(AnchorLayout.BOTTOM_LEFT)
                            separation = 20
                            soundButton {
                                text = "+"
                                onPressed += {
                                    onAddServer.emit()
                                }
                            }
                        }

                        hBoxContainer {
                            anchor(AnchorLayout.BOTTOM_RIGHT)
                            separation = 20

                            soundButton {
                                text = "Refresh"
                                onPressed += {
                                    onRefresh.emit()
                                }
                                color = Color.DARK_GREEN
                            }

                            soundButton {
                                text = "Back"
                                onPressed += {
                                    onBack.emit()
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        onBack.clear()
        onRefresh.clear()
        onAddServer.clear()
        onRemoveServer.clear()
        onEditServer.clear()
        onServerConnect.clear()
    }
}

data class Server(var name: String? = null,
                  val ip: String,
                  val port: Int,
                  val psuedoName: String? = null,
                  val ticket: String?=null,
                  var description: String?=null,
                  var status: String?=null,
                  var maxPlayers: Int?=null,
                  var onlinePlayers: Int?=null){
    override fun equals(other: Any?): Boolean {
        return ip == (other as Server).ip && port == other.port
    }
}


fun Node.renderServer( server: Server, onEditServer: Signal, onServerConnect: Signal, onChooseServer : (server: Server) -> Unit) {
    if (server.name == null) {
        paddedContainer{
            padding(5)
            row {
                minWidth = 800f
                separation = 10
                // just put server ip and edit button
                label {
                    minWidth = 175f
                    text = server.ip
                }
                label {
                    minWidth = 480f
                    text = "Unknown Server"
                }
                label {
                    text = "OFFLINE"
                    fontColor = Color.RED
                }
                soundButton {
                    text = "Edit"
                    onPressed += {
                        onEditServer.emit()
                    }
                }
                soundButton {
                    text = "Connect"
                    disabled = true
                }
            }
        }
        return
    }
    val _width = 800f
    paddedContainer {
        padding(5)
        row {
            minWidth = _width
            separation = 10
            label {
                minWidth = 175f
                text = server.name!!
            }
            label {
                minWidth = 480f;
                text = server.description!!
            }
            label {
                text = server.status!!
                fontColor = if (server.status == "ONLINE") Color.GREEN else Color.YELLOW
            }

            soundButton {
                text = "Edit"
                onPressed += {
                    onEditServer.emit()
                }
            }
            soundButton {
                text = "Connect"
                fontColor = Color.CYAN
                onPressed += {
                    onChooseServer(server)
                    // Emit the server data when the "Connect" button is clicked
                    onServerConnect.emit()

                }
            }

        }
    }
}