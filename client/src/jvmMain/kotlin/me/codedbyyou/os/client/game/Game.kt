package me.codedbyyou.os.client.game

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Experimental
import com.lehaine.littlekt.Game
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.node.ViewportCanvasLayer
import com.lehaine.littlekt.graph.node.ui.Control
import com.lehaine.littlekt.graph.node.ui.paddedContainer
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.*
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.font.BitmapFont
import com.lehaine.littlekt.graphics.g2d.font.VectorFont
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.game.manager.TitleManager
import me.codedbyyou.os.client.game.scenes.GameScene
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.game.scenes.ServerLobbyScene
import me.codedbyyou.os.client.game.scenes.MenuScene
import me.codedbyyou.os.client.game.scenes.ServerMenuJoinScene
import me.codedbyyou.os.core.models.Title
import kotlin.reflect.KClass

class Game(context: Context) : Game<Scene>(context) {

    companion object {
        var backgroundImage : Texture? = null
            set(value) {
                if (field != null) {
                    return
                }
                field = value
            }
        var INSTANCE : me.codedbyyou.os.client.game.Game? = null
            set(value) {
                if (field != null) {
                    return
                }
                field = value
            }
    }
    init {
        INSTANCE = this
    }


    private val titleManager = TitleManager

    private var font : BitmapFont? = null
    private var vectorFont : VectorFont? = null

    private suspend fun <T : Scene> onSelection(scene: KClass<out T>) =
        setScene(scene)

    private val camera = OrthographicCamera(context.graphics.width, context.graphics.height)
    private var viewport_ = ExtendViewport(context.graphics.width, context.graphics.height, camera)
    private var myBatch = SpriteBatch(context)

    @OptIn(Experimental::class)
    override suspend fun Context.start() {
        KtScope.launch {
            backgroundImage = context.resourcesVfs["bg.png"].readTexture()
            font = Fonts.default
            vectorFont = Assets.vectorFont
        }
        Assets.createInstance(context = context){}

        setSceneCallbacks(this)
        sceneGraph(this) {
            val port : ViewportCanvasLayer
                    = viewport {
                viewport = viewport_
                paddedContainer {
                    minWidth = this@Game.graphics.width.toFloat()
                    minHeight = this@Game.graphics.height.toFloat()
                    verticalSizeFlags = Control.SizeFlag.FILL
                }
            }

            this@Game.context.onRender {
                camera.update()
                if (input.isKeyJustPressed(Key.M))
                    Config.musicMultiplier = if (Config.musicMultiplier > 0f) 0f else 1f
                if (backgroundImage == null)
                    return@onRender

                val title = titleManager.getCurrentTitle()
                if (title != null) {
                    this@Game.context.renderText(title)
                }

            }


            Assets.music.play(Config.musicMultiplier, true)
        }.initialize()

        addScene(
            MenuScene::class,
            MenuScene(
            ::onSelection,
            this ))

        addScene(ServerMenuJoinScene::class, ServerMenuJoinScene(
            ::onSelection,
            this
        ))

        addScene(GameScene::class, GameScene(
            ::onSelection,
            context = this
        ))

        addScene(ServerLobbyScene::class, ServerLobbyScene(
            ::onSelection,
            context = this
        ))


        setScene<MenuScene>()
//        setScene<LoginScene>()
//        setScene<ServerMenuJoinScene>()
//        ConnectionManager.serverScreenCallBack = {
//                setScene<ServerMenuJoinScene>()
//        }
    }

    private fun Context.renderText(title: Title) {
        val elapsedTime = System.currentTimeMillis() - titleManager.getLastTitleTime()
        val fadeDuration = title.duration * 1000 // Convert fade duration to milliseconds


        val gamma =  elapsedTime / fadeDuration
        if(title.text != ""){
            vectorFont?.queue(
                VectorFont.TextBlock(
                    -title.text.length * 11.5f,
                    -(viewport_.camera.viewProjection.scaleY / 4) - (viewport_.camera.virtualHeight / 4),
                    mutableListOf(
                        VectorFont.Text(
                            title.text, 48,
                            Color.LIME.toLinear().gamma(
                                gamma
                            )
                        )
                    )
                )
            )
        }

        if (title.subtitle != ""){
            vectorFont?.queue(
                VectorFont.TextBlock(
                    -title.subtitle.length * 5.5f,
                    -(viewport_.camera.viewProjection.scaleY / 4) - (viewport_.camera.virtualHeight / 4) + 50,
                    mutableListOf(
                        VectorFont.Text(
                            title.subtitle, 24,
                            Color.LIME.toLinear().gamma(
                                gamma
                            )
                        )
                    )
                )
            )
        }



        vectorFont?.flush(viewport_.camera.viewProjection)

        // Check if the text has finished fading
        if (elapsedTime > fadeDuration) {
            vectorFont?.flush(viewport_.camera.viewProjection)
        }
    }

}