package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.async.KT
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.async.newSingleThreadAsyncContext
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.util.Signal
import com.lehaine.littlekt.util.signal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.codedbyyou.os.client.game.Game
import me.codedbyyou.os.client.ping
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton

fun Node.serverDialog(width : Float, height : Float, callback: ServerDialog.() -> Unit = {}, ) = node(ServerDialog(width, height), callback)

class ServerDialog(dialogWidth: Float, dialogHeight: Float) : CenterContainer() {
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
            println("Connecting to server: $chosenServer")
        }
        onRefresh += {
            println("Refreshing server list")
            serverListColumn?.destroyAllChildren()
            for (server in Config.servers){
                KtScope.launch {
                    withContext(executor){
                        server.ping()
                        serverListColumn?.renderServer(server, onEditServer, onServerConnect) {
                            chosenServer = it
                         }
                    }
                }
            }
        }
        onAddServer += {
            println("Adding server")
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
                  var onlinePlayers: Int?=null)


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
                    // Emit the server data when the "Connect" button is clicked
                    onServerConnect.emit()
                    onChooseServer(server)
                }
            }

        }
    }
}