package me.codedbyyou.os.client.game

import com.lehaine.littlekt.AssetProvider
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Game
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.g2d.use
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.input.Key
import kotlinx.coroutines.launch
import me.codedbyyou.os.client.game.manager.ConnectionManager
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.resources.Config
import me.codedbyyou.os.client.game.scenes.LoginScene
import me.codedbyyou.os.client.game.scenes.MenuScene
import me.codedbyyou.os.client.game.scenes.ServerMenuJoinScene
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
    }
    private suspend fun <T : Scene> onSelection(scene: KClass<out T>) =
        setScene(scene)

    override suspend fun Context.start() {

        KtScope.launch {
            backgroundImage = context.resourcesVfs["bg.png"].readTexture()
        }
        Assets.createInstance(context = context){}

        setSceneCallbacks(this)
        sceneGraph(this) {
            onRender {
                if (input.isKeyJustPressed(Key.M))
                Config.musicMultiplier = if (Config.musicMultiplier > 0f) 0f else 1f
                if (backgroundImage == null)
                    return@onRender

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

        addScene(LoginScene::class, LoginScene(
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



}