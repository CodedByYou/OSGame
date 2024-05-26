package me.codedbyyou.os.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.software.project.ui.controller.NavigationController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.core.models.Title
import me.codedbyyou.os.game.client.Client
import me.codedbyyou.os.game.data.*
import me.codedbyyou.os.game.resource.AudioPlayer
import me.codedbyyou.os.game.resource.Config
import me.codedbyyou.os.game.ui.manager.TitleManager
import me.codedbyyou.os.game.ui.theme.AppTheme
import me.codedbyyou.os.game.ui.view.*
import me.codedbyyou.os.game.utils.Logger
import java.awt.Toolkit

@Composable
fun App() {
    AppTheme (
        useDarkTheme = true
    ) {
        Surface(
            color = Color.Black
        ) {
            Box(
                modifier = Modifier.fillMaxSize(1f)
            ) {
                CurrentView()
                TitleDisplay()
                LeaderboardDialog()
            }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            TitleManager.checkAndSetCurrentTitle()
            delay(200)

        }
    }
}

@Composable
fun CurrentView() {
    NavigationController.loadView()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TitleDisplay() {
    val title = remember { TitleManager.currentTitle }
    if (title.value == null) return

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(title.value) {
        alpha.snapTo(0f)
        alpha.animateTo(1f, animationSpec = tween(500))  // Fade in over 0.5 seconds
        delay((title.value!!.duration.toLong() * 1000L) - 1000)  // Keep title visible for most of its duration minus fade-out time
        alpha.animateTo(0f, animationSpec = tween(500))  // Fade out over 0.5 seconds
        delay(500)
        TitleManager.skipCurrentTitle()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .padding(16.dp)
                .height((0.35f * LocalWindowInfo.current.containerSize.height).dp)
                .fillMaxHeight()
        ) {
            Text(
                text = title.value!!.text,
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFF9C27B0),
                textAlign = TextAlign.Center
            )
            Text(
                text = title.value!!.subtitle,
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFF3F3F3),
                textAlign = TextAlign.Center
            )
        }
    }

}

fun main() {
    TitleManager.addTitle(Title("Welcome to the Game", "2/3rds somehow", 3f))
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val windowWidth = screenSize.width * 0.8
    val windowHeight = screenSize.height * 0.8
    Client
    Config
    AudioPlayer.playOneShotSound("intro")
    GlobalScope.launch{
        delay(2000)
        AudioPlayer.playBackgroundMusic()
    }
    registerHosts()
    application {
        AppSessionData.exitHandle = {
            GlobalScope.launch {
                if (Client.connectionManager.isConnected()) {
                    try {
                        Client.connectionManager.sendPacket(
                            PacketType.PLAYER_LEAVE.toPacket()
                        )
                        println(
                            "Sent leave packet"
                        )
                    } catch (e: Exception) {
                        println("Failed to send leave packet")
                    }
                }
                delay(500)
                exitApplication()
            }
        }
        Window(
            onCloseRequest = {
                if (Client.connectionManager.isConnected()) {
                    try {
                        Client.connectionManager.sendPacket(
                            PacketType.PLAYER_LEAVE.toPacket()
                        )
                    } catch (e: Exception) {
                        println("Failed to send leave packet")
                    }
                }
                exitApplication()
            },
            onPreviewKeyEvent = {
                if (it.key == Key.Tab) {
                    if(!(it.isCtrlPressed && it.isAltPressed && it.isMetaPressed)) {
                        if (NavigationController.currentView.value!!.title.contains("game",true)) {
                            if ((it.type == KeyEventType.KeyDown) != AppSessionData.showTabList.value)
                                AppSessionData.showTabList.value = it.type == KeyEventType.KeyDown

                        }

                    }
                } else if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    AppSessionData.showMenu.value = !AppSessionData.showMenu.value
                }
                false
            },
            state = rememberWindowState(
                size = WindowSize(
                    width = windowWidth.dp,
                    height = windowHeight.dp
                ),
            )
        ) {
            App()
        }
    }
}

