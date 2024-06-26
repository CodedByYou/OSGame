package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.util.signal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.game.enums.GameState
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.game.scenes.MenuScene
import me.codedbyyou.os.client.game.scenes.ServerLobbyScene
import me.codedbyyou.os.client.ui.soundButton
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import kotlin.reflect.KClass


fun Node.inGameMenuDialog(onSelection: suspend (KClass<out Scene>) -> Unit,callback: InGameMenuDialog.() -> Unit) = node(InGameMenuDialog(onSelection), callback)

class InGameMenuDialog(
    private val onSelection: suspend (KClass<out Scene>) -> Unit
) : CenterContainer() {

    val onResume = signal()
    val onSettings = signal()

    init {
        name = "InGameMenuDialog"
        onSettings.plusAssign {
            this.visible = false
            this.parent!!.settingsDialog {
                this.onBack.plusAssign {
                    this@InGameMenuDialog.visible=true
                    this@InGameMenuDialog.parent?.removeChild(this)
                }
            }
        }
        onResume += {
            this.parent?.removeChild(this)
        }
        anchor(layout = AnchorLayout.CENTER)

        panelContainer {
            paddedContainer {
                padding(10)
                column {
                    separation = 10
                    label {
                        text = "Server Menu"
                        horizontalAlign = HAlign.CENTER
                    }

                    soundButton {
                        minWidth = 200f
                        text = "Resume"
                        onReady += {
                            scene?.requestFocus(this)
                        }
                        onPressed += {
                            onResume.emit()
                        }
                    }

                    soundButton {
                        minWidth = 200f
                        text = "Settings"
                        onPressed += {
                            onSettings.emit()
                        }
                    }

                    if(Client.gameState == GameState.PLAYING) {
                        soundButton {
                            minWidth = 200f
                            text = "Leave Game"
                            onPressed += {
                                KtScope.launch {
                                    Client.connectionManager.sendPacket(
                                        PacketType.GAME_LEAVE.toPacket()
                                    )
                                    Client.gameState = GameState.SERVER_JOIN_MENU
                                    delay(500)
                                    onSelection.invoke(ServerLobbyScene::class)
                                }
                            }
                        }
                    }

                    soundButton {
                        minWidth = 200f
                        text = "Leave Server"
                        onPressed += {
                            KtScope.launch {
                                if(GameState.PLAYING == Client.gameState) {
                                    Client.connectionManager.sendPacket(
                                        PacketType.GAME_LEAVE.toPacket()
                                    )
                                    delay(1000)
                                }


                                onSelection.invoke(MenuScene::class)
                                if (Client.connectionManager.isConnected())
                                    Client.connectionManager.disconnect()
                            }
                        }
                    }

                    soundButton {
                        minWidth = 200f
                        onReady += {
                            if (context.platform != Context.Platform.DESKTOP) {
                                enabled = false
                            }
                        }
                        text = "Quit"
                        onPressed += {
                            context.close()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onResume.clear()
        onSettings.clear()
    }
}
