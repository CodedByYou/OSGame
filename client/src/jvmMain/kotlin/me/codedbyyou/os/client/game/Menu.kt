package me.codedbyyou.os.client.game

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Game
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.file.vfs.readBitmapFont
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.font.BitmapFont
import com.lehaine.littlekt.graphics.g2d.use
import kotlin.time.Duration

class MenuScene(context: Context, private val font: BitmapFont) : Scene(context) {
    private val batch = SpriteBatch(context)

    override  fun Context.render(dt: Duration) {
        batch.use{
            font.draw(batch, "Menu", 100f, 100f)
        }
    }

    override  fun Context.dispose() {
        // dispose of assets
    }
}

class Menu(context: Context) : Game<Scene>(context) {


    override suspend fun Context.start(){
        setSceneCallbacks(this)

        val font = resourcesVfs["display2.ttf"].readBitmapFont()

        addScene(MenuScene(this, font))

        setScene<MenuScene>()
    }

    final fun dispose(){
        context.dispose()
    }

    /*
     * Dispose of the menu assets
     */
    open fun Context.dispose(){

    }

}