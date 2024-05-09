package me.codedbyyou.os.client.game.scenes

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.g2d.font.BitmapFont
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.littlekt.util.viewport.FitViewport
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.dialog.startDialog
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * ServerChoosal
 * @author Abdollah Kandrani
 * @since 1.0
 * @version 1.0
 */
class MenuScene(
    private val onSelection: suspend (KClass<out Scene>) -> Unit,
    context: Context, private val font: BitmapFont? = null) : Scene(context) {
    val graph = sceneGraph(context) {
        viewport {
            viewport = FitViewport(context.graphics.width, context.graphics.height)
            this.startDialog(onSelection) {}
        }
    }
    private var firstLoaded = false

    override suspend fun Context.show() {
        graph.initialize()
        graph.root.enabled = true
        graph.resize(graphics.width, graphics.height)
    }

    override fun Context.render(dt: Duration) {
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT)
        if (!firstLoaded) {
            firstLoaded = true
            Assets.intro.play(Config.sfxMultiplier)
        }
        graph.update(dt)
        graph.render()
    }

    override fun Context.resize(width: Int, height: Int) {
        graph.resize(width, height)
    }

    override fun Context.dispose() {
        graph.dispose()
        super.dispose()
    }

    override suspend fun Context.hide() {
        graph.root.enabled = false
        graph.releaseFocus()
    }
}