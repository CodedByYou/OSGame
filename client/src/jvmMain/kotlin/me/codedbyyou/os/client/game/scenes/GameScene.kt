package me.codedbyyou.os.client.game.scenes

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.resource.HAlign
import com.lehaine.littlekt.graph.node.ui.*
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.OrthographicCamera
import com.lehaine.littlekt.graphics.g2d.SpriteBatch
import com.lehaine.littlekt.graphics.g2d.font.VectorFont
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.util.Signal
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import me.codedbyyou.os.client.game.Game
import me.codedbyyou.os.client.game.enums.GameState
import me.codedbyyou.os.client.game.manager.ConnectionManager
import me.codedbyyou.os.client.game.runtime.client.Client
import me.codedbyyou.os.client.resources.Assets
import me.codedbyyou.os.client.ui.dialog.*
import me.codedbyyou.os.client.ui.soundButton
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import kotlin.math.min
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
    private val extendViewport = ExtendViewport(context.graphics.width, context.graphics.height, camera).also { it.apply { context } }
    private var exitMenu : Node? = null
    private var textureRect : TextureRect? = null
    private var gameStatus = "Starting Soon"
    private var chancesLeftString = "Chances Left: 5"

    init {
        // why is this not running?
        // answer: because the game is not started yet

        KtScope.launch {

            while (true){
                if (Client.gameState == GameState.PLAYING){
                    val packet = Client.connectionManager.gameSceneChannel.receive()
                    println("Received Packet ${packet.packetType}")
                    when(packet.packetType){
                        PacketType.GAME_START -> {
                            gameStatus = "Game Started"
                            println("Game Started")
                        }
                        PacketType.GAME_END -> {
                            gameStatus = "Game Ended"
                        }
                        PacketType.GAME_ROUND_START -> {
                            val data = packet.packetData["data"].toString().split(":")
                            val round = data[0]
                            val chances = data[1]
                            gameStatus = "Round $round"
                            chancesLeftString = "Chances Left: $chances"
                            println(chancesLeftString)
                            guessingButton.enabled = true
                        }
                        PacketType.GAME_ROUND_END -> {
                            leaderboard.enabled = true
                            leaderboard.visible = true
                            gameStatus = "Round Ended"
                            guessingButton.enabled = false
                        }
                        PacketType.GAME_PLAYER_GUESS -> {
//                            some player guessed
                        }
                        else -> {
                            println("Unhandled packet type ${packet.packetType}")
                        }

                    }
                    println(Client.connectionManager.gameSceneChannel.isClosedForReceive)
                }
                delay(100)
            }
        }
    }

    private val leaderboard : PanelContainer
    private val guessBox : PanelContainer
    private val guessingButton : Node
    val graph = sceneGraph(context, batch = myBatch) {
        viewport {
            viewport  = extendViewport
            textureRect = textureRect {
                slice = Game.backgroundImage?.slice()
                stretchMode = TextureRect.StretchMode.KEEP_ASPECT_CENTERED
            }

            leaderboard = panelContainer {
                marginTop = 50f
                marginLeft = 10f

                anchor(Control.AnchorLayout.CENTER)
                minWidth = context.graphics.width * 5 / 10f
                minHeight = context.graphics.height * 7 / 10f
                column {
                    separation = 10

                    label {
                        text = "Leaderboard"
                        fontScale = Vec2f(1.5f, 1.5f)
                        horizontalAlign = HAlign.LEFT
                    }

                    column {
                        separation = 20

                        column {
                            separation = 10
                            column {
                                label {
                                    text = "No Data Available.."
                                    horizontalAlign = HAlign.LEFT
                                    color = Color.RED
                                }
                            }
                        }
                    }
                }
                enabled = false
                visible = false
            }

            guessBox = panelContainer {
                anchor(Control.AnchorLayout.CENTER_BOTTOM)
                row {
                    var input2 = lineEdit {
                        placeholderText = "Enter your guess"
                        minWidth = context.graphics.width * 7 / 10f - 130f

                    }
                    guessingButton = soundButton {
                        text = "Submit"
                        minWidth = 100f
                        enabled = false
                        onPressed += {
                                input2.text?.let {
                                    Client.connectionManager.sendPacket(
                                        PacketType.GAME_PLAYER_GUESS.toPacket(
                                            mapOf("guess" to it)
                                        )
                                    )
                                }
                            input2.text = ""
                            enabled = false
                            visible = false
                        }
                    }
                }

                marginBottom = 10f
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

    override suspend fun Context.show() {
        graph.initialize()
        graph.root.enabled = true
        vectorFont.resize(context.graphics.width, context.graphics.height, this)
        graph.resize(context.graphics.width, context.graphics.height)
    }

    override fun Context.render(dt: kotlin.time.Duration) {
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT)


        text = VectorFont.TextBlock(
            10f,
            25f, mutableListOf(VectorFont.Text("${Client?.user?.psuedoName}#${Client?.user?.ticket}",24, Color.RED)))

        extendViewport.update(graphics.width, graphics.height, context, true)
        if (input.isKeyJustPressed(com.lehaine.littlekt.input.Key.ESCAPE)) {
            exitMenuSignal.emit()
        }
        if(input.isKeyJustReleased(com.lehaine.littlekt.input.Key.TAB)){
            serverList!!.toggleList.emit()
        }
        if (input.isKeyJustPressed(com.lehaine.littlekt.input.Key.TAB)) {
            serverList!!.toggleList?.emit()
        }

        graph.update(dt)
        camera.update()
        graph.render()

        textureRect?.slice = Game.backgroundImage?.slice()
        val roomNameAndCode = "Ask your friends to join Room: default#0"
        val gameStatus = VectorFont.TextBlock(
            extendViewport.camera.virtualWidth/2f - this@GameScene.gameStatus.length*11.5f,
            40f, mutableListOf(VectorFont.Text(this@GameScene.gameStatus,48, Color.DARK_RED)))
        val subGameStatus = VectorFont.TextBlock(
            extendViewport.camera.virtualWidth/2f - roomNameAndCode.length*5.5f,
            80f, mutableListOf(VectorFont.Text(roomNameAndCode,24, Color.RED)))
        // this shall be under the client name
        val chancesLeftVC = VectorFont.TextBlock(
            10f,
            50f, mutableListOf(VectorFont.Text(
                chancesLeftString
                ,24, Color.RED)))
        vectorFont.queue(text)
        vectorFont.queue(gameStatus)
        vectorFont.queue(subGameStatus)
        vectorFont.queue(chancesLeftVC)
        vectorFont.flush(extendViewport.camera.viewProjection)
    }

    override fun Context.dispose() {
        myBatch.dispose()
        super.dispose()
    }

    override suspend fun Context.hide() {
        graph.root.enabled = false
        graph.releaseFocus()
    }

    override fun Context.resize(width: Int, height: Int) {
        extendViewport.update(width, height, this)
        vectorFont.resize(width, height, this)
        graph.resize(width, height)
    }

}