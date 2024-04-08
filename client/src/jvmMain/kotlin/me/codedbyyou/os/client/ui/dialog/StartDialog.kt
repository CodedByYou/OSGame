package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.util.signal
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton


fun Node.startDialog(callback: StartDialog.() -> Unit) = node(StartDialog(), callback)

class StartDialog : CenterContainer() {
    val size       = Config.VSIZE()
    val onServers  = signal()
    val onSettings = signal()

    init {
        anchorRight = 1f;
        anchorBottom = 1f
        onServers.plusAssign {
            this.parent?.serverDialog(size.first, size.second) {
                this@StartDialog.visible = false
                this.onBack.plusAssign {
                    this@StartDialog.parent?.removeChild(this)
                    this@StartDialog.visible = true
                }
            }
        }

        onSettings.plusAssign {
            this.parent?.settingsDialog {
                this@StartDialog.visible = false
                this.onBack.plusAssign {
                    this@StartDialog.parent?.removeChild(this)
                    this@StartDialog.visible = true
                }
            }
        }
        theme = Assets.theme
        panelContainer {
            minWidth = 400f
            paddedContainer {
                padding(10)
                column {
                    stretchRatio = 1f
                    separation = 10
                    label {
                        text = "Main Menu"
                        horizontalAlign = HAlign.CENTER
                    }


                    soundButton {
                        width = this@column.width * 0.8f
                        text = "Servers"
                        onReady += {
                            scene?.requestFocus(this)
                        }
                        onPressed += {
                            onServers.emit()
                        }
                    }

                    soundButton {
                        stretchRatio = 1f
                        text = "Settings"
                        onPressed += {
                            onSettings.emit()
                        }
                    }

                    soundButton {
                        stretchRatio = 1f
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
        onServers.clear()
        onSettings.clear()
    }
}