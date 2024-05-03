package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.util.signal
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.game.runtime.client.User
import me.codedbyyou.os.client.game.scenes.ServerLobbyScene
import me.codedbyyou.os.client.game.scenes.ServerMenuJoinScene
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton
import me.codedbyyou.os.core.interfaces.server.Packet
import me.codedbyyou.os.core.interfaces.server.PacketType
import kotlin.reflect.KClass


fun Node.registerMenuDialog(
    onSelection: suspend (KClass<out Scene>) -> Unit,
    callback: RegisterMenuDialog.() -> Unit = {}) = node(RegisterMenuDialog(onSelection), callback)

class RegisterMenuDialog(private val onSelection: suspend (KClass<out Scene>) -> Unit) : CenterContainer() {

    val onRegisterFailure = signal()
    val onRegisterSuccess = signal()

    init {
        var hiddenNode: VBoxContainer? = null
        var usernameField: LineEdit? = null
        anchorRight = 0f
        anchorBottom = 0f
        anchor(layout = AnchorLayout.CENTER)
        theme = Assets.theme
        verticalSizeFlags = SizeFlag.FILL
        horizontalSizeFlags = SizeFlag.FILL
        verticalGrowDirection = GrowDirection.BOTH
        horizontalGrowDirection = GrowDirection.BOTH
        panelContainer() {
            minWidth = 400f
            paddedContainer {
                padding(10)
                column {
                    stretchRatio = 1f
                    horizontalSizeFlags = SizeFlag.FILL
                    verticalSizeFlags = SizeFlag.FILL
                    separation = 20
                    column {
                        horizontalSizeFlags = SizeFlag.FILL
                        verticalSizeFlags = SizeFlag.FILL
                        stretchRatio = 1f
                        separation = 20
                        label {
                            text = "Register"
                            horizontalAlign = HAlign.CENTER
                        }
                    }

                    column {
                        horizontalSizeFlags = SizeFlag.FILL
                        verticalSizeFlags = SizeFlag.FILL
                        stretchRatio = 1f
                        separation = 10
                        label {
                            text = "Username"
                            horizontalAlign = HAlign.LEFT
                        }
                        row {
                            separation = 15
                            usernameField = lineEdit {
                                minWidth = 375f
                                height = 50f
                                placeholderText = "Enter Username"
                            }
                        }
                    }
                    hiddenNode = column{
                        this.visible = false
                        horizontalSizeFlags = SizeFlag.FILL
                        verticalSizeFlags = SizeFlag.FILL
                        stretchRatio = 1f
                        separation = 10
                        label {
                            text = "Ticket"
                            horizontalAlign = HAlign.LEFT
                        }
                    }
                    column {
                        horizontalSizeFlags = SizeFlag.FILL
                        verticalSizeFlags = SizeFlag.FILL
                        soundButton {
                            text = "Register & Generate Ticket"
                            onPressed += {
                                val username = usernameField!!.text
                                val hiddenContainerLine = (hiddenNode!!.children[0] as Label)
                                if (username.isEmpty()) {
                                    hiddenNode!!.visible = true
                                    hiddenContainerLine.text = "Username cannot be empty"
                                    hiddenContainerLine.color = Color.RED
                                } else {
                                    Client.connectionManager.sendPacket(
                                        Packet(
                                            PacketType.SERVER_REGISTER,
                                            mapOf(
                                                "pseudoName" to usernameField!!.text,
                                                "machineId" to Client.macAddress
                                            )
                                        )
                                    )

                                    KtScope.launch {
                                        val packet = Client.connectionManager.channel.receive()
                                        val packetType = packet.packetType
                                        val packetData = packet.packetData
                                        when (packetType) {
                                            PacketType.SERVER_REGISTER_SUCCESS -> {
                                                hiddenNode!!.visible = true
                                                val ticket = packetData["ticket"] as String
                                                Client.user = User(text, ticket)
                                                hiddenContainerLine.color = Color.GREEN
                                                hiddenContainerLine.text = "Registered with ticket: $ticket"
                                                val server: Server = Config.servers.find {
                                                    it.ip  == Client.connectionManager.connectedToIP
                                                }!!
                                                Config.upsertServer(
                                                    Server(
                                                        server.name,
                                                        server.ip,
                                                        server.port,
                                                        usernameField!!.text,
                                                        ticket,
                                                        server.description,
                                                        server.status,
                                                        server.maxPlayers,
                                                        server.onlinePlayers
                                                    )
                                                )
                                                Client.user =
                                                    User(usernameField!!.text, ticket)
                                                onSelection.invoke(ServerLobbyScene::class)
                                            }

                                            PacketType.SERVER_REGISTER_FAIL -> {
                                                hiddenNode!!.visible = true
                                                hiddenContainerLine.text =
                                                    "Failed to register, please try with another pseudo name"
                                                hiddenContainerLine.color = Color.RED
                                                onSelection.invoke(ServerMenuJoinScene::class)
                                            }

                                            else -> {
                                                println("Unexpected packet type: $packetType")
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
