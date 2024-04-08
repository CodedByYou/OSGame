package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.util.signal
import me.codedbyyou.os.client.ui.soundButton

enum class UpsertType(){
    ADD, UPDATE
}
fun Node.upsertServerDialog(type: Server?, callback: UpsertServerDialog.() -> Unit = {}, ) = node(UpsertServerDialog(type), callback)

class UpsertServerDialog(val chosenServer: Server?) : CenterContainer() {
        val onBack = signal()
        val onAddServer = signal()
        val onEditServer = signal()
        val onRemove = signal()
        val type = if (chosenServer == null) UpsertType.ADD else UpsertType.UPDATE
        init {
            anchor(layout = AnchorLayout.CENTER)
            verticalSizeFlags = SizeFlag.FILL
            horizontalSizeFlags = SizeFlag.FILL
            verticalGrowDirection = GrowDirection.BOTH
            horizontalGrowDirection = GrowDirection.BOTH
            panelContainer {
                minWidth = 500f
                height   = 300f
//                height = dialogHeight
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
                                minWidth = 60f
                                text = "Server Details"
                                horizontalAlign = HAlign.CENTER
                            }
                            row {
                                separation = 10
                                label { text = "Server IP" }
                                lineEdit {
                                    text= chosenServer?.ip ?: ""
                                    placeholderText="Enter Server IP"
                                    minWidth = 400f
                                }
                            }
                            row {
                                separation = 10
                                label {
                                    minWidth = 55f
                                    text = "Port"
                                }
                                lineEdit {
                                    text = chosenServer?.port?.toString() ?: ""
                                    placeholderText = "Port Number (default: 13337)"
                                    minWidth = 400f
                                }
                            }
                            row {
                                separation = 10

                                soundButton {
                                    text = "Back"
                                    onPressed += { onBack.emit() }
                                }

                                if (type == UpsertType.UPDATE){
                                    soundButton {
                                        text = "Remove"
                                        onPressed += { onRemove.emit() }
                                    }
                                }

                                soundButton {
                                    text = if (type == UpsertType.UPDATE ) "Update" else "Add"
                                    onPressed += {
                                        if (type == UpsertType.UPDATE)
                                            onEditServer.emit()
                                        else
                                            onAddServer.emit()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }