package me.codedbyyou.os.client.game.scenes

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.Fonts
import com.lehaine.littlekt.graphics.OrthographicCamera
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.font.BitmapFontCache
import com.lehaine.littlekt.graphics.g2d.font.VectorFont
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.util.Signal
import com.lehaine.littlekt.util.viewport.ExtendViewport
import me.codedbyyou.os.client.game.Game
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.ui.dialog.*
import kotlin.reflect.KClass

class GameScene(
    private val onSelection: suspend (KClass<out Scene>) -> Unit,
    context: Context
) : Scene(context) {

    var INSTANCE : GameScene? = null
        set(value) {
            if (field != null) {
                return
            }
            field = value
        }


    init {
        INSTANCE = this
    }

    val myBatch = SpriteBatch(context)
    private val camera = OrthographicCamera(context.graphics.width, context.graphics.height)
    private val vectorFont = Assets.vectorFont
    private val exitMenuSignal = Signal()
    private var container : CenterContainer? = null
    private var serverList : ServerInfoDialog? = null
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


}