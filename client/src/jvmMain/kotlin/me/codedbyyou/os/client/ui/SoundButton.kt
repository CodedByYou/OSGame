package me.codedbyyou.os.client.ui

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graph.node.ui.Button
import com.lehaine.littlekt.graph.node.ui.button
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config

fun Node.soundButton(callback: @SceneGraphDslMarker Button.() -> Unit) = button {
    onFocus += {
        Assets.sfxSelect.play(0.1f * Config.sfxMultiplier)
    }

    callback()
}