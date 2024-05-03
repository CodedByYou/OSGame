package me.codedbyyou.os.client.game.scenes

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.*
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.font.BitmapFontCache
import com.lehaine.littlekt.graphics.g2d.font.VectorFont
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.Signal
import com.lehaine.littlekt.util.viewport.*
import me.codedbyyou.os.client.game.Game
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.ui.dialog.*
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * LoginScene
 * @author Abdollah Kandrani
 * @since 1.0
 * @version 1.0
 */
class ServerLobbyScene(
    private val onSelection: suspend (KClass<out Scene>) -> Unit,
    context: Context
) : Scene(context)
{
    companion object {
        var INSTANCE : ServerLobbyScene? = null
            set(value) {
                if (field != null) {
                    return
                }
                field = value
            }
    }
    private val fontCache2 = BitmapFontCache(Fonts.default)
    val myBatch = SpriteBatch(context)
    private val camera = OrthographicCamera(context.graphics.width, context.graphics.height)
    private val vectorFont = Assets.vectorFont
    private val exitMenuSignal = Signal()
    private var container : CenterContainer? = null
    private var serverList : ServerInfoDialog? = null
    private var info : RoomInfoDialog? = null
    private var text = VectorFont.TextBlock(
        10f,
        25f, mutableListOf(VectorFont.Text("${Client?.user?.psuedoName}",24, Color.LIGHT_RED)))
    private val viewport_ = ExtendViewport(context.graphics.width, context.graphics.height, camera).also { it.apply { context } }
    private var exitMenu : Node? = null
    private var textureRect : TextureRect? = null

    val graph = sceneGraph(context, batch = myBatch) {
        viewport {
            viewport  = viewport_
            textureRect = textureRect {
                slice = Game.backgroundImage?.slice()
                stretchMode = TextureRect.StretchMode.KEEP_ASPECT_CENTERED
            }
            control {
                debugColor = Color.RED
                horizontalSizeFlags = Control.SizeFlag.FILL
                verticalSizeFlags = Control.SizeFlag.FILL
                anchor(Control.AnchorLayout.CENTER)
                container = centerContainer {
                    anchor(Control.AnchorLayout.CENTER)
                    stretchRatio = 1f
                    horizontalSizeFlags = Control.SizeFlag.FILL
                    verticalSizeFlags = Control.SizeFlag.FILL
                }

            }
            serverList = serverInfoDialog()
            chatBox()
            muteBox()
            info  = roomInfoDialog(onSelection, context) {}
            exitMenuSignal+= {
                if (exitMenu != null) {
                    container!!.children.forEach {
                        if (it.name == "SettingsDialog")
                            container!!.removeChild(it)
                    }
                    container!!.removeChild(exitMenu!!)
                    exitMenu = null
                }else {
                    exitMenu = container!!.inGameMenuDialog(onSelection) {}
                }
            }
        }

    }

    init {
        INSTANCE = this
    }

    override suspend fun Context.show() {
        graph.initialize()
        info!!.show();
        info!!.updateRooms.emit();
        graph.root.enabled = true
        vectorFont.resize(context.graphics.width, context.graphics.height, this)
        graph.resize(graphics.width, graphics.height)
    }

    override fun Context.render(dt: Duration) {
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT)


        text = VectorFont.TextBlock(
            10f,
            25f, mutableListOf(VectorFont.Text("${Client?.user?.psuedoName}#${Client?.user?.ticket}",24, Color.RED)))

        viewport_.update(graphics.width, graphics.height, context, true)
        if (input.isKeyJustPressed(com.lehaine.littlekt.input.Key.ESCAPE)) {
            exitMenuSignal.emit()
        }
        if(input.isKeyJustReleased(com.lehaine.littlekt.input.Key.TAB)){
            serverList!!.toggleList.emit()
        }
        if (input.isKeyJustPressed(com.lehaine.littlekt.input.Key.TAB)) {
            serverList!!.toggleList?.emit()
        }
        if (input.isKeyJustReleased(com.lehaine.littlekt.input.Key.ENTER)) {
            info!!.updateRooms.emit()
        }
        graph.update(dt)
        camera.update()
        graph.render()
        textureRect?.slice = Game.backgroundImage?.slice()
        vectorFont.queue(text)
        vectorFont.flush(viewport_.camera.viewProjection)
    }

    override fun Context.resize(width: Int, height: Int) {
        camera.virtualHeight = height.toFloat()
        camera.virtualWidth = width.toFloat()
        viewport_.update(width, height, context, true)
        graph.resize(width, height, true)
    }

    override fun Context.dispose() {
        graph.dispose()
        fontCache2.clear()
        super.dispose()
    }

    override suspend fun Context.hide() {
        graph.root.enabled = false
        graph.releaseFocus()
    }

}