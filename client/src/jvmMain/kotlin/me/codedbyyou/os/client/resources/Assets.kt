package me.codedbyyou.os.client.resources

import com.lehaine.littlekt.AssetProvider
import com.lehaine.littlekt.BitmapFontAssetParameter
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Disposable
import com.lehaine.littlekt.audio.AudioClip
import com.lehaine.littlekt.audio.AudioStream
import com.lehaine.littlekt.file.vfs.readTtfFont
import com.lehaine.littlekt.graph.node.resource.NinePatchDrawable
import com.lehaine.littlekt.graph.node.resource.Theme
import com.lehaine.littlekt.graph.node.resource.createDefaultTheme
import com.lehaine.littlekt.graph.node.ui.Button
import com.lehaine.littlekt.graph.node.ui.Label
import com.lehaine.littlekt.graph.node.ui.Panel
import com.lehaine.littlekt.graph.node.ui.ProgressBar
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.Fonts
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.*
import com.lehaine.littlekt.graphics.g2d.font.BitmapFont
import com.lehaine.littlekt.graphics.g2d.font.TtfFont
import com.lehaine.littlekt.graphics.g2d.font.VectorFont
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.util.fastForEach
import kotlin.jvm.Volatile
import kotlin.time.Duration.Companion.milliseconds

class Assets private constructor(context: Context) : Disposable {
    private val assets = AssetProvider(context)
//    private val atlas: TextureAtlas by assets.load(context.resourcesVfs["tiles.atlas.json"])
//    private val levelUp by assets.prepare { atlas.getAnimation("levelUp") }

    private var theme: Theme? = null

//    private val sfxCollect: AudioClip by assets.load(context.resourcesVfs["sfx/collect0.wav"])
    private val sfxSelect: AudioClip by assets.load(context.resourcesVfs["sfx/minecraft_click.wav"])
//    private val sfxWarning: AudioClip by assets.load(context.resourcesVfs["sfx/warning0.wav"])
    private val music: AudioStream by assets.load(context.resourcesVfs["sfx/c418_mc_music.wav"])
    private val font: TtfFont by assets.load(context.resourcesVfs["display1.ttf"])
    private val bitmapFont: BitmapFont by assets.load(context.resourcesVfs["display.fnt"], BitmapFontAssetParameter())
    private val vectorFont: VectorFont by lazy { VectorFont(font).also { it.prepare(context) }}

//    private val background : Texture by assets.load(context.resourcesVfs["bg.png"])

    init {
        assets.prepare {
//            val button9p = NinePatch(atlas.getByPrefix("uiButton").slice, 1, 1, 1, 1)
//            val buttonHighlight9p = NinePatch(atlas.getByPrefix("uiButtonHighlight").slice, 1, 1, 1, 1)
//            val panel9p = NinePatch(atlas.getByPrefix("uiPanel").slice, 15, 15, 15, 1)
//            val outline9p = NinePatch(atlas.getByPrefix("uiOutline").slice, 1, 1, 1, 1)
//            val pixel9p = NinePatch(atlas.getByPrefix("fxPixel").slice, 0, 0, 0, 0)
//            theme = createDefaultTheme(
//                extraDrawables = mapOf(
//                    "Button" to mapOf(
//                        Button.themeVars.normal to NinePatchDrawable(button9p).apply {
//                            modulate = Color.RED.toMutableColor().scaleRgb(1f)
//                        },
//                        Button.themeVars.pressed to NinePatchDrawable(button9p).apply {
//                            modulate = Color.WHITE.toMutableColor().scaleRgb(0.6f)
//                        },
//                        Button.themeVars.hover to NinePatchDrawable(buttonHighlight9p),
//                        Button.themeVars.focus to NinePatchDrawable(outline9p),
//                        Button.themeVars.disabled to NinePatchDrawable(buttonHighlight9p).apply {
//                            modulate = Color.WHITE.toMutableColor().scaleRgb(0.6f)
//                        }
//                    ),
//                    "Panel" to mapOf(
//                        Panel.themeVars.panel to NinePatchDrawable(panel9p)
//                    ),
//                    "ProgressBar" to mapOf(
//                        ProgressBar.themeVars.bg to NinePatchDrawable(pixel9p).apply {
//                            modulate = Color.fromHex("#422e37")
//                        },
//                        ProgressBar.themeVars.fg to NinePatchDrawable(pixel9p).apply {
//                            modulate = Color.fromHex("#994551")
//                        }
//                    ),
//                ),
//                extraColors = mapOf(
//                    "Label" to mapOf(
//                        Label.themeVars.fontColor to Color.fromHex("#f2e6e6")
//                    ),
//                    "Button" to mapOf(
//                        Button.themeVars.fontColor to Color.fromHex("#f2e6e6")
//                    ),
//                    "ProgressBar" to mapOf(
//                        ProgressBar.themeVars.fontColor to Color.fromHex("#f2e6e6")
//                    )
//                ),
//                defaultFont = Fonts.default
//            )
//
//            Theme.defaultTheme = theme!!
        }
    }

    override fun dispose() {
//        atlas.dispose()
//        sfxCollect.dispose()
        sfxSelect.dispose()
//        sfxWarning.dispose()
        music.dispose()
    }

    companion object {
        @Volatile
        private var instance: Assets? = null
        private val INSTANCE: Assets get() = instance ?: error("Instance has not been created!")

//        val atlas: TextureAtlas get() = INSTANCE.atlas
//        val levelUp: Animation<TextureSlice> get() = INSTANCE.levelUp

//        val sfxCollect get() = INSTANCE.sfxCollect

        val sfxSelect get() = INSTANCE.sfxSelect
//        val sfxWarning get() = INSTANCE.sfxWarning
        val vectorFont get() = INSTANCE.vectorFont
        val bitmapFont get() = INSTANCE.bitmapFont
        val theme get() = INSTANCE.theme

        val music get() = INSTANCE.music

        fun createInstance(context: Context, onLoad: () -> Unit): Assets {
            check(instance == null) { "Instance already created!" }
            val newInstance = Assets(context)
            instance = newInstance
            INSTANCE.assets.onFullyLoaded = onLoad

            context.onRender { INSTANCE.assets.update() }
            return newInstance
        }

        fun dispose() {
            instance?.dispose()
        }
    }
}