fun registerHosts() {
    Logger.main.info{"Registering hosts..."}
    NavigationController.addNavigationHost("mainMenu") {
        MainScreen()
    }

    NavigationController.addNavigationHost("serverList") { it ->
        ServerView(it)
    }

    NavigationController.addNavigationHost("addServer") {
        AddServerScreen(
            onAddServer = { ip, port ->
                Config.upsertServer(
                    server = Server(
                        ip = ip,
                        port = port.toInt()
                    )
                )
                NavigationController.goBack()
            }
        ){
            NavigationController.goBack()
        }
    }

    NavigationController.addNavigationHost("editServer") {
        val server = it["server"] as Server
        EditServerScreen(
            server,
            onEditServer = { oldIP, ip, port ->
                if (oldIP != ip) {
                    Config.removeServer(
                        server = server
                    )
                }
                ServersViewModel.servers.remove(
                    it["server"] as Server
                )
                Config.upsertServer(
                    server = Server(
                        name = server.name ?: null,
                        ip = ip,
                        port = port.toInt(),
                        psuedoName = server.psuedoName ?: null,
                        ticket = server.ticket ?: null,
                        description = server.description ?: null,
                        maxPlayers = server.maxPlayers ?: null,
                        onlinePlayers = server.onlinePlayers ?: null,
                        status = server.status ?: null
                    )
                )

                NavigationController.goBack()
            }
        ){
            NavigationController.goBack()
        }
    }

    NavigationController.addNavigationHost("game") {

        LaunchedEffect(Unit){
            Client.connectionManager.sendPacket(
                PacketType.GAMES_LIST.toPacket()
            )
        }

        GameLobbyScreen(
            Client.connectionManager.connectedToIP!!,
            onJoinGame = { room ->
                if(room.roomStatus == RoomStatus.NOT_STARTED
                    && room.players.size < room.roomMaxPlayers
                    ){
                        Client.connectionManager.sendPacket(
                            PacketType.GAME_JOIN.toPacket(
                                mapOf("room" to room.roomNumber)
                            )
                        )
                        Client.roomID = room.roomName+"#"+room.roomNumber
                    }
            },
            onStartNewGame = {
                // start new game
//                NavigationController.navigateTo("newGame")
                TitleManager.addTitle(Title("This feature is not yet implemented", "Please wait for the next update or use the command system", 2f))
            }
        )
    }

    NavigationController.addNavigationHost("registerToServer") {
        ServerRegistrationForm(
            "Register to Server",
            "",
            onSubmit = { username ->
                GlobalScope.launch {
                    Client.user = User(username, "")
                    Client.connectionManager.sendPacket(
                        PacketType.SERVER_REGISTER.toPacket(
                            mapOf(
                                "pseudoName" to username,
                                "machineId" to Client.macAddress!!
                            )
                        )
                    )
                    delay(1000)
                }
            },
            onBack = {
                NavigationController.goBack()
            }
        )
    }

    NavigationController.addNavigationHost("inGame") {
        InGameScene(
            Client.user!!.nickTicket,
            serverIp = Client.connectionManager.connectedToIP!!,
        )
    }

    NavigationController.navigateTo("mainMenu")
}

@Composable
fun LeaderboardDialog() {
    if (DialogViewModel.showDialog.value) {
        val points = DialogViewModel.leaderboard.value.map { it.second.toIntOrNull() ?: -999}
        val firstMaxPoint = points.maxOrNull() ?: 0
        val secondMaxPoint = points.filter { it < firstMaxPoint }.maxOrNull() ?: 0
        val thirdMaxPoint = points.filter { it < secondMaxPoint }.maxOrNull() ?: 0

        Dialog(
            onDismissRequest = { DialogViewModel.showDialog.value = false }
        ) {
            Surface(
                shape = RoundedCornerShape(5.dp),
                color = Color(
                    0xFF39424E
                ).copy(0.5f),
                border = BorderStroke(1.dp, Color(0xFF39424E)),
            ) {
                LazyColumn (modifier = Modifier.padding(16.dp)) {
                    items(DialogViewModel.leaderboard.value) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = item.first,
                                modifier = Modifier.weight(1f),
                                color = when (item.second.toInt()) {
                                    firstMaxPoint -> Color(0xFFFFD700)
                                    secondMaxPoint -> Color(0xFFC0C0C0)
                                    thirdMaxPoint -> Color(0xFFCD7F32)
                                    else -> Color.White
                                },
                                style = TextStyle(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                )
                            )
                            Text(text = item.second, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
