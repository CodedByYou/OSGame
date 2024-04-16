package me.codedbyyou.os.client.game.scenes

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.littlekt.util.viewport.FitViewport
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.ui.dialog.registerMenuDialog
import kotlin.reflect.KClass
import kotlin.time.Duration

class ServerMenuJoinScene(private val onSelection: suspend (KClass<out Scene>) -> Unit,
                          context: Context) : Scene(context = context){

    private val graph = sceneGraph(context = context) {
        viewport {
            viewport = FitViewport(context.graphics.width, context.graphics.height)
            this.registerMenuDialog(
                onSelection
            ) {}
        }
    }

    override suspend fun Context.show() {
        graph.initialize()
        graph.root.enabled = true
        graph.resize(graphics.width, graphics.height)
    }

    override fun Context.render(dt: Duration) {
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT)

        if (input.isKeyPressed(Key.ESCAPE)) {
            KtScope.launch {
                onSelection(MenuScene::class)
            }
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