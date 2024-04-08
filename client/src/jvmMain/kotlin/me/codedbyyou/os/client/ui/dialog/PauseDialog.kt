package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.util.signal
import me.codedbyyou.os.client.ui.soundButton


fun Node.pauseDialog(callback: PauseDialog.() -> Unit) = node(PauseDialog(), callback)

class PauseDialog : CenterContainer() {

    val onResume = signal()
    val onSettings = signal()

    init {
        anchorRight = 1f
        anchorBottom = 1f
        onSettings.plusAssign {
            this.parent?.settingsDialog {
                this@PauseDialog.visible = false
                this.onBack.plusAssign {
                    this@PauseDialog.visible=true
                    this.destroy()
                }
            }
        }
        panelContainer {
            paddedContainer {
                padding(10)
                column {
                    separation = 10
                    label {
                        text = "Paused"
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