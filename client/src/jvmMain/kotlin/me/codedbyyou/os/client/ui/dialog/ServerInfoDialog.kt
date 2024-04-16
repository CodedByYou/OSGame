package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
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
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import java.awt.Container
import javax.naming.ldap.Control

fun Node.serverInfoDialog(callback: ServerInfoDialog.() -> Unit = {}) = node (ServerInfoDialog(), callback)


class ServerInfoDialog() : PaddedContainer() {
    private val serverNameLabel: Label
    private val playersContainer: ScrollContainer
    val toggleList = signal()
    init {
        anchor(layout = AnchorLayout.CENTER_TOP)
        padding(10)
        column {
            separation = 10
            serverNameLabel = label {
                text = Client.connectionManager.connectedToIP ?: "No Server"
                horizontalAlign = HAlign.CENTER
            }
            val connectedPlayers = listOf("Player 1", "Player 2", "Player 3")
            playersContainer = scrollContainer {
                separation = 20
                minWidth = 200f
                minHeight = 200f
                column {
                    separation = 10
                    for (i in 0 until 10)
                    connectedPlayers.forEach { player ->
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

        visible = false
        toggleList += {

            if (!visible) {
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

    }


}

fun Node.chatBox(callback: ChatBox.() -> Unit = {}) = node(ChatBox(), callback)

class ChatBox() : PaddedContainer() {
    private val chatBox: ScrollContainer
    private val chatInput: LineEdit
    private var chatBoxContent: VBoxContainer
    private var chatSend: Button? = null
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
                    for (i in 0 until 10)
                        column {
                            label {
                                text = "Player 1: Hello"
                                horizontalAlign = HAlign.LEFT
                                color = Color.GREEN
                            }
                        }
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
                        chatBox.visible = false
                    }
                }
                chatSend = button {
                    text = "Send"
                    minWidth = 50f
                    onPressed += {
                        println("Sending message: ${chatInput.text}")
                        chatBoxContent.addChildAt(column {
                            label {
                                text = "${Client.user?.nickTicket ?: "No User"}: ${chatInput.text}"
                                horizontalAlign = HAlign.LEFT
                                color = Color.LIME
                            }
                        },
                            chatBoxContent.children.size )
                        chatBox.position = Vec2f(0f,
                            chatBoxContent.children.size.toFloat() * chatBoxContent.children[0].viewport().virtualHeight)
                    }
                }
            }
        }
    }
}


fun Node.roomInfoDialog(context: Context, callback: RoomInfoDialog.() -> Unit = {}) = node(RoomInfoDialog(context), callback)

class RoomInfoDialog(val our: Context) : PaddedContainer(){
    private val roomNameLabel: Label
    private val roomInfoContainer: ScrollContainer
    private val roomList: VBoxContainer
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

                roomList = column {
                    separation = 20

                    column {
                        separation = 10
                        for (i in 0..<10)
                            column {
                                label {
                                    text = "Room #${i+1}: 1/4"
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.GREEN
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
                for (i in 0 until 10)
                    column {
                        label {
                            text = "Room 1: 1/4"
                            horizontalAlign = HAlign.CENTER
                            color = Color.GREEN
                        }
                    }
            }
        }

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