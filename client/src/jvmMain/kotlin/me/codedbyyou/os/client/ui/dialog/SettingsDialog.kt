package me.codedbyyou.os.client.ui.dialog

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.resource.AlignMode
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.util.signal
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.soundButton

// how to use:?
// anwer
fun Node.settingsDialog(callback: SettingsDialog.() -> Unit = {}) = node(SettingsDialog(), callback)

class SettingsDialog : CenterContainer() {

    val onKeyboardChange = signal()
    val onBack = signal()

    init {
        anchorRight = 1f
        anchorBottom = 1f
        name = "SettingsDialog"
        panelContainer {
            paddedContainer {
                padding(10)
                column {
                    separation = 10
                    label {
                        text = "Settings"
                        horizontalAlign = HAlign.CENTER
                    }

                    label {
                        text = "Music:"
                    }

                    row {
                        name = "Music Container"
                        separation = 5
                        align = AlignMode.CENTER
                        val progressBar = ProgressBar()
                        progressBar.step = Config.soundAdjustStepRate
                        soundButton {
                            text = "-"
                            onUpdate += {
                                if (pressed && it.inWholeSeconds < 0.5) {
                                    progressBar.value -= progressBar.step
                                }
                            }
                        }
                        node(progressBar) {
                            ratio = Config.musicMultiplier
                            minWidth = 100f
                            onValueChanged += {
                                Config.musicMultiplier = ratio
                            }
                        }
                        soundButton {
                            text = "+"
                            onUpdate += {
                                if (pressed) {
                                    progressBar.value += progressBar.step
                                }
                            }
                        }
                    }

                    label {
                        text = "Sfx:"
                    }

                    row {
                        name = "Sfx Container"
                        separation = 5
                        align = AlignMode.CENTER
                        val progressBar = ProgressBar()
                        progressBar.step = Config.soundAdjustStepRate
                        soundButton {
                            text = "-"
                            onUpdate += {
                                if (pressed) {
                                    progressBar.value -= progressBar.step
                                }
                            }
                        }
                        node(progressBar) {
                            ratio = Config.sfxMultiplier
                            minWidth = 100f
                            onValueChanged += {
                                Config.sfxMultiplier = ratio
                            }
                        }
                        soundButton {
                            text = "+"
                            onUpdate += {
                                if (pressed) {
                                    progressBar.value += progressBar.step
                                }
                            }
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

    override fun onDestroy() {
        super.onDestroy()
        onBack.clear()
        onKeyboardChange.clear()
    }
}