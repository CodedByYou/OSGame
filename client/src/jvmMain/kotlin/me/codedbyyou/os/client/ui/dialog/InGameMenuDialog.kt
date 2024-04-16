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
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.game.scenes.MenuScene
import me.codedbyyou.os.client.ui.soundButton
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

                    soundButton {
                        minWidth = 200f
                        text = "Leave Server"
                        onPressed += {
                            KtScope.launch {
                                if (Client.connectionManager.isConnected())
                                    Client.connectionManager.disconnect()
                                onSelection.invoke(MenuScene::class)
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